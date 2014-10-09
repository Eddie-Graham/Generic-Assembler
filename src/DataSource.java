import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;


public class DataSource {
	
	LinkedHashMap<String, ArrayList<String>> assemblyCode;
	
	ArrayList<String> architecture;
	HashMap<String,String> opcodes;
	ArrayList<String> registers;
	HashMap<String, ArrayList<String>> opcodeFormat;
	ArrayList<String> instructionFormat;
	
	public DataSource(){
		
		assemblyCode = new LinkedHashMap<String, ArrayList<String>>();
		
		architecture = new ArrayList<String>();
		opcodes = new HashMap<String, String>();
		registers = new ArrayList<String>();
		opcodeFormat = new HashMap<String, ArrayList<String>>();
		instructionFormat = new ArrayList<String>();
	}

	public LinkedHashMap<String, ArrayList<String>> getAssemblyCode() {
		return assemblyCode;
	}

	public void setAssemblyCode(
			LinkedHashMap<String, ArrayList<String>> assemblyCode) {
		this.assemblyCode = assemblyCode;
	}



	public ArrayList<String> getArchitecture() {
		return architecture;
	}

	public void setArchitecture(ArrayList<String> architecture) {
		this.architecture = architecture;
	}

	public HashMap<String, String> getOpcodes() {
		return opcodes;
	}

	public void setOpcodes(HashMap<String, String> opcodes) {
		this.opcodes = opcodes;
	}

	public ArrayList<String> getRegisters() {
		return registers;
	}

	public void setRegisters(ArrayList<String> registers) {
		this.registers = registers;
	}

	public HashMap<String, ArrayList<String>> getOpcodeFormat() {
		return opcodeFormat;
	}

	public void setOpcodeFormat(HashMap<String, ArrayList<String>> opcodeFormat) {
		this.opcodeFormat = opcodeFormat;
	}

	public ArrayList<String> getInstructionFormat() {
		return instructionFormat;
	}

	public void setInstructionFormat(ArrayList<String> instructionFormat) {
		this.instructionFormat = instructionFormat;
	}	
}
