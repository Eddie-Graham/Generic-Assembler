/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileParser {
	
	DataSource data;
	
	boolean architecture, registers, mnemonicFormat, instructionFormat, operandsyntax;
	boolean atMnemonicFormat, atCodes, atInsName, firstTab;
	
	MnemonicFormatData currentMnemonicFormat;
	
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
			
			if (line.trim().length() > 0){
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
			boolean title = false;
			String line = inputFile.nextLine();

			if (line.trim().length() > 0){
				
				if (line.startsWith("architecture:")) {
					setBooleanValues(true, false, false, false, false);
					title = true;
				}

				else if (line.startsWith("registers:")) {
					setBooleanValues(false, true, false, false, false);
					title = true;
				}

				else if (line.startsWith("mnemonicformat:")) {
					setBooleanValues(false, false, true, false, false);
					title = true;
				}

				else if (line.startsWith("instructionformat:")) {
					setBooleanValues(false, false, false, true, false);
					title = true;
				}
				
				else if (line.startsWith("operandsyntax:")) {
					setBooleanValues(false, false, false, false, true);
					title = true;
				}
		
				if (!title) {
					if (architecture)
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
		
		String[] tokens = line.split("\\s+");
		ArrayList<String> registers = new ArrayList<String>();
		
		for (String token : tokens) {
			if(!token.equals("="))
				registers.add(token);										
		}
		
		putRegistersInHashMap(registers);
	}
	
	
	private void analyseMnemonicFormat(String line){		
		
		MnemonicFormatData mnemonicFormat = currentMnemonicFormat;
		
		if (line.trim().length() > 0) {
			if (line.startsWith("\t")) {
				if (firstTab) {
					atCodes = true;
					atMnemonicFormat = false;
					firstTab = false;
				} else {
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
				formatOpcodes(mnemonicFormat, line);
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

	private void formatOpcodes(MnemonicFormatData mnemonicFormat, String opcodes) {

		opcodes = opcodes.replaceAll("\\s+", "");

		String[] conditions = opcodes.split(",");		
		
		for(String condition: conditions){
			String[] elements = condition.split("=");
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
	
	private void putRegistersInHashMap(ArrayList<String> registers){		
		
		if(registers.get(0).contains("-")){
				
			String label = registers.get(0);
			String hexTerm = registers.get(1);						
			
			String[] labelNoDash = label.split("-");	// needed for final conversion before entry into hashmap
			String[] hexNoDash = hexTerm.split("-");	// needed for final conversion before entry into hashmap
			
			String tempLabelNum = label.replaceAll("[^\\d.^-]", "");	// remove all except numbers and "-"
			String[] LabelNumTerms = tempLabelNum.split("-");	// split label numbers in array
			
			String tempHex = hexTerm.replaceAll("[^\\d.^-]", "");	// remove all except numbers and "-"
			String[] hexNumTerms = tempHex.split("-");	// split hex numbers in array
			
			int lowerIntHex = Integer.parseInt(hexNumTerms[0]);	// get first hex Int
			
			int lowerIntLabel = Integer.parseInt(LabelNumTerms[0]);	// get first label Int
			int upperIntLabel = Integer.parseInt(LabelNumTerms[1]);	// get last label Int
			
			String currentLabel = labelNoDash[0];	// current label to work with 
			String currentHex = hexNoDash[0];	// current hex to work with 
			
			while(lowerIntLabel <= upperIntLabel){
				String regLabel = currentLabel.replaceAll("[\\d]", Integer.toString(lowerIntLabel));
				String hex = currentHex.replaceAll("[\\d]", Integer.toString(lowerIntHex));
				
				lowerIntHex++;
				lowerIntLabel++;

				data.getRegisterHash().put(regLabel, hex);
			}
		}
		else{
			String regLabel = registers.get(0);
			String hex = registers.get(1);			
			data.getRegisterHash().put(regLabel, hex);
		}
	}	

	public DataSource getData() {
		return data;
	}	
}
