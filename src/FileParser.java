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
	boolean architecture, registers, opcodeFormat, instructionFormat;
	boolean atOpFormat, atCodes, atInsName, firstTab;
	OpcodeFormatData currentOpFormat;
	
	public FileParser(String assemblyFile, String specFile){
		
		data = new DataSource();
		architecture = false;
		registers = false;
		opcodeFormat = false;
		instructionFormat = false;
		
		atOpFormat = true;
		atCodes = false;
		atInsName = false;
		firstTab = true;
		
		currentOpFormat = null;
		
		scanAssemblyFile(assemblyFile);
		scanSpecFile(specFile);
	}
	
	public void scanAssemblyFile(String fileName) {

		Scanner inputFile = null;	

		try {
			inputFile = new Scanner(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("File " + fileName + " not found.");
			System.exit(0);
		}

		while (inputFile.hasNextLine()) {
			
			ArrayList<String> assembly = new ArrayList<String>();
			String line = inputFile.nextLine();
			
			if(!line.isEmpty()){
				String[] tokens = line.replaceAll("^[,\\s]+", "").split("[,\\s]+");
			
				for(String token: tokens)
					assembly.add(token);
				
			data.getAssemblyCode().add(assembly);
			}
		}
		
		inputFile.close();
	}
	
	public void scanSpecFile(String fileName){
		
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

			if (!line.isEmpty()) {
				
				if (line.startsWith("architecture:")) {
					setBooleanValues(true, false, false, false);
					title = true;
				}

				else if (line.startsWith("registers:")) {
					setBooleanValues(false, true, false, false);
					title = true;
				}

				else if (line.startsWith("opcodeformat:")) {
					setBooleanValues(false, false, true, false);
					title = true;
				}

				else if (line.startsWith("instructionformat:")) {
					setBooleanValues(false, false, false, true);
					title = true;
				}
		
				if (!title) {
					if (architecture)
						analyseArchitecture(line);

					else if (registers)
						analyseRegisters(line);

					else if (opcodeFormat)
						analyseOpcodeFormat(line);

					else if (instructionFormat)
						analyseInstructionFormat(line);
				}
			}
		}
		inputFile.close();
	}
	
	public void setBooleanValues(boolean architecture, boolean registers, boolean opcodeFormat, boolean instructionFormat){
		
		this.architecture = architecture;
		this.registers = registers;
		this.opcodeFormat = opcodeFormat;
		this.instructionFormat = instructionFormat;
	}
	
	public void analyseArchitecture(String line){
		
		String[] tokens = line.split("\\s+");
		
		for (String token : tokens) {
			data.getArchitecture().add(token);					
		}		
	}
	
	public void analyseRegisters(String line){
		
		String[] tokens = line.split("\\s+");
		ArrayList<String> registers = new ArrayList<String>();
		
		for (String token : tokens) {
			if(!token.equals("="))
				registers.add(token);										
		}
		
		putRegistersInHashMap(registers);
	}
	
	
	public void analyseOpcodeFormat(String line){		
		
		String[] tokens = line.split("\\s+");
		OpcodeFormatData opFormat = currentOpFormat; 
		boolean inCodes = false;
		String opCodes = "";
		
		if(line.startsWith("\t")){
			if(firstTab){				
				atCodes = true;
				atOpFormat = false;
				firstTab = false;
			}
			else{
				atCodes = false;
				atInsName = true;
			}
		}
		
		else{
			opFormat = new OpcodeFormatData();
			resetBooleanValues();
			currentOpFormat = opFormat;
		}
		
		boolean first = true;
		
		for(String token: tokens){
			
			if (atOpFormat) {				
				if(first){
					opFormat.setMnemonic(token);
					first = false;
				}
		
				token = token.replaceAll("[,]", "");
				opFormat.getOpFormat().add(token);
			} 
			
			else if (atCodes) {
				opCodes += token;
				inCodes = true;
			} 
			
			else if (atInsName) {				
				if(!token.isEmpty()){
					opFormat.setInstructionName(token);	
					String mnemonic = opFormat.getMnemonic();
					data.getOpcodeFormats().put(mnemonic, currentOpFormat);
				}
			}			
		}
		
		if(inCodes)
			formatConditions(opFormat, opCodes);		
	}
	
	private void resetBooleanValues() {
		
		atOpFormat = true;
		atCodes = false;
		atInsName = false;
		firstTab = true;
		
		currentOpFormat = null;	
	}

	private void formatConditions(OpcodeFormatData opFormat, String opConditions) {

		String[] conditions = opConditions.split(",");		
		
		for(String condition: conditions){
			String[] condElements = condition.split("=");
			opFormat.getOpConditions().put(condElements[0], condElements[1]);
		}	
	}

	public void analyseInstructionFormat(String line){	
		
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
				insF.getOpFormatBitHash().put(operand, bitSize);
							
			}
		}
		insF.setInstructionName(insName);
		data.getInstructionFormat().put(insName, insF);
	}	
	
	public void putRegistersInHashMap(ArrayList<String> registers){		
		
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
