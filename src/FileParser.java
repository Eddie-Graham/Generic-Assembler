/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class FileParser {
	
	private DataSource data;
	
	private boolean architecture, registers, mnemonicFormat, instructionFormat, adt;
	private boolean atGlobalOpcodes, first, working, emptyLine, atMnemType;
	private boolean atInsLabels, atOpCodes;
	private boolean firstEntry;
	private MnemonicData currentMnemonicFormat;
	private MnemType currentMnemType;
	
	public FileParser(String assemblyFile, String specFile){
		
		data = new DataSource();
		
		architecture = false;
		registers = false;
		mnemonicFormat = false;
		instructionFormat = false;
		adt = false;
		
		atGlobalOpcodes = true;
		first = false;
		working = false;
		emptyLine = false;
		atMnemType = false;
		atInsLabels = true;
		atOpCodes = false;
		
		firstEntry = true;
		
		currentMnemonicFormat = null;
		currentMnemType = null;
		
		scanAssemblyFile(assemblyFile);
		scanSpecFile(specFile);
	}
	
	private void scanAssemblyFile(String fileName) {

		Scanner inputFile = null;	

		try {
			inputFile = new Scanner(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("File " + fileName + " not found.");
			System.exit(0);
		}

		while (inputFile.hasNextLine()) {			
			
			String line = inputFile.nextLine();
			data.getAssemblyCode().add(line);									
		}		
		
		inputFile.close();
	}
	
	private void scanSpecFile(String fileName){
		
		Scanner inputFile = null;

		try {
			inputFile = new Scanner(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("File " + fileName + " not found.");
			System.exit(0);
		}	

		while (inputFile.hasNextLine()) {
			
			String line = inputFile.nextLine();
			String[] commentSplit = line.split(";");
			line = commentSplit[0];				
				
			line = line.replaceAll("\\s+$", "");	// remove end whitespace
				
			if (line.startsWith("architecture:")) 
				setBooleanValues(true, false, false, false, false);

			else if (line.startsWith("registers:")) 
				setBooleanValues(false, true, false, false, false);

			else if (line.startsWith("mnemonicformat:")) 
				setBooleanValues(false, false, true, false, false);

			else if (line.startsWith("instructionformat:")) 
				setBooleanValues(false, false, false, true, false);
				
			else if (line.startsWith("adt:")) 
				setBooleanValues(false, false, false, false, true);					
	
			else if (architecture)
				analyseArchitecture(line);

			else if (registers)
				analyseRegisters(line);

			else if (mnemonicFormat)
				analyseMnemonicFormat(line);

			else if (instructionFormat)
				analyseInstructionFormat(line);

			else if (adt)
				analyseADT(line);				
			}
		
		inputFile.close();
	}
	
	private void analyseADT(String line) {
		
		if (line.trim().length() == 0)
			return;
		
		ADT adt = data.getAdt();
		
		String[] colonSplit = line.split(":");
		String label = colonSplit[0].trim();
		
		if(firstEntry){
			
			String rootTerm = label;
			adt.setRootTerm(rootTerm);
			firstEntry = false;
		}
		
		String conds = colonSplit[1].trim();
		
		ArrayList<String> conditions = new ArrayList<String>();
		
		conditions.add(conds);
		
		ArrayList<String> list = adt.getAdtHash().get(label);
		
		if(list != null)
			list.add(conds);
		
		else		
			adt.getAdtHash().put(label, conditions);
	}

	private void setBooleanValues(boolean architecture, boolean registers, boolean mnemonicFormat, boolean instructionFormat, boolean adt){
		
		this.architecture = architecture;
		this.registers = registers;
		this.mnemonicFormat = mnemonicFormat;
		this.instructionFormat = instructionFormat;
		this.adt = adt;
	}
	
	private void analyseArchitecture(String line){
		
		if (line.trim().length() == 0)
			return;
		
		data.setArchitecture(line.trim());	
	}
	
	private void analyseRegisters(String line){
		
		if (line.trim().length() == 0)
			return;
		
		line = line.trim();
		char dataType = line.charAt(line.length()-1);
		line = line.substring(0, line.length()-1);
			
		String[] tokens = line.split("\\s+");
		
		String regLabel = tokens[0];
		String regValue = tokens[1];
	
		if(dataType == 'B')
			data.getRegisterHash().put(regLabel, regValue);
		
		else if(dataType == 'H'){
			
			regValue = Assembler.hexToBinary(regValue);
			data.getRegisterHash().put(regLabel, regValue);
		}
		
		else if(dataType == 'D'){
			
			regValue = Assembler.decimalToBinary(regValue);
			data.getRegisterHash().put(regLabel, regValue);
		}
		
		else{
			//error
		}
	}	
	
	private void analyseMnemonicFormat(String line){		
		
		if (line.trim().length() == 0){
			
			if(working)			
				emptyLine = true;
			
			return;
		}
						
		if (line.startsWith("\t")) {
				
			if(first && !emptyLine){
					
				atGlobalOpcodes = true;
				first = false;
			}
				
			else
				atMnemType = true;
								
		}
			
		else {	// new mnemonic
				
			resetBooleanValues();
				
			String mnem = line.trim();
				
			currentMnemonicFormat = new MnemonicData();				
			currentMnemonicFormat.setMnemonic(mnem);
				
			data.getMnemonicTable().put(mnem, currentMnemonicFormat);
				
			working = true;
		}
			
		if(atGlobalOpcodes){

			analyseGlobalOpcodes(line);
			atGlobalOpcodes = false;
		}
			
		else if(atMnemType)
			analyseMnemType(line);				
	}	
	
	private void analyseMnemType(String line) {
		
		if (line.startsWith("\t\t")) {

			if(atInsLabels){

				line = line.trim();				
				
				currentMnemType.setInsLabels(line);
				
				atInsLabels = false;
				atOpCodes = true;
			}
			
			else if(atOpCodes){
				
				line = line.replaceAll("\\s+", "");
				
				if(!line.equals("--")){					
				
					String[] tokens = line.split(",");		
				
					for(String token: tokens){
					
						String[] elements = token.split("=");
						currentMnemType.getOpCodes().put(elements[0], elements[1]);
					}
				}
				
				atOpCodes = false;					
			}
			
			else{
				
				line = line.trim();
				
				String[] tokens = line.split("\\s+");
				
				for(String str: tokens)
					currentMnemType.getInstructionFormat().add(str);
			}			
		}
		
		else if (line.startsWith("\t")){
			
			String mnemType = line.trim();

			currentMnemType = new MnemType();
			currentMnemType.setMnemType(mnemType);
			
			currentMnemonicFormat.getMnemTypes().add(mnemType);
			currentMnemonicFormat.getMnemTypeHash().put(mnemType, currentMnemType);
			
			atInsLabels = true;
			atOpCodes = false;
		}		
	}

	private void resetBooleanValues() {
		
		atGlobalOpcodes = false;
		first = true;
		working = false;
		emptyLine = false;
		atMnemType = false;
		
		currentMnemonicFormat = null;	
	}

	private void analyseGlobalOpcodes(String opcodes) {
		
		opcodes = opcodes.replaceAll("\\s+", "");

		String[] tokens = opcodes.split(",");		
		
		for(String token: tokens){
			
			String[] elements = token.split("=");
			currentMnemonicFormat.getGlobalOpCodes().put(elements[0], elements[1]);
		}	
	}		
	
	private void analyseInstructionFormat(String line){		
		
		if (line.trim().length() == 0)
			return;		

		InstructionFormatData insF = new InstructionFormatData();
		
		String[] tokens = line.split(":");		
		
		String insName = tokens[0].trim();		
		String operands = tokens[1].trim();
		
		String[] operandTokens = operands.split("\\s+");
		
		for(String operand: operandTokens){
			
			String[] tokenTerms = operand.split("\\(|\\)");
			
			String op = tokenTerms[0];
			int bitSize = Integer.parseInt(tokenTerms[1]);
			
			insF.getOperands().add(op);
			insF.getOperandBitHash().put(op, bitSize);			
		}
		
		insF.setInstructionName(insName);
		data.getInstructionFormat().put(insName, insF);
	}	

	public DataSource getData() {
		return data;
	}	
}
