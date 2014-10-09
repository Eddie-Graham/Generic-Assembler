import java.util.ArrayList;

public class Assembler {
	
	DataSource data;
	ArrayList<String> objectCode;
	
	public Assembler(DataSource data){
		
		this.data = data;
		objectCode = new ArrayList<String>();
	}
	
	public void assemble(){
		
		
		
	}
	
	static String hexToBinary(String hex) {

		int i = Integer.parseInt(hex, 16);
		String binary = Integer.toBinaryString(i);
		return binary;
	}

}
