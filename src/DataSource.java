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
	HashMap<String, OpcodeFormat> opcodeFormats;
	HashMap<String, ArrayList<String>> instructionFormat;	
	HashMap<String, String> registerHash;
	
	public DataSource(){
		
		assemblyCode = new ArrayList<ArrayList<String>>();
		
		architecture = new ArrayList<String>();
		opcodeFormats = new HashMap<String, OpcodeFormat>();
		instructionFormat = new HashMap<String, ArrayList<String>>();		
		registerHash = new HashMap<String, String>();
	}

	public ArrayList<ArrayList<String>> getAssemblyCode() {
		return assemblyCode;
	}

	public ArrayList<String> getArchitecture() {
		return architecture;
	}

	public HashMap<String, OpcodeFormat> getOpcodeFormats() {
		return opcodeFormats;
	}

	public HashMap<String, ArrayList<String>> getInstructionFormat() {
		return instructionFormat;
	}

	public HashMap<String, String> getRegisterHash() {
		return registerHash;
	}
}
