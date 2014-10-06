import java.util.ArrayList;
import java.util.HashMap;


public class DataSource {
	
	ArrayList<String> assemblyCode;
	
	ArrayList<String> architecture;
	HashMap<String,String> opcodes;
	ArrayList<String> regsters;
	
	
	public DataSource(){
		
		assemblyCode = new ArrayList<String>();
		architecture = new ArrayList<String>();
		opcodes = new HashMap<String, String>();
		regsters = new ArrayList<String>();
	}

	public ArrayList<String> getAssemblyCode() {
		return assemblyCode;
	}

	public void setAssemblyCode(ArrayList<String> assemblyCode) {
		this.assemblyCode = assemblyCode;
	}

	public ArrayList<String> getRegsters() {
		return regsters;
	}

	public void setRegsters(ArrayList<String> regsters) {
		this.regsters = regsters;
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

}
