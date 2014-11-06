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
				
				else if (line.startsWith("operandprefixes:")) 
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
					analyseOperandPrefixes(line);
				
			}
		}
		inputFile.close();
	}
	
	private void analyseOperandPrefixes(String line) {
		
		String[] tokens = line.split("\\s+");

		if (tokens[0].equals("immediate:")) {			
			data.getPrefixHash().put(tokens[1], DataSource.OperandType.IMMEDIATE.name());
			data.getPrefixes().add(tokens[1]);
		}
		else if (tokens[0].equals("register:")) {
			data.getPrefixHash().put(tokens[1], DataSource.OperandType.REGISTER.name());
			data.getPrefixes().add(tokens[1]);
		}
		else if (tokens[0].equals("memory:")) {
			data.getPrefixHash().put(tokens[1], DataSource.OperandType.MEMORY.name());
			data.getPrefixes().add(tokens[1]);
		}
		else{
			//error
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
		
		data.setArchitecture(line);	
	}
	
	private void analyseRegisters(String line){
		
		char dataType = line.charAt(line.length()-1);
		line = line.substring(0, line.length()-1);
			
		String[] tokens = line.split("\\s+");
		
		String regLabel = tokens[0];
		String regValue = tokens[1];
	
		if(dataType == 'B'){
			data.getRegisterHash().put(regLabel, regValue);
		}
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
