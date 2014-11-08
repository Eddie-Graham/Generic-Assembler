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

	private void assemble() {

		for (String assemblyLine : data.getAssemblyCode()) {
			System.out.println("**************************************************************");

			populateInstruction(assemblyLine);
		}
	}

	private void populateInstruction(String assemblyLine) {
		
		System.out.println(assemblyLine);

		String[] tokens = assemblyLine.split("\\s+");
		String mnemonic = tokens[0];

		MnemonicFormatData op = data.getMnemonicTable().get(mnemonic);

		HashMap<String, String> assemblyOpFormatHash = makeAssemblyOpFormatHash(assemblyLine, op.getMnemonicFormat());

		System.out.println(assemblyOpFormatHash);
	
		String insName = op.getInstructionName();
		InstructionFormatData insF = data.getInstructionFormat().get(insName);

		String binaryLine = "";

		for (String formatOperand : insF.getOperands()) { // ins format operands
			
			DataSource.OperandType opType = null;

			if (op.getOpcodes().get(formatOperand) != null) {	// an opcode
				String binaryOp = op.getOpcodes().get(formatOperand);
				binaryLine += binaryOp + " ";
			} 
			else { // register, immediate or memory???

				String assemblyOperand = assemblyOpFormatHash.get(formatOperand);
				opType = getOpType(assemblyOperand);
				int bits = insF.getOperandBitHash().get(formatOperand);
				
				if(opType == DataSource.OperandType.REGISTER){
					String regOperand = removePrefix(assemblyOperand);				
					String regBinary = data.getRegisterHash().get(regOperand);					
					String binary = binaryFromBinaryFormatted(regBinary, bits);
					
					binaryLine += binary + " ";
					
				}
				else if(opType == DataSource.OperandType.MEMORY) {
					String memOperand = removePrefix(assemblyOperand);								
					String binary = getBinaryFromNumSys(memOperand, opType, bits);
					
					binaryLine += binary + " ";
				}
				else if(opType == DataSource.OperandType.IMMEDIATE) {
					String immOperand = removePrefix(assemblyOperand);				
					String binary = getBinaryFromNumSys(immOperand, opType, bits);
					
					binaryLine += binary + " ";
				}
				else{
					// error??
				}
			}
		}
		System.out.println(binaryLine);
	}
	
	private String getBinaryFromNumSys(String value, DataSource.OperandType opType, int bits) {
		
		String binary = "";
		
		if(opType.getSys() == DataSource.TypeNumSystem.DECIMAL){
			binary = binaryFromDecimalFormatted(value, bits);
		}
		else if(opType.getSys() == DataSource.TypeNumSystem.HEX){
			binary = binaryFromHexFormatted(value, bits);
		}
		return binary;
	}

	private String removePrefix(String operand) {

		ArrayList<String> prefixes = data.getPrefixes();

		for (String prefix : prefixes) {
			if (operand.startsWith(prefix)) {
				
				if(!isAlphaNumeric(prefix)){
					operand = operand.replaceFirst("\\"+prefix, "");	// what if more than single char prefix??
					break;
				}
				else{
					operand = operand.replaceFirst(prefix, "");
				}
			}
		}		
		return operand;
	}

	private DataSource.OperandType getOpType(String assemblyOperand) {
		
		DataSource.OperandType opType = null;
		
		ArrayList<String> prefixes = data.getPrefixes();
		
		for(String prefix: prefixes){
			
			if(assemblyOperand.startsWith(prefix)){
				opType = data.getPrefixTypeHash().get(prefix);
				break;
			}
		}
		return opType;
	}

	private HashMap<String, String> makeAssemblyOpFormatHash(String assemblyLine, String mnemonicFormat) {

		HashMap<String, String> assemblyOpFormatHash = new HashMap<String, String>();
		
		mnemonicFormat = mnemonicFormat.replaceAll("\\s+", " ");
		assemblyLine = assemblyLine.replaceAll("\\s+", " ");
		
		ArrayList<String> prefixes = data.getPrefixes();
		String prefixRegex = "";
		
		for(String prefix: prefixes){
			if(!isAlphaNumeric(prefix))
				prefixRegex += prefix;
		}
		
		mnemonicFormat = mnemonicFormat.trim();	// remove leading and trailing whitespace
		assemblyLine = assemblyLine.trim();
		
		String[] splitFormatTerms = mnemonicFormat.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");
		String[] splitAssemblyTerms = assemblyLine.split("(?=[^a-zA-Z0-9" + prefixRegex + "])|(?<=[^a-zA-Z0-9" + prefixRegex + "])");
			
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
	
	public static String binaryFromDecimalFormatted(String decimal, int bits) {

		String binary = decimalToBinary(decimal);

		int initialLength = binary.length();
		int zerosNeeded = bits - initialLength;

		String zeros = "";

		for (; zerosNeeded > 0; zerosNeeded -= 1)
			zeros += "0";

		String finalString = zeros + binary;

		return finalString;
	}

	public static String binaryFromHexFormatted(String hex, int bits) {

		String binary = hexToBinary(hex);

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
