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
	HashMap<String, OpcodeFormatData> opcodeFormats;
	HashMap<String, InstructionFormatData> instructionFormat;	
	HashMap<String, String> registerHash;
	
	public DataSource(){
		
		assemblyCode = new ArrayList<ArrayList<String>>();
		
		architecture = new ArrayList<String>();
		opcodeFormats = new HashMap<String, OpcodeFormatData>();
		instructionFormat = new HashMap<String, InstructionFormatData>();		
		registerHash = new HashMap<String, String>();
	}

	public ArrayList<ArrayList<String>> getAssemblyCode() {
		return assemblyCode;
	}

	public ArrayList<String> getArchitecture() {
		return architecture;
	}

	public HashMap<String, OpcodeFormatData> getOpcodeFormats() {
		return opcodeFormats;
	}

	public HashMap<String, InstructionFormatData> getInstructionFormat() {
		return instructionFormat;
	}

	public HashMap<String, String> getRegisterHash() {
		return registerHash;
	}	
}
