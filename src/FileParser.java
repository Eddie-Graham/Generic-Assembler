import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class FileParser {
	
	DataSource data;
	
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
		boolean architecture = false, opcodes = false, registers = false;

		while (inputFile.hasNextLine()) {
			line = inputFile.nextLine();

			if (!line.isEmpty()) {				
				tokens = line.split("\\s+");

				for (String token : tokens) {

					if (token.equals("architecture:")) {
						architecture = true;
						opcodes = false;
						registers = false;
						break;
					}

					if (token.equals("opcodes:")) {
						architecture = false;
						opcodes = true;
						registers = false;
						break;
					}

					if (token.equals("registers:")) {
						architecture = false;
						opcodes = false;
						registers = true;
						break;
					}

				}

				if (architecture) {
					for (String token : tokens) {
						if(!token.equals("architecture:")){						
							data.getArchitecture().add(token);
						}						
					}
				}

				if (registers) {		
					for (String token : tokens) {
						if(!token.equals("registers:")){						
							data.getRegsters().add(token);							
						}						
					}
				}

				if (opcodes) {					
					String op = null;
					String code = null;
					boolean first = true;
					
					for (String token : tokens) {
						
						if(first){
							op = token;
							first = false;
						}
						if(!token.equals("opcodes:") && !first){
							code = token;
							
							data.getOpcodes().put(op, code);
						}
					}
				}
			}
		}		
			
//			if(string.equals("architecture:")){			
//				string = inputFile.next();
//				
//				while(!string.equals("opcodes:") && !string.equals("registers:")){
//					data.getArchitecture().add(string);
//					
//					if(!inputFile.hasNext())
//						return;
//						
//					string = inputFile.next();
//				}
//			}
//			
//			if(string.equals("opcodes:")){				
//				string = inputFile.next();
//				String nextString = null;
//				
//				while(!string.equals("registers:") && !string.equals("architecture:")){
//					nextString = inputFile.next();
//					data.getOpcodes().put(string, nextString);
//					
//					if(!inputFile.hasNext())
//						return;
//						
//					string = inputFile.next();
//				}
//			}
//			
//			if(string.equals("registers:")){
//				string = inputFile.next();
//				
//				while(!string.equals("architecture:") && !string.equals("opcodes:")){
//					data.getRegsters().add(string);
//					
//					if(!inputFile.hasNext())
//						return;
//						
//					string = inputFile.next();
//				}
//			}	
//		}	
		
		inputFile.close();
	}

	public DataSource getData() {
		return data;
	}

	public void setData(DataSource data) {
		this.data = data;
	}		

}
