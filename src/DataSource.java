import java.util.ArrayList;


public class DataSource {
	
	ArrayList<String> assemblyCode;
	ArrayList<String> architecture;
	
	public DataSource(){
		
		assemblyCode = new ArrayList<String>();
		architecture = new ArrayList<String>();
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
	
	

}
