/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class DataSource {
	
	ArrayList<ArrayList<String>> assemblyCode;
	
	ArrayList<String> architecture;
	HashMap<String, MnemonicFormatData> mnemonicTable;
	HashMap<String, InstructionFormatData> instructionFormat;	
	HashMap<String, String> registerHash;
	
	public DataSource(){
		
		assemblyCode = new ArrayList<ArrayList<String>>();
		
		architecture = new ArrayList<String>();
		mnemonicTable = new HashMap<String, MnemonicFormatData>();
		instructionFormat = new HashMap<String, InstructionFormatData>();		
		registerHash = new HashMap<String, String>();
	}

	public ArrayList<ArrayList<String>> getAssemblyCode() {
		return assemblyCode;
	}

	public void setAssemblyCode(ArrayList<ArrayList<String>> assemblyCode) {
		this.assemblyCode = assemblyCode;
	}

	public ArrayList<String> getArchitecture() {
		return architecture;
	}

	public void setArchitecture(ArrayList<String> architecture) {
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
}
