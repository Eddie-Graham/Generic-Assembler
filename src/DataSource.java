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
	
	private ArrayList<String> architecture;
	private HashMap<String, MnemonicFormatData> mnemonicTable;
	private HashMap<String, InstructionFormatData> instructionFormat;	
	private HashMap<String, String> registerHash;
	private String immediateSyntax;
	private String registerSyntax;
	private String memorySyntax;
	
	public DataSource(){
		
		assemblyCode = new ArrayList<String>();
		
		architecture = new ArrayList<String>();
		mnemonicTable = new HashMap<String, MnemonicFormatData>();
		instructionFormat = new HashMap<String, InstructionFormatData>();		
		registerHash = new HashMap<String, String>();
		
	}

	public ArrayList<String> getAssemblyCode() {
		return assemblyCode;
	}

	public void setAssemblyCode(ArrayList<String> assemblyCode) {
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

	public String getImmediateSyntax() {
		return immediateSyntax;
	}

	public void setImmediateSyntax(String immediateSyntax) {
		this.immediateSyntax = immediateSyntax;
	}

	public String getRegisterSyntax() {
		return registerSyntax;
	}

	public void setRegisterSyntax(String registerSyntax) {
		this.registerSyntax = registerSyntax;
	}

	public String getMemorySyntax() {
		return memorySyntax;
	}

	public void setMemorySyntax(String memorySyntax) {
		this.memorySyntax = memorySyntax;
	}	
}
