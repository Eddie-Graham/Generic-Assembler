import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

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
		
		String string = inputFile.next();

		while (inputFile.hasNext()) {
			
			if(string.equals("architecture:")){
				data.setArchitectureName(inputFile.next());
				
				if(!inputFile.hasNext())
					return;
					
				string = inputFile.next();
			}
			
			if(string.equals("opcodes:")){				
				string = inputFile.next();
				
				while(!string.equals("registers:") && !string.equals("architecture:")){
					data.getOpcodes().add(string);
					
					if(!inputFile.hasNext())
						return;
						
					string = inputFile.next();
				}
			}
			
			if(string.equals("registers:")){
				string = inputFile.next();
				
				while(!string.equals("architecture:") && !string.equals("opcodes:")){
					data.getRegsters().add(string);
					
					if(!inputFile.hasNext())
						return;
						
					string = inputFile.next();
				}
			}	
		}		
		inputFile.close();
	}

	public DataSource getData() {
		return data;
	}

	public void setData(DataSource data) {
		this.data = data;
	}		

}
