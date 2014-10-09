import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

		while (inputFile.hasNext()) {
			data.getAssemblyCode().add(inputFile.next());
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
		
		String line = null;
		String[] tokens = null;		

		while (inputFile.hasNextLine()) {
			line = inputFile.nextLine();

			if (!line.isEmpty()) {				
				tokens = line.split("\\s+");

				if(tokens.length == 1){
					String token = tokens[0];

					if (token.equalsIgnoreCase("architecture:")) {
						setBooleanValues(true, false, false, false, false);
						
					}

					if (token.equalsIgnoreCase("opcodes:")) {
						setBooleanValues(false, false, true, false, false);
						
					}

					if (token.equalsIgnoreCase("registers:")) {
						setBooleanValues(false, true, false, false, false);	
						
					}
					
					if (token.equalsIgnoreCase("opcodeformat:")) {
						setBooleanValues(false, false, false, true, false);
						
					}
					
					if (token.equalsIgnoreCase("instructionformat:")) {
						setBooleanValues(false, false, false, false, true);						
					}
				}

				if (architecture)
					analyseArchitecture(tokens);			

				if (registers) 	
					analyseRegisters(tokens);				

				if (opcodes) 	
					analyseOpcodes(tokens);				
				
				if(opcodeFormat)
					analyseOpcodeFormat(tokens);
				
				if(instructionFormat)
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
			if(!token.equalsIgnoreCase("architecture:")){						
				data.getArchitecture().add(token);
			}						
		}		
	}
	
	public void analyseRegisters(String[] tokens){
		
		for (String token : tokens) {
			if(!token.equalsIgnoreCase("registers:")){						
				data.getRegisters().add(token);							
			}						
		}
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
		String[] opformat = new String[10];
		int i = 0;
		
		for(String token: tokens){
			if(!token.equalsIgnoreCase("opcodeformat:")){
				if(first){
					op = token;
					first = false;
				}
				else{						
					opformat[i] = token;
					i++;
				}
				done = true;
			}
			
		}
		if(done)
			data.getOpcodeFormat().put(op, opformat);
	}
	
	public void analyseInstructionFormat(String[] tokens){
		
		for (String token : tokens) {
			if(!token.equalsIgnoreCase("instructionformat:")){						
				data.getInstructionFormat().add(token);							
			}						
		}		
	}

	public DataSource getData() {
		return data;
	}

	public void setData(DataSource data) {
		this.data = data;
	}	
}
