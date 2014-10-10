/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

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
