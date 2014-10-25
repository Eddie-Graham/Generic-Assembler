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
	HashMap<String, String> opFormatHash;
	
	public Assembler(DataSource data){
		
		this.data = data;
		objectCode = new ArrayList<String>();
		
		assemble();
	}
	
	public void assemble(){
		
		for(ArrayList<String> assemblyLine: data.getAssemblyCode()){
			System.out.println("**************************************************************");
			
			populateInstruction(assemblyLine);
		}		
	}
	
	private void populateInstruction(ArrayList<String> assemblyLine) {
		
		String mnemonic = assemblyLine.get(0);
		OpcodeFormatData op = data.getOpcodeFormats().get(mnemonic);		
		makeOpFormatHash(assemblyLine, op.getOpFormat());
		System.out.println(opFormatHash);
		
		String insName = op.getInstructionName();
		InstructionFormatData insF = data.getInstructionFormat().get(insName);
		
		
	}

	private void makeOpFormatHash(ArrayList<String> assemblyLine, ArrayList<String> opFormat) {
		
		opFormatHash = new HashMap<String, String>();
		
		int i = 0;
		
		for(String assemblyTerm: assemblyLine){
			String formatTerm = opFormat.get(i);
			opFormatHash.put(formatTerm, assemblyTerm);
			i++;
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
