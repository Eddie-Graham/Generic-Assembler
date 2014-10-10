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
	HashMap<String,String> opcodes;
	HashMap<String, ArrayList<String>> opcodeFormat;
	ArrayList<String> instructionFormat;	
	HashMap<String, String> registerHash;
	
	public DataSource(){
		
		assemblyCode = new ArrayList<ArrayList<String>>();
		
		architecture = new ArrayList<String>();
		opcodes = new HashMap<String, String>();
		opcodeFormat = new HashMap<String, ArrayList<String>>();
		instructionFormat = new ArrayList<String>();		
		registerHash = new HashMap<String, String>();
	}

	public ArrayList<ArrayList<String>> getAssemblyCode() {
		return assemblyCode;
	}

	public ArrayList<String> getArchitecture() {
		return architecture;
	}

	public HashMap<String, String> getOpcodes() {
		return opcodes;
	}

	public HashMap<String, ArrayList<String>> getOpcodeFormat() {
		return opcodeFormat;
	}

	public ArrayList<String> getInstructionFormat() {
		return instructionFormat;
	}

	public HashMap<String, String> getRegisterHash() {
		return registerHash;
	}	
}
