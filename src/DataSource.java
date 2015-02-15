/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class DataSource {	
	
	private ArrayList<String> assemblyCode;	
	
	private boolean errorInSpecFile;
	
	private String architecture;
	private HashMap<String, MnemonicData> mnemonicTable;
	private HashMap<String, InstructionFormatData> instructionFormat;	
	private HashMap<String, String> registerHash;
	private ADT adt;
	private String endian;
	private int minAdrUnit;
	
	
	public DataSource(){
		
		assemblyCode = new ArrayList<String>();	
		
		errorInSpecFile = false;
		
		architecture = "";
		mnemonicTable = new HashMap<String, MnemonicData>();
		instructionFormat = new HashMap<String, InstructionFormatData>();		
		registerHash = new HashMap<String, String>();
		adt = new ADT();
		endian = "";
	}

	public ArrayList<String> getAssemblyCode() {
		return assemblyCode;
	}

	public void setAssemblyCode(ArrayList<String> assemblyCode) {
		this.assemblyCode = assemblyCode;
	}

	public String getArchitecture() {
		return architecture;
	}

	public void setArchitecture(String architecture) {
		this.architecture = architecture;
	}

	public HashMap<String, MnemonicData> getMnemonicTable() {
		return mnemonicTable;
	}

	public void setMnemonicTable(HashMap<String, MnemonicData> mnemonicTable) {
		this.mnemonicTable = mnemonicTable;
	}

	public HashMap<String, InstructionFormatData> getInstructionFormat() {
		return instructionFormat;
	}

	public void setInstructionFormat(
			HashMap<String, InstructionFormatData> instructionFormat) {
		this.instructionFormat = instructionFormat;
	}

	public HashMap<String, String> getRegisterHash() {
		return registerHash;
	}

	public void setRegisterHash(HashMap<String, String> registerHash) {
		this.registerHash = registerHash;
	}

	public ADT getAdt() {
		return adt;
	}

	public void setAdt(ADT adt) {
		this.adt = adt;
	}

	public String getEndian() {
		return endian;
	}

	public void setEndian(String endian) {
		this.endian = endian;
	}

	public int getMinAdrUnit() {
		return minAdrUnit;
	}

	public void setMinAdrUnit(int minAdrUnit) {
		this.minAdrUnit = minAdrUnit;
	}

	public boolean isErrorInSpecFile() {
		return errorInSpecFile;
	}

	public void setErrorInSpecFile(boolean errorInSpecFile) {
		this.errorInSpecFile = errorInSpecFile;
	}		
}
