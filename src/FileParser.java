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
	
	public FileParser(String assemblyFile, String specFile){
		
		data = new DataSource();
		architecture = false;
		registers = false;
		opcodeFormat = false;
		instructionFormat = false;
		
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
			String line = inputFile.nextLine();

			if (!line.isEmpty()) {				
				String[] tokens = line.split("\\s+");

				if(tokens.length == 1){
					String token = tokens[0];

					if (token.equalsIgnoreCase("architecture:")) 
						setBooleanValues(true, false, false, false);					

					if (token.equalsIgnoreCase("registers:")) 
						setBooleanValues(false, true, false, false);							
					
					if (token.equalsIgnoreCase("opcodeformat:")) 
						setBooleanValues(false, false, true, false);
					
					if (token.equalsIgnoreCase("instructionformat:")) 
						setBooleanValues(false, false, false, true);					
					
				}

				else if (architecture)
					analyseArchitecture(tokens);			

				else if (registers) 	
					analyseRegisters(tokens);				
			
				else if (opcodeFormat)
					analyseOpcodeFormat(tokens);

				else if (instructionFormat)
					analyseInstructionFormat(tokens);				
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
	
	public void analyseArchitecture(String[] tokens){
		
		for (String token : tokens) {
			data.getArchitecture().add(token);					
		}		
	}
	
	public void analyseRegisters(String[] tokens){
		
		ArrayList<String> registers = new ArrayList<String>();
		
		for (String token : tokens) {				
			registers.add(token);										
		}
		putRegistersInHashMap(registers);
	}
	
	
	public void analyseOpcodeFormat(String[] tokens){		
		
		OpcodeFormatData opFormat = new OpcodeFormatData();
		
		boolean atFormat = true, first = true, atConditions = false;
		String mnemonic = "";
		String opConditions = "";
		
		for(String token: tokens){
			
			if(first){	// first token mnemonic
				mnemonic = token;
				opFormat.setMnemonic(mnemonic);
				first = false;
			}
			
			else if(token.startsWith("(")){		// at condition tokens		
				atFormat = false;
				atConditions = true;
			}	
			
			if(atFormat){	// at format tokens
				token = token.replaceAll("[,]", "");
				opFormat.getOpFormat().add(token);
			}
			else if(atConditions){
				
				if(token.endsWith(")")){	// end of condition tokens
					opConditions+= token;
					formatConditions(opFormat, opConditions);
					atConditions = false;
				}	
				else
					opConditions+= token;
			}
			
			else	// at last token (instruction name)
				opFormat.setInstructionName(token);					
		}
		data.getOpcodeFormats().put(mnemonic, opFormat);		
	}
	
	private void formatConditions(OpcodeFormatData opFormat, String opConditions) {

		String condNoBracket = opConditions.replaceAll("[()]", "");
		String[] conditions = condNoBracket.split(",");		
		
		for(String condition: conditions){
			String[] condElements = condition.split("=");
			opFormat.getOpConditions().put(condElements[0], condElements[1]);
		}	
	}

	public void analyseInstructionFormat(String[] tokens){	
		
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
						
			String hexTerm = registers.get(0);
			String label = registers.get(1);			
			
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
	

	public DataSource getData() {
		return data;
	}	
}
