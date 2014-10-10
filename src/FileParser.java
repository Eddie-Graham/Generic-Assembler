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
	boolean architecture = false, opcodes = false, registers = false, opcodeFormat = false, instructionFormat = false;
	
	public FileParser(String assemblyFile, String specFile){
		
		data = new DataSource();
		
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
			String[] tokens = line.replaceAll("^[,\\s]+", "").split("[,\\s]+");
			
			for(String token: tokens){
				assembly.add(token);
			}
			data.getAssemblyCode().add(assembly);
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
						setBooleanValues(true, false, false, false, false);						

					if (token.equalsIgnoreCase("opcodes:")) 
						setBooleanValues(false, false, true, false, false);						

					if (token.equalsIgnoreCase("registers:")) 
						setBooleanValues(false, true, false, false, false);							
					
					if (token.equalsIgnoreCase("opcodeformat:")) 
						setBooleanValues(false, false, false, true, false);
					
					if (token.equalsIgnoreCase("instructionformat:")) 
						setBooleanValues(false, false, false, false, true);						
					
				}

				if (architecture)
					analyseArchitecture(tokens);			

				if (registers) 	
					analyseRegisters(tokens);				

				if (opcodes) 	
					analyseOpcodes(tokens);				
				
				if (opcodeFormat)
					analyseOpcodeFormat(tokens);

				if (instructionFormat)
					analyseInstructionFormat(tokens);
				
			}
		}
		inputFile.close();
	}
	
	public void setBooleanValues(boolean architecture, boolean registers, boolean opcodes, boolean opcodeFormat, boolean instructionFormat){
		
		this.architecture = architecture;
		this.registers = registers;
		this.opcodes = opcodes;
		this.opcodeFormat = opcodeFormat;
		this.instructionFormat = instructionFormat;
	}
	
	public void analyseArchitecture(String[] tokens){
		
		for (String token : tokens) {
			if(!token.equalsIgnoreCase("architecture:"))						
				data.getArchitecture().add(token);					
		}		
	}
	
	public void analyseRegisters(String[] tokens){
		
		ArrayList<String> registers = new ArrayList<String>();
		boolean done = false;
		
		for (String token : tokens) {
			if(!token.equalsIgnoreCase("registers:")){	
				registers.add(token);	
				done = true;
			}						
		}
		if(done)
			putRegistersInHashMap(registers);
	}
	
	public void analyseOpcodes(String[] tokens){
		
		String op = null;
		String code = null;
		boolean first = true;
		
		for (String token : tokens) {			
			if(!token.equalsIgnoreCase("opcodes:")){
				if(first){
					op = token;
					first = false;
				}
				else
					code = token;
				
				data.getOpcodes().put(op, code);
			}
		}		
	}
	
	public void analyseOpcodeFormat(String[] tokens){
		
		boolean first = true, done = false;
		String op = null;
		ArrayList<String> opformat = new ArrayList<String>();
		
		for(String token: tokens){
			if(!token.equalsIgnoreCase("opcodeformat:")){
				if(first){
					op = token;
					first = false;
				}
				else					
					opformat.add(token);
					
				done = true;
			}			
		}
		if(done)
			data.getOpcodeFormat().put(op, opformat);
	}
	
	public void analyseInstructionFormat(String[] tokens){		//add label in spec.txt?
		
		for (String token : tokens) {
			if(!token.equalsIgnoreCase("instructionformat:")){						
				data.getInstructionFormat().add(token);							
			}						
		}		
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
