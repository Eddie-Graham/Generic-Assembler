/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class DataSource {
	
	public static enum OperandType {
	    IMMEDIATE, REGISTER, MEMORY;
	    
	    public void setType(TypeNumSystem sys) {
	        this.sys = sys;
	      }
	    
	    public TypeNumSystem getSys(){
	    	return sys;
	    }	    
	    
	    public TypeNumSystem sys;
	}	
	
	public static enum TypeNumSystem{
		DECIMAL, HEX
	}
	
	
	private ArrayList<String> assemblyCode;	
	
	private String architecture;
	private HashMap<String, MnemonicFormatData> mnemonicTable;
	private HashMap<String, InstructionFormatData> instructionFormat;	
	private HashMap<String, String> registerHash;
	private HashMap<String, OperandType> prefixTypeHash;	
	private ArrayList<String> prefixes;
	
	public DataSource(){
		
		assemblyCode = new ArrayList<String>();	
		
		architecture = "";
		mnemonicTable = new HashMap<String, MnemonicFormatData>();
		instructionFormat = new HashMap<String, InstructionFormatData>();		
		registerHash = new HashMap<String, String>();
		prefixTypeHash = new HashMap<String, OperandType>();		
		prefixes = new ArrayList<String>();
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

	public HashMap<String, MnemonicFormatData> getMnemonicTable() {
		return mnemonicTable;
	}

	public void setMnemonicTable(HashMap<String, MnemonicFormatData> mnemonicTable) {
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

	public HashMap<String, OperandType> getPrefixTypeHash() {
		return prefixTypeHash;
	}

	public void setPrefixTypeHash(HashMap<String, OperandType> prefixTypeHash) {
		this.prefixTypeHash = prefixTypeHash;
	}

	public ArrayList<String> getPrefixes() {
		return prefixes;
	}

	public void setPrefixes(ArrayList<String> prefixes) {
		this.prefixes = prefixes;
	}	
}
