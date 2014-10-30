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
	HashMap<String, String> opFormatTermsHash;
	
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
		
		String mnemonic = assemblyLine.get(0);	//get mnemonic
		OpcodeFormatData op = data.getOpcodeFormats().get(mnemonic);	// get format for operand		
		makeOpFormatHash(assemblyLine, op.getOpFormat());	// make operand hash with values
		
		String insName = op.getInstructionName();	// get name of instruction type		
		InstructionFormatData insF = data.getInstructionFormat().get(insName);	// get ins format data

		String binaryLine = "";
		
		for(String operand: insF.getOperands()){	// ins format operands
			
			if(op.getOpConditions().get(operand) != null){
				String binaryOp = op.getOpConditions().get(operand);
				binaryLine += binaryOp + " ";
			}
			else{	//registers??
				
				String reg = opFormatTermsHash.get(operand);
				String regHex = data.getRegisterHash().get(reg);
				int bits = insF.getOpFormatBitHash().get(operand);
				String binary = binaryFromHexFormatted(regHex, bits);
				
				binaryLine += binary + " ";
			}			
		}
		System.out.println(binaryLine);		
	}

	private void makeOpFormatHash(ArrayList<String> assemblyLine, ArrayList<String> opFormat) {
		
		opFormatTermsHash = new HashMap<String, String>();
		
		int i = 0;
		
		for(String assemblyTerm: assemblyLine){
			String formatTerm = opFormat.get(i);
			opFormatTermsHash.put(formatTerm, assemblyTerm);
			i++;
		}		
	}
	
	public static String binaryFromHexFormatted(String hex, int bits){
		
		if(hex.charAt(0) == '$'){
			hex = hex.substring(1);
		}
		
		String binary = hexToBinary(hex);
		
		int initialLength = binary.length();
		int zerosNeeded = bits - initialLength;
		
		String zeros = "";
		
		for(;zerosNeeded>0;zerosNeeded -= 1)
			zeros += "0";		
		
		String finalString = zeros + binary;
		
		return finalString;
	}
	
	public static String binaryFromBinaryFormatted(String binary, int bits) {

		int initialLength = binary.length();
		int zerosNeeded = bits - initialLength;

		String zeros = "";

		for (; zerosNeeded > 0; zerosNeeded -= 1)
			zeros += "0";

		String finalString = zeros + binary;

		return finalString;
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
	
	public static String decimalToBinary(String decimal){
	       
		int decimalInt = Integer.parseInt(decimal);
		String binary = Integer.toBinaryString(decimalInt);

		return binary;
	   }
}
