import java.util.ArrayList;


public class DataSource {
	
	ArrayList<String> assemblyCode;
	
	String architectureName;
	ArrayList<String> opcodes;
	ArrayList<String> regsters;
	
	
	public DataSource(){
		
		assemblyCode = new ArrayList<String>();
		opcodes = new ArrayList<String>();
		regsters = new ArrayList<String>();
	}

	public ArrayList<String> getAssemblyCode() {
		return assemblyCode;
	}

	public void setAssemblyCode(ArrayList<String> assemblyCode) {
		this.assemblyCode = assemblyCode;
	}

	public String getArchitectureName() {
		return architectureName;
	}

	public void setArchitectureName(String architectureName) {
		this.architectureName = architectureName;
	}

	public ArrayList<String> getOpcodes() {
		return opcodes;
	}

	public void setOpcodes(ArrayList<String> opcodes) {
		this.opcodes = opcodes;
	}

	public ArrayList<String> getRegsters() {
		return regsters;
	}

	public void setRegsters(ArrayList<String> regsters) {
		this.regsters = regsters;
	}	

}
