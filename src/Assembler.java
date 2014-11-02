/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.sun.xml.internal.ws.util.StringUtils;

public class Assembler {

	DataSource data;
	ArrayList<String> objectCode;

	public Assembler(DataSource data) {

		this.data = data;
		objectCode = new ArrayList<String>();

		assemble();
	}

	public void assemble() {

		for (ArrayList<String> assemblyLine : data.getAssemblyCode()) {
			System.out
					.println("**************************************************************");

			populateInstruction(assemblyLine);
		}
	}

	private void populateInstruction(ArrayList<String> assemblyLine) {

		String mnemonic = assemblyLine.get(0); // get mnemonic
		MnemonicFormatData op = data.getMnemonicTable().get(mnemonic); 
		HashMap<String, String> assemblyOpFormatHash = makeAssemblyOpFormatHash(assemblyLine, op.getMnemonicFormat()); 

		String insName = op.getInstructionName(); 
		InstructionFormatData insF = data.getInstructionFormat().get(insName); 

		System.out.println(assemblyOpFormatHash);
		String binaryLine = "";

		for (String operand : insF.getOperands()) { // ins format operands

			if (op.getOpcodes().get(operand) != null) {
				String binaryOp = op.getOpcodes().get(operand);
				binaryLine += binaryOp + " ";
			} else { // registers??

				String reg = assemblyOpFormatHash.get(operand);
				String regHex = data.getRegisterHash().get(reg);
				int bits = insF.getOperandBitHash().get(operand);
				String binary = binaryFromHexFormatted(regHex, bits);

				binaryLine += binary + " ";
			}
		}
		System.out.println(binaryLine);
	}

	private HashMap<String, String> makeAssemblyOpFormatHash(ArrayList<String> assemblyLine, ArrayList<String> mnemonicFormat) {

		HashMap<String, String> assemblyOpFormatHash = new HashMap<String, String>();

		int i = 0;

		for (String assemblyTerm : assemblyLine) {
			String formatTerm = mnemonicFormat.get(i);

			String[] splitFormatTerms = formatTerm.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");
			String[] splitAssemblyTerm = assemblyTerm.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");
			
			int x = 0;
			
			for(String furtherFormatTerm: splitFormatTerms){
				String furtherAssemblyTerm = splitAssemblyTerm[x];
				
				if(furtherFormatTerm.matches("^.*[^a-zA-Z0-9].*$")){
					if(furtherFormatTerm.equals(furtherAssemblyTerm)){
						//correct
					}
						
				}
				else{
					assemblyOpFormatHash.put(furtherFormatTerm, furtherAssemblyTerm);
				}				
				x++;
			}
			i++;
		}
		return assemblyOpFormatHash;
	}

	public static String binaryFromHexFormatted(String hex, int bits) {

		if (hex.charAt(0) == '$') {
			hex = hex.substring(1);
		}

		String binary = decimalToBinary(hex);

		int initialLength = binary.length();
		int zerosNeeded = bits - initialLength;

		String zeros = "";

		for (; zerosNeeded > 0; zerosNeeded -= 1)
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

		if (hex.contains("x"))
			binary = Integer.toBinaryString(Integer.decode(hex));

		else {
			int i = Integer.parseInt(hex, 16);
			binary = Integer.toBinaryString(i);
		}
		return binary;
	}

	public static String binaryToHex(String binary) {

		Long l = Long.parseLong(binary, 2);
		String hex = String.format("%X", l);
		return hex;
	}

	public static String decimalToBinary(String decimal) {

		int decimalInt = Integer.parseInt(decimal);
		String binary = Integer.toBinaryString(decimalInt);

		return binary;
	}
}
