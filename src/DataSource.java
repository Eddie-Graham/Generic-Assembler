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
	
	private String architecture;
	private HashMap<String, Mnemonic> mnemonicTable;
	private HashMap<String, InstructionFormat> instructionFormatHash;	
	private HashMap<String, String> registerHash;
	private AssemblyOpTree assemblyOpTree;
	private String endian;
	private int minAdrUnit;
	
	
	public DataSource(){
		
		assemblyCode = new ArrayList<String>();	
		
		architecture = null;
		mnemonicTable = new HashMap<String, Mnemonic>();
		instructionFormatHash = new HashMap<String, InstructionFormat>();		
		registerHash = new HashMap<String, String>();
		assemblyOpTree = new AssemblyOpTree();
		endian = null;
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


	public HashMap<String, Mnemonic> getMnemonicTable() {
		return mnemonicTable;
	}


	public void setMnemonicTable(HashMap<String, Mnemonic> mnemonicTable) {
		this.mnemonicTable = mnemonicTable;
	}


	public HashMap<String, InstructionFormat> getInstructionFormatHash() {
		return instructionFormatHash;
	}


	public void setInstructionFormatHash(
			HashMap<String, InstructionFormat> instructionFormatHash) {
		this.instructionFormatHash = instructionFormatHash;
	}


	public HashMap<String, String> getRegisterHash() {
		return registerHash;
	}


	public void setRegisterHash(HashMap<String, String> registerHash) {
		this.registerHash = registerHash;
	}


	public AssemblyOpTree getAssemblyOpTree() {
		return assemblyOpTree;
	}


	public void setAssemblyOpTree(AssemblyOpTree assemblyOpTree) {
		this.assemblyOpTree = assemblyOpTree;
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
}
