/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class Assembler {
	
	DataSource data;
	ArrayList<String> objectCode;
	
	public Assembler(DataSource data){
		
		this.data = data;
		objectCode = new ArrayList<String>();
		assemble();
	}
	
	public void assemble(){
		
		for(ArrayList<String> assemblyLine: data.getAssemblyCode()){
			System.out.println("**************************************************************");
			System.out.println(assemblyLine);
			String mnemonic = assemblyLine.get(0);
			OpcodeFormat op = data.getOpcodeFormats().get(mnemonic);
			System.out.println(op.getOpFormat());
			String opLabel = op.getLabel();
			ArrayList<String> insF = data.getInstructionFormat().get(opLabel);
			System.out.println(insF);
			HashMap<String,String> cond = op.getOpConditions();
			System.out.println(cond);
			System.out.println("**************************************************************");
		}		
	}
	
	public static String hexToBinary(String hex) {
		
		String binary = "";
		
		if(hex.contains("x"))
			binary = Integer.toBinaryString(Integer.decode(hex));
		
		else{
			int i = Integer.parseInt(hex, 16);
			binary = Integer.toBinaryString(i);
		}
		return binary;
	}
	
	public static String binaryToHex(String binary) {
		
		Long l = Long.parseLong(binary,2);
		String hex = String.format("%X", l) ;
		return hex;
	}
}
