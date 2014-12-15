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
	private boolean atMnemonicFormat, atOpFormat, atOpCodes, atInsName, firstTabSection, secondTab;
	private boolean firstEntry;
	private MnemonicFormatData currentMnemonicFormat;
	
	public FileParser(String assemblyFile, String specFile){
		
		data = new DataSource();
		
		architecture = false;
		registers = false;
		mnemonicFormat = false;
		instructionFormat = false;
		adt = false;
		
		atMnemonicFormat = true;
		atOpFormat = false;
		atOpCodes = false;
		atInsName = false;
		firstTabSection = true;
		secondTab = false;
		
		firstEntry = true;
		
		currentMnemonicFormat = null;
		
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
		
		data.setArchitecture(line);	
	}
	
	private void analyseRegisters(String line){
		
		if (line.trim().length() == 0)
			return;
		
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
		
		if (line.trim().length() > 0) {	
			
			if (line.startsWith("\t")) {	
				
				if(!atOpFormat){				
				
					if (firstTabSection) {	// op formats
					
						atMnemonicFormat = false;
						atOpFormat = true;
					} 
				
					else if(secondTab){	// opcodes
						
						atOpFormat = false;
						atOpCodes = true;						
						secondTab = false;
					}
					
					else{	// ins name
						
						atOpCodes = false;
						atInsName = true;
					}
				}
			}
			
			else {	// new mnemonic
				resetBooleanValues();
				currentMnemonicFormat = new MnemonicFormatData();
			}
			
			if (atMnemonicFormat) {
				
				String[] tokens = line.split("\\s+");
				String mnemonicName = tokens[0];

				currentMnemonicFormat.setMnemonic(mnemonicName);
			}
			
			else if (atOpFormat) 
				analyseOpFormat(line);
					
			else if (atOpCodes) 
				analyseOpcodes(line);
			
			else if (atInsName) {
				
				String insName = line.replaceAll("\\s+", "");				
				currentMnemonicFormat.setInstructionName(insName);
				
				String mnemonic = currentMnemonicFormat.getMnemonic();				
				data.getMnemonicTable().put(mnemonic, currentMnemonicFormat);
			}
		}
		
		else if(atOpFormat){
			
			firstTabSection = false;
			atOpFormat = false;
			secondTab = true;
		}
	}
	
	private void analyseOpFormat(String line) {
		
		line = line.trim();
		
		currentMnemonicFormat.getOpFormats().add(line);		
	}

	private void resetBooleanValues() {
		
		atMnemonicFormat = true;
		atOpFormat = false;
		atOpCodes = false;
		atInsName = false;
		firstTabSection = true;
		
		currentMnemonicFormat = null;	
	}

	private void analyseOpcodes(String opcodes) {
		
		opcodes = opcodes.replaceAll("\\s+", "");

		String[] tokens = opcodes.split(",");		
		
		for(String token: tokens){
			
			String[] elements = token.split("=");
			currentMnemonicFormat.getOpCodes().put(elements[0], elements[1]);
		}	
	}

	private void analyseInstructionFormat(String line){	
		
		if (line.trim().length() == 0)
			return;
		
		String[] tokens = line.split("\\s+");
		
		String insName = "";
		InstructionFormatData insF = new InstructionFormatData();
		
		for (String token : tokens) {
			
			if(token.endsWith(":")){
				
				token = token.replaceAll("[:]", "");
				insName = token;
			}
			
			else{
				
				String[] tokenTerms = token.split("\\(|\\)");
				String operand = tokenTerms[0];
				int bitSize = Integer.parseInt(tokenTerms[1]);
				insF.getOperands().add(operand);
				insF.getOperandBitHash().put(operand, bitSize);							
			}
		}
		
		insF.setInstructionName(insName);
		data.getInstructionFormat().put(insName, insF);
	}		

	public DataSource getData() {
		return data;
	}	
}
