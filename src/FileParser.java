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
		}

		while (inputFile.hasNext()) {
			data.getArchitecture().add(inputFile.next());
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
