/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Assembler {

	private DataSource data;
	private ArrayList<String> objectCode;

	public Assembler(DataSource data) {

		this.data = data;
		objectCode = new ArrayList<String>();

		assemble();
	}

	public void assemble() {

		for (String assemblyLine : data.getAssemblyCode()) {
			System.out.println("**************************************************************");

			populateInstruction(assemblyLine);
		}
	}

	private void populateInstruction(String assemblyLine) {

		String[] tokens = assemblyLine.split("\\s+");
		String mnemonic = tokens[0];

		MnemonicFormatData op = data.getMnemonicTable().get(mnemonic);

		HashMap<String, String> assemblyOpFormatHash = makeAssemblyOpFormatHash(assemblyLine, op.getMnemonicFormat());

		System.out.println(assemblyOpFormatHash);
	
		String insName = op.getInstructionName();
		InstructionFormatData insF = data.getInstructionFormat().get(insName);

		String binaryLine = "";

		for (String operand : insF.getOperands()) { // ins format operands

			if (op.getOpcodes().get(operand) != null) {
				String binaryOp = op.getOpcodes().get(operand);
				binaryLine += binaryOp + " ";
			} else { // registers??

				String reg = assemblyOpFormatHash.get(operand);
				String regHex = data.getRegisterHash().get(reg);
				int bits = insF.getOperandBitHash().get(operand);
				String binary = binaryFromBinaryFormatted(regHex, bits);

				binaryLine += binary + " ";
			}
		}
		System.out.println(binaryLine);
	}

	private HashMap<String, String> makeAssemblyOpFormatHash(String assemblyLine, String mnemonicFormat) {

		HashMap<String, String> assemblyOpFormatHash = new HashMap<String, String>();
		
		mnemonicFormat = mnemonicFormat.replaceAll("\\s+", " ");
		assemblyLine = assemblyLine.replaceAll("\\s+", " ");
		
		String[] splitFormatTerms = mnemonicFormat.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");
		String[] splitAssemblyTerms = assemblyLine.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");
				
		int i = 0;

		for (String term : splitFormatTerms) {
			if (!isAlphaNumeric(term)) {
				// correct
			} 
			else {
				String assemblyTerm = splitAssemblyTerms[i];
				assemblyOpFormatHash.put(term, assemblyTerm);
			}
			i++;
		}		
		return assemblyOpFormatHash;
	}

	private String getRegex(ArrayList<String> delimiters){
		
		String firstPart = "(?=[\\";
		String secondPart = "|(?<=[\\";
		
		boolean first = true;
		
		for(String delimiter: delimiters){
			if(!first){
				firstPart += "|\\";
				secondPart += "|\\";
			}			
		
			firstPart += delimiter;
			secondPart += delimiter;
			first = false;
		}
		
		firstPart += "])";
		secondPart += "])";
		
		String regex = firstPart + secondPart;
		
		return regex;
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

	private boolean isAlphaNumeric(String s) {
		String pattern = "^[a-zA-Z0-9]*$";
		if (s.matches(pattern)) {
			return true;
		}
		return false;
	}
}
