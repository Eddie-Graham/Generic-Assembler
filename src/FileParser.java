/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FileParser {
	
	private DataSource data;
	
	private boolean architecture, registers, mnemonicFormat, instructionFormat, operandsyntax;
	private boolean atMnemonicFormat, atCodes, atInsName, firstTab;
	private boolean regFirst;
	private String regValueType;	
	private MnemonicFormatData currentMnemonicFormat;
	
	public FileParser(String assemblyFile, String specFile){
		
		data = new DataSource();
		
		architecture = false;
		registers = false;
		mnemonicFormat = false;
		instructionFormat = false;
		operandsyntax = false;
		
		atMnemonicFormat = true;
		atCodes = false;
		atInsName = false;
		firstTab = true;
		
		regFirst = true;
		
		regValueType = "";
		
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
			
			if (line.trim().length() > 0 && !line.startsWith(";")){				
				String[] commentSplit = line.split(";");
				line = commentSplit[0];
				
				data.getAssemblyCode().add(line);
			}						
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

			if (line.trim().length() > 0 && !line.startsWith(";")){
				
				String[] commentSplit = line.split(";");
				line = commentSplit[0];
				
				if (line.startsWith("architecture:")) 
					setBooleanValues(true, false, false, false, false);

				else if (line.startsWith("registers:")) 
					setBooleanValues(false, true, false, false, false);

				else if (line.startsWith("mnemonicformat:")) 
					setBooleanValues(false, false, true, false, false);

				else if (line.startsWith("instructionformat:")) 
					setBooleanValues(false, false, false, true, false);
				
				else if (line.startsWith("operandsyntax:")) 
					setBooleanValues(false, false, false, false, true);					
	
				else if (architecture)
					analyseArchitecture(line);

				else if (registers)
					analyseRegisters(line);

				else if (mnemonicFormat)
					analyseMnemonicFormat(line);

				else if (instructionFormat)
					analyseInstructionFormat(line);

				else if (operandsyntax)
					analyseOperandSyntax(line);
				
			}
		}
		inputFile.close();
	}
	
	private void analyseOperandSyntax(String line) {
		
		String[] tokens = line.split("\\s+");

		if (tokens[0].equals("immediate:")) {
			data.setImmediateSyntax(tokens[1]);
		}
		else if (tokens[0].equals("register:")) {
			data.setRegisterSyntax(tokens[1]);
		}
		else if (tokens[0].equals("memory:")) {
			data.setMemorySyntax(tokens[1]);
		}		
	}

	private void setBooleanValues(boolean architecture, boolean registers, boolean mnemonicFormat, boolean instructionFormat, boolean operandSyntax){
		
		this.architecture = architecture;
		this.registers = registers;
		this.mnemonicFormat = mnemonicFormat;
		this.instructionFormat = instructionFormat;
		this.operandsyntax = operandSyntax;
	}
	
	private void analyseArchitecture(String line){
		
		String[] tokens = line.split("\\s+");
		
		for (String token : tokens) {
			data.getArchitecture().add(token);					
		}		
	}
	
	private void analyseRegisters(String line){
		
		if(regFirst){
			String[] tokens = line.split("Values in ");			
			regValueType = tokens[1];													
			
			regFirst = false;
		}
		else{
			
			String[] tokens = line.split("\\s+");
		
			String regLabel = tokens[0];
			String regValue = tokens[1];
	
			if(regValueType.equals("binary")){
				data.getRegisterHash().put(regLabel, regValue);
			}
			else if(regValueType.equals("hex")){
				regValue = Assembler.hexToBinary(regValue);
				data.getRegisterHash().put(regLabel, regValue);
			}
			else if(regValueType.equals("decimal")){
				regValue = Assembler.decimalToBinary(regValue);
				data.getRegisterHash().put(regLabel, regValue);
			}
			else{
				//error
			}
		}
	}
	
	
	private void analyseMnemonicFormat(String line){		
		
		MnemonicFormatData mnemonicFormat = currentMnemonicFormat;
		
		if (line.trim().length() > 0) {	
			if (line.startsWith("\t")) {
				if (firstTab) {
					atCodes = true;
					atMnemonicFormat = false;
					firstTab = false;
				} 
				else {
					atCodes = false;
					atInsName = true;
				}
			}
			else {
				mnemonicFormat = new MnemonicFormatData();
				resetBooleanValues();
				currentMnemonicFormat = mnemonicFormat;
			}
			
			if (atMnemonicFormat) {
				String[] tokens = line.split("\\s+");
				String mnemonicName = tokens[0];

				mnemonicFormat.setMnemonic(mnemonicName);
				mnemonicFormat.setMnemonicFormat(line);
			}
			else if (atCodes) {
				analyseOpcodes(mnemonicFormat, line);
			}
			else if (atInsName) {
				String insName = line.replaceAll("\\s+", "");				
				mnemonicFormat.setInstructionName(insName);
				
				String mnemonic = mnemonicFormat.getMnemonic();				
				data.getMnemonicTable().put(mnemonic, currentMnemonicFormat);
			}
		}
	}
	
	private void resetBooleanValues() {
		
		atMnemonicFormat = true;
		atCodes = false;
		atInsName = false;
		firstTab = true;
		
		currentMnemonicFormat = null;	
	}

	private void analyseOpcodes(MnemonicFormatData mnemonicFormat, String opcodes) {

		opcodes = opcodes.replaceAll("\\s+", "");

		String[] tokens = opcodes.split(",");		
		
		for(String token: tokens){
			String[] elements = token.split("=");
			mnemonicFormat.getOpcodes().put(elements[0], elements[1]);
		}	
	}

	private void analyseInstructionFormat(String line){	
		
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
