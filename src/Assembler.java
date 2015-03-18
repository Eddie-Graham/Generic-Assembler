/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * <pre>
 * This class assembles each instruction using the data source.
 * </pre>
 * 
 * @author Eddie Graham
 * 
 */
public class Assembler {

	private DataSource data;

	private int locationCounter;
	private int insNumber;
	private HashMap<Integer, Integer> insAdrTable;
	private HashMap<String, Integer> symbolTable;
	private HashMap<String, Integer> dataTable;

	private ArrayList<ArrayList<String>> legitAssemblyOpTreePaths;
	private HashMap<String, String> assemblyTermTypeHash;

	private boolean atData, atText;
	private boolean dataDeclared, textDeclared;

	private boolean debug = false;

	private ArrayList<String> objectCode;

	/**
	 * <pre>
	 * Constructor for class, initialises variables and calls assemble() method.
	 * </pre>
	 * 
	 * @param data - Data for assembler to work on.
	 */
	public Assembler(DataSource data) {

		this.data = data;

		locationCounter = 0;
		insNumber = 0;
		insAdrTable = new HashMap<Integer, Integer>();
		symbolTable = new HashMap<String, Integer>();
		dataTable = new HashMap<String, Integer>();

		legitAssemblyOpTreePaths = new ArrayList<ArrayList<String>>();
		assemblyTermTypeHash = new HashMap<String, String>();

		atData = false;
		atText = false;
		dataDeclared = false;
		textDeclared = false;

		objectCode = new ArrayList<String>();

		assemble();
	}

	private void assemble() {

		firstPass();

		insNumber++;
		insAdrTable.put(insNumber, locationCounter); // to account for last line

		insNumber = 0;

		secondPass();

		writeLinesToFile("object_code.txt", objectCode);
	}

	private void firstPass() {

		int lineCounter = 0;

		for (String assemblyLine : data.getAssemblyCode()) {

			lineCounter++;

			String[] commentSplit = assemblyLine.split(";");
			
			try {
				
				assemblyLine = commentSplit[0];
				
			} catch (ArrayIndexOutOfBoundsException e) {
				
				assemblyLine = "";
			}
			
			assemblyLine = assemblyLine.trim();

			if (assemblyLine.length() > 0) {

				try {
					
					analyseLineFirstPass(assemblyLine);
					
				} catch (AssemblerException e) {
					
					String error = getErrorMessage(lineCounter, assemblyLine, e.getMessage());
					objectCode.add(error);
					writeLinesToFile("object_code.txt", objectCode);
					System.exit(0);
				}
			}
		}
	}
	
	private void analyseLineFirstPass(String assemblyLine) throws AssemblerException {


		if (assemblyLine.startsWith(".data")){
			
			if(dataDeclared)
				throw new AssemblerException(".data section already declared.");
			
			dataDeclared = true;
			
			atData = true;
			atText = false;
		}

		else if (assemblyLine.startsWith(".text")) {
			
			if(textDeclared)
				throw new AssemblerException(".text section already declared.");
			
			textDeclared = true;
			
			atData=false;
			atText=true;
		}

		else if (atData) 
			analyseDataFirstPass(assemblyLine);

		else if (atText) 
			analyseInstructionsFirstPass(assemblyLine);
		
		else
			throw new AssemblerException("No section header (\".data\" or \".text\").");
		
	}

	private void analyseDataFirstPass(String assemblyLine) throws AssemblerException {

		boolean legitIntDataLine = Pattern.matches(
				"[A-Za-z0-9]+\\s+[0-9]+MAU\\s+[^\\s]+", assemblyLine);

		boolean legitAsciiDataLine = Pattern.matches(
				"[A-Za-z0-9]+\\s+.ascii\\s+\".+\"", assemblyLine);

		boolean legitUninitializedDataLine = Pattern.matches(
				"[A-Za-z0-9]+\\s+[0-9]+MAU", assemblyLine);

		if (!legitIntDataLine && !legitAsciiDataLine && !legitUninitializedDataLine)
			throw new AssemblerException(".data line incorrect syntax.");
		
		String[] splitDataLine = assemblyLine.split("\\s+");
		String label = splitDataLine[0];

		int noOfMinAdrUnits = 0;

		if (legitAsciiDataLine) {

			String[] splitByQuotation = assemblyLine.split("\"", 2);

			String asciiData = splitByQuotation[1].substring(0,	splitByQuotation[1].length() - 1);

			int noOfBits = 0;

			for (int i = 0; i < asciiData.length(); i++)
				noOfBits += 8;

			int minAdrUnit = data.getMinAdrUnit();
			noOfMinAdrUnits = noOfBits / minAdrUnit;
		}

		else if (legitIntDataLine || legitUninitializedDataLine) {

			String[] splitDataTerm = assemblyLine.split("\\s+");

			String minUnitTerm = splitDataTerm[1];
			String[] splitMinUnitTerm = minUnitTerm.split("MAU");
			String noOfMinAdrUnitsStr = splitMinUnitTerm[0];
			noOfMinAdrUnits = Integer.parseInt(noOfMinAdrUnitsStr);
		}

		if (symbolTable.get(label) == null && dataTable.get(label) == null)
			dataTable.put(label, locationCounter);

		else
			throw new AssemblerException("\"" + label
					+ "\" already exists in symbol table.");

		insNumber++;
		insAdrTable.put(insNumber, locationCounter);

		locationCounter += noOfMinAdrUnits;
	}

	private void analyseInstructionsFirstPass(String assemblyLine) throws AssemblerException {

		legitAssemblyOpTreePaths = new ArrayList<ArrayList<String>>();

		analyseWithAssemblyOpTree(assemblyLine);
		
		System.out.println(legitAssemblyOpTreePaths);

		if (legitAssemblyOpTreePaths.isEmpty())
			throw new AssemblerException("Assembly line not consistent with assemblyOpTree. Please check tree.");

		MnemonicData mnemData = getMnemData(assemblyLine);

		if (mnemData == null)
			throw new AssemblerException("Mnemonic not declared in MnemonicData section within specification file.");

		ArrayList<String> operandFormats = mnemData.getMnemFormats();

		ArrayList<String> legitOpFormats = new ArrayList<String>();

		for (String opFormat : operandFormats) {

			if (formatMatch(opFormat))
				legitOpFormats.add(opFormat);
		}

		if (legitOpFormats.isEmpty()) {

			String error = "Incorrectly formatted operands. Expected formats for mnemonic \""+ mnemData.getMnemonic()+"\":\n";

			for (String opFormat : operandFormats)
				error += "\n" + opFormat;
			
			error += "\n\nOperand tree built from assembly line:\n\n" + legitAssemblyOpTreePaths;

			throw new AssemblerException(error);
		}
		
		String foundOpFormat = null;

		for(String opFormat: legitOpFormats){
			
			if(correctSyntax(opFormat, assemblyLine)){
				foundOpFormat = opFormat;
				break;
			}
		}
		
		if(foundOpFormat == null){
			
			String error = "Assembly line syntax error. Check use of commas and spaces between operands. Expected syntax:\n";
			
			for(String opFormat: legitOpFormats)
				error += "\n" + opFormat;

			throw new AssemblerException(error);
		}

		MnemonicFormat format = mnemData.getMnemFormatHash().get(foundOpFormat);

		ArrayList<String> instructionFormat = format.getInstructionFormat();

		int insSize = 0;

		for (String instruction : instructionFormat) {

			InstructionFormatData insFormat = data.getInstructionFormat().get(instruction);

			ArrayList<String> instructions = insFormat.getOperands();

			for (String field : instructions) {

				int bits = insFormat.getOperandBitHash().get(field);

				insSize += bits;
			}
		}

		int minAdrUnit = data.getMinAdrUnit();

		int noOfAdrUnits = insSize / minAdrUnit;

		insNumber++;
		insAdrTable.put(insNumber, locationCounter);

		String label = getLabelString();

		if (label != null) {

			if (symbolTable.get(label) == null && dataTable.get(label) == null)
				symbolTable.put(label, locationCounter);

			else
				throw new AssemblerException("\"" + label
						+ "\" already exists in symbol table.");
		}

		locationCounter += noOfAdrUnits;
	}

	private void secondPass() {

		int lineCounter = 0;

		for (String assemblyLine : data.getAssemblyCode()) {

			lineCounter++;

			String[] commentSplit = assemblyLine.split(";");
			
			try {
				
				assemblyLine = commentSplit[0];
				
			} catch (ArrayIndexOutOfBoundsException e) {
				
				assemblyLine = "";
			}
			
			assemblyLine = assemblyLine.trim();

			if (assemblyLine.length() > 0) {

				try {
					
					analyseLineSecondPass(assemblyLine);
					
				} catch (AssemblerException e) {
					
					String error = getErrorMessage(lineCounter, assemblyLine, e.getMessage());
					objectCode.add(error);
					writeLinesToFile("object_code.txt", objectCode);
					System.exit(0);
				}
			}
		}
	}

	private void analyseLineSecondPass(String assemblyLine) throws AssemblerException {

		if (assemblyLine.startsWith(".data")){
			
			atData = true;
			atText = false;
		}
			

		else if (assemblyLine.startsWith(".text")) {
			
			atData = false;
			atText = true;
		}

		else if (atData)
			populateDataSecondPass(assemblyLine);

		else if (atText) 
			populateInstructionSecondPass(assemblyLine);		
	}

	private void populateDataSecondPass(String assemblyLine) throws AssemblerException {

		insNumber++;

		boolean legitIntDataLine = Pattern.matches(
				"[A-Za-z0-9]+\\s+[0-9]+MAU\\s+[^\\s]+", assemblyLine);

		boolean legitAsciiDataLine = Pattern.matches(
				"[A-Za-z0-9]+\\s+.ascii\\s+\".+\"", assemblyLine);

		boolean legitUninitializedDataLine = Pattern.matches(
				"[A-Za-z0-9]+\\s+[0-9]+MAU", assemblyLine);

		String binary = "";
		ArrayList<String> binaryArray = null;

		if (legitAsciiDataLine) {

			String[] splitByQuotation = assemblyLine.split("\"", 2);

			String asciiData = splitByQuotation[1].substring(0, splitByQuotation[1].length() - 1);

			binary = "";

			for (int i = 0; i < asciiData.length(); i++) {

				char character = asciiData.charAt(i);
				int ascii = (int) character;
				String asciiBinary = Integer.toBinaryString(ascii);
				binary += binaryFormatted(asciiBinary, 8);
			}

			binaryArray = splitToMinAdrUnits(binary);
		}

		else if (legitIntDataLine) {

			String[] splitDataLine = assemblyLine.split("\\s+");

			String integer = splitDataLine[2];

			try {

				binary = intToBinary(integer);

			} catch (NumberFormatException e) {

				throw new AssemblerException("\"" + integer
						+ "\" is not a valid integer.");
			}

			String minUnitTerm = splitDataLine[1];
			String[] splitMinUnitTerm = minUnitTerm.split("MAU");
			String noOfMinAdrUnitsStr = splitMinUnitTerm[0];
			int noOfMinAdrUnits = Integer.parseInt(noOfMinAdrUnitsStr);
			int minAdrUnit = data.getMinAdrUnit();
			int noOfBits = noOfMinAdrUnits * minAdrUnit;
			
			binary = binaryFormatted(binary, noOfBits);
			binaryArray = splitToMinAdrUnits(binary);

			if (binaryArray.size() > noOfMinAdrUnits)
				throw new AssemblerException("\"" + integer
						+ "\" exceeds expected bits.");
		}

		else if (legitUninitializedDataLine) {

			String[] splitDataLine = assemblyLine.split("\\s+");

			String minUnitTerm = splitDataLine[1];
			String[] splitMinUnitTerm = minUnitTerm.split("MAU");
			String noOfMinAdrUnitsStr = splitMinUnitTerm[0];
			int noOfMinAdrUnits = Integer.parseInt(noOfMinAdrUnitsStr);
			int minAdrUnit = data.getMinAdrUnit();
			int numberOfzeros = minAdrUnit * noOfMinAdrUnits;

			binary = binaryFormatted(binary, numberOfzeros);

			binaryArray = splitToMinAdrUnits(binary);
		}

		int adr = insAdrTable.get(insNumber);

		String address = Integer.toHexString(adr) + ":";
		String hexObjCode = getHexObjCode(binaryArray);		

		String objectCodeLine = String.format("%-10s %s", address, hexObjCode);
		objectCode.add(objectCodeLine);
		
		System.out.println(objectCodeLine);
	}

	private void populateInstructionSecondPass(String assemblyLine) throws AssemblerException {

		System.out.println("*****************************");
		System.out.println(assemblyLine);

		legitAssemblyOpTreePaths = new ArrayList<ArrayList<String>>();
		assemblyTermTypeHash = new HashMap<String, String>();

		analyseWithAssemblyOpTree(assemblyLine);

		System.out.println(legitAssemblyOpTreePaths);

		MnemonicData mnemData = getMnemData(assemblyLine);

		ArrayList<String> operandFormats = mnemData.getMnemFormats();

		ArrayList<String> legitOpFormats = new ArrayList<String>();

		for (String opFormat : operandFormats) {

			if (formatMatch(opFormat))
				legitOpFormats.add(opFormat);
		}
		
		String foundOpFormat = null;

		for(String opFormat: legitOpFormats){
			
			if(correctSyntax(opFormat, assemblyLine)){
				foundOpFormat = opFormat;
				break;
			}
		}

		MnemonicFormat format = mnemData.getMnemFormatHash().get(foundOpFormat);

		String insFieldLabels = format.getInsFieldLabels();

		HashMap<String, String> insFieldHash = null;		
		ArrayList<String> relevantOperands = null;

		if (insFieldLabels != "") {

			relevantOperands = getRelevantOperands(foundOpFormat);
			insFieldHash = mapInsFieldLabels(relevantOperands, insFieldLabels);
		}

		ArrayList<String> instructionFormat = format.getInstructionFormat();

		String binary = "";
		insNumber++;

		System.out.println("insFieldHash: " + insFieldHash);
		System.out.println("assTypeHash: " + assemblyTermTypeHash);

		for (String instruction : instructionFormat) {

			InstructionFormatData insFormat = data.getInstructionFormat().get(instruction);

			ArrayList<String> instructions = insFormat.getOperands();

			for (String field : instructions) {

				String binaryTemp = "";

				int bits = insFormat.getOperandBitHash().get(field);

				if (mnemData.getGlobalOpCodes().get(field) != null) // global
					binaryTemp = mnemData.getGlobalOpCodes().get(field);

				else if (format.getOpcodes().get(field) != null) // local
					binaryTemp = format.getOpcodes().get(field);

				else if (insFieldHash.get(field) != null) {

					String assemblyTerm = insFieldHash.get(field);

					if (data.getRegisterHash().get(assemblyTerm) != null) // reg
						binaryTemp = data.getRegisterHash().get(assemblyTerm);

					else if (assemblyTermTypeHash.get(assemblyTerm) != null) {

						String type = assemblyTermTypeHash.get(assemblyTerm);

						if (type.equals("INT"))
							binaryTemp = intToBinary(assemblyTerm);

						else if (type.equals("HEX"))
							binaryTemp = hexToBinary(assemblyTerm);

						else if (type.equals("LABEL")) {

							if (symbolTable.get(assemblyTerm) != null)
								binaryTemp = relativeJumpInBinary(assemblyTerm, bits);

							else if (dataTable.get(assemblyTerm) != null) 
								binaryTemp = dataOffset(assemblyTerm, bits);

							else
								throw new AssemblerException("Label \""
										+ assemblyTerm + " \" not found.");
						}
					}

					else {
						throw new AssemblerException(
								"Encoding data for \""
										+ assemblyTerm
										+ "\" (for instrucution field \""
										+ field
										+ "\") not found.\nIf term is a register, make sure it is defined as \""
										+ assemblyTerm
										+ "\" in registers (i.e., "
										+ assemblyTerm
										+ " 001B).\nIf term is an INT etc, make sure it is specified as so in assemblyOpTree (i.e., immediate : INT).");
					}

					int binaryLength = binaryTemp.length();

					if (binaryLength > bits) {

						String error = "Bit representation of \""
								+ assemblyTerm
								+ "\" exceeds expected number of bits (" + bits
								+ ")\nfor instruction field \"" + field + "\".";
						throw new AssemblerException(error);

					}

				}

				binary += binaryFormatted(binaryTemp, bits);
			}
		}

		ArrayList<String> binaryArray = splitToMinAdrUnits(binary);

		int adr = insAdrTable.get(insNumber);

		String address = Integer.toHexString(adr) + ":";
		String hexObjCode = getHexObjCode(binaryArray);		

		String objectCodeLine = String.format("%-10s %s", address, hexObjCode);
		objectCode.add(objectCodeLine);
		
		System.out.println(objectCodeLine);
	}

	static public void writeLinesToFile(String filename, ArrayList<String> lines) {

		File file = null;

		try {
			
			file = new File(filename);
			file.createNewFile();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}

		try {
			
			FileWriter writer = new FileWriter(file);

			for (String line : lines)
				writer.write(line + "\n");

			writer.close();

		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	private String getErrorMessage(int lineCounter, String assemblyLine, String message) {

		String msg = "------------------------------------------\n";
		msg += "Exception at line " + lineCounter + " :\n";
		msg += "\n";
		msg += assemblyLine + "\n";
		msg += "------------------------------------------\n";
		msg += "\n";
		msg += message + "\n\n";

		return msg;
	}

	private String dataOffset(String assemblyTerm, int bits) {

		int dataOffset = dataTable.get(assemblyTerm);

		String binary = Integer.toBinaryString(dataOffset);

		if (binary.length() > bits)
			binary = binary.substring(binary.length() - bits);

		return binary;
	}
	
	private void analyseWithAssemblyOpTree(String assemblyLine) throws AssemblerException {

		AssemblyOpTree assemblyOpTree = data.getAssemblyOpTree();
		String assemblyOpTreeRoot = assemblyOpTree.getRootToken();

		
		ArrayList<String> roots = assemblyOpTree.getAssemblyOpTreeHash().get(assemblyOpTreeRoot);

		ArrayList<ArrayList<String>> paths = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentPath = new ArrayList<String>();

		ArrayList<String> fullTermsIter = new ArrayList<String>();
		fullTermsIter.add(assemblyOpTreeRoot);

		ArrayList<String> assemblyList = new ArrayList<String>();

		String[] assemblySplit = assemblyLine.split("\\s+"); // space

		for (String str : assemblySplit) {

			if (!str.matches(",+")) {

				str = str.replaceAll("^,+", "");
				str = str.replaceAll(",+$", "");

				assemblyList.add(str);
			}
		}

		for(String rootTokens: roots){
			ArrayList<String> rootTerm = new ArrayList<String>();
			rootTerm.add(rootTokens);
			
			if(analyseOperands(rootTerm, assemblyList, rootTerm, rootTerm, paths,
				currentPath))
				break;
		}
	}

	private boolean analyseOperands(ArrayList<String> tokens,
			ArrayList<String> assemblyTokens,
			ArrayList<String> tokensToAnalyse, ArrayList<String> fullExp,
			ArrayList<ArrayList<String>> paths, ArrayList<String> currentPath) {

		boolean done = false;

		if (debug) {
			System.out.println("--------------------");
			System.out.println("terms: " + tokens);
			System.out.println("paths: " + paths);
			System.out.println("curpath: " + currentPath);
			System.out.println("asslist: " + assemblyTokens);
			System.out.println("fulltermsIter: " + fullExp);
			System.out.println("termsIter: " + tokensToAnalyse);
		}

		for (String token : tokens) {

			if (debug)
				System.out.println(token);

			String[] furtherTokenSplit = token.split("\\s+");

			if (furtherTokenSplit.length > 1) { 
				
				ArrayList<String> furtherTokens = new ArrayList<String>();

				for (String str : furtherTokenSplit)
					furtherTokens.add(str);

				done = analyseOperands(furtherTokens, assemblyTokens,
						tokensToAnalyse, fullExp, paths, currentPath);

				if (done)
					return true;

				else
					return false;
			}

			else { // one term

				String tempToken = "";
				
				if(token.charAt(token.length()-1) == '?' || token.charAt(token.length()-1) == '*' || token.charAt(token.length()-1) == '+')
					tempToken = token.substring(0, token.length()-1);
				
				else
					tempToken = token;			

				if (token.charAt(token.length() - 1) == '+') {

					ArrayList<String> oneOrMoreExp = new ArrayList<String>();
					String oneOrMore = tempToken + " " + tempToken + "*";
					oneOrMoreExp.add(oneOrMore);

					ArrayList<String> newTokensToAnalyse = updateExp(oneOrMoreExp, tokensToAnalyse, token);
					ArrayList<String> newFullExp = updateExp(oneOrMoreExp, fullExp, token);

					done = analyseOperands(oneOrMoreExp, assemblyTokens,
							newTokensToAnalyse, newFullExp, paths, currentPath);

					if (done)
						return true;

					else
						return false;
				}

				else {

					ArrayList<String> assemblyOpTreeToken = data.getAssemblyOpTree().getAssemblyOpTreeHash().get(tempToken);

					ArrayList<String> newCurrentPath = new ArrayList<String>(currentPath);

					if (token.charAt(token.length() - 1) == '?')
						newCurrentPath.add("?");

					if (!(token.startsWith("\"") && token.endsWith("\"")))
						newCurrentPath.add(tempToken);

					if (assemblyOpTreeToken != null) { // not leaf

						done = analyseOperands(assemblyOpTreeToken,
								assemblyTokens, tokensToAnalyse, fullExp,
								paths, newCurrentPath);

						if (done)
							return true;
					}

					else { // leaf

						String assemblyTerm = assemblyTokens.get(0);

						if (match(tempToken, assemblyTerm)) {

							if(!newCurrentPath.contains(assemblyTerm))
								newCurrentPath.add(assemblyTerm);

							if (debug)
								System.out.println("found: " + token);

							if (!validWithTokensToAnalyse(tokensToAnalyse, newCurrentPath))
								return false;

							ArrayList<ArrayList<String>> newPaths = new ArrayList<ArrayList<String>>(paths);
							newPaths.add(newCurrentPath);

							tokensToAnalyse = updateTokensToAnalyse(tokensToAnalyse, newCurrentPath);
							assemblyTokens = removeFirstElement(assemblyTokens);

							newCurrentPath = new ArrayList<String>();

							if (tokensToAnalyse.isEmpty() || assemblyTokens.isEmpty()) {

								if (tokensToAnalyse.isEmpty() && !assemblyTokens.isEmpty())
									return false;

								else if (!tokensToAnalyse.isEmpty()	&& assemblyTokens.isEmpty()) {

									if (!validWithFullExp(fullExp, newPaths))
										return false;
								}

								legitAssemblyOpTreePaths = newPaths; // legit

								return true;
							}

							done = analyseOperands(tokensToAnalyse,
									assemblyTokens, tokensToAnalyse, fullExp,
									newPaths, newCurrentPath);

							if (done)
								return true;
						}

						else { // not found

						}
					}
				}
			}
		}

		return done;
	}

	private ArrayList<String> updateTokensToAnalyse(ArrayList<String> tokensToAnalyse, ArrayList<String> currentPath) {

		ArrayList<String> newTermsIter = new ArrayList<String>();
		String newIterStr = "";

		String termsIter = tokensToAnalyse.get(0);
		String[] splitTermsIter = termsIter.split("\\s+");

		boolean found = false;
		int index = 0;

		for (String token : splitTermsIter) {

			for (String pathTerm : currentPath) {
				
				String tempToken = "";
				
				if(token.charAt(token.length()-1) == '*' || token.charAt(token.length()-1) == '?')
					tempToken = token.substring(0, token.length()-1);
				
				else
					tempToken = token;

				if (tempToken.equals(pathTerm)) {
					
					found = true;
					break;
				}
			}

			if (found && token.charAt(token.length() - 1) == '*')
				return tokensToAnalyse;

			if (found) {

				index++;
				break;
			}

			index++;
		}

		for (String iterTerm : splitTermsIter) {

			if (index <= 0)
				newIterStr += iterTerm + " ";

			index--;
		}

		newIterStr = newIterStr.trim();

		if (newIterStr != "")
			newTermsIter.add(newIterStr);

		return newTermsIter;
	}

	private ArrayList<String> updateExp(
			ArrayList<String> updateExp,
			ArrayList<String> expToUpdate, String tokenToChange) {

		ArrayList<String> newTermsIter = new ArrayList<String>();

		String newIterStr = "";

		String termsIter = expToUpdate.get(0);
		String[] splitTermsIter = termsIter.split("\\s+");

		boolean done = false;

		for (String iterTerm : splitTermsIter) {

			if (iterTerm.equals(tokenToChange) && !done) {

				for (String str : updateExp) {
					
					newIterStr += str + " ";
					done = true;
				}
			}

			else
				newIterStr += iterTerm + " ";
		}

		newIterStr = newIterStr.trim();
		newTermsIter.add(newIterStr);

		return newTermsIter;
	}

	private boolean validWithTokensToAnalyse(ArrayList<String> tokensToAnalyse,
			ArrayList<String> currentPath) {

		boolean legit = false;

		String termsIter = tokensToAnalyse.get(0);
		String[] splitTermsIter = termsIter.split("\\s+");

		for (String iterTerm : splitTermsIter) {

			String rawIterTerm = "";
			
			if(iterTerm.charAt(iterTerm.length()-1) == '?' || iterTerm.charAt(iterTerm.length()-1) == '*' || iterTerm.charAt(iterTerm.length()-1) == '+')
				rawIterTerm = iterTerm.substring(0, iterTerm.length()-1);
			
			else
				rawIterTerm = iterTerm;

			for (String pathTerm : currentPath) {

				if (rawIterTerm.equals(pathTerm) || iterTerm.equals(pathTerm)) {
					
					legit = true;
					break;
				}
			}

			if (legit)
				return true;

			else if (!(iterTerm.charAt(iterTerm.length() - 1) == '?')
					&& !(iterTerm.charAt(iterTerm.length() - 1) == '*'))
				return false;
		}

		return false;
	}

	private ArrayList<String> removeFirstElement(ArrayList<String> list) {

		boolean first = true;
		ArrayList<String> newList = new ArrayList<String>();

		for (String str : list) {

			if (first)
				first = false;

			else
				newList.add(str);
		}

		return newList;
	}

	private boolean validWithFullExp(
			ArrayList<String> fullParseTermsIter,
			ArrayList<ArrayList<String>> newPaths) {

		String str = fullParseTermsIter.get(0);
		String[] splitFullTermsIter = str.split("\\s+");

		boolean pathsFinished = false;
		int pathCounter = 0;
		ArrayList<String> path = null;

		for (String iterTerm : splitFullTermsIter) {

			if (pathsFinished) {

				if (!(iterTerm.charAt(iterTerm.length() - 1) == '?' || iterTerm.charAt(iterTerm.length() - 1) == '*'))
					return false;
			}

			else {

				path = newPaths.get(pathCounter);
//				String tempIterTerm = iterTerm.replaceAll("\\?|\\*", "");
				
				String tempIterTerm = "";
				
				if(iterTerm.charAt(iterTerm.length()-1) == '?' || iterTerm.charAt(iterTerm.length()-1) == '*' || iterTerm.charAt(iterTerm.length()-1) == '+')
					tempIterTerm = iterTerm.substring(0, iterTerm.length()-1);
				
				else
					tempIterTerm = iterTerm;

				if (iterTerm.charAt(iterTerm.length() - 1) == '*') {

					while (legitPath(path, tempIterTerm)) {

						pathCounter++;

						if (pathCounter > newPaths.size() - 1) {

							pathsFinished = true;
							break;
						}

						else
							path = newPaths.get(pathCounter);
					}
				}

				else if (!legitPath(path, tempIterTerm)) {

					if (!(iterTerm.charAt(iterTerm.length() - 1) == '?' || iterTerm.charAt(iterTerm.length() - 1) == '*'))
						return false;
				}

				else {

					pathCounter++;

					if (pathCounter > newPaths.size() - 1)
						pathsFinished = true;
				}
			}
		}

		return true;
	}

	private boolean legitPath(ArrayList<String> path, String iterTerm) {

		for (String pathTerm : path) {

			if (iterTerm.equals(pathTerm))
				return true;
		}

		return false;
	}

	private boolean match(String assemblyOpTreeTerm, String assemblyTerm) {

		if (assemblyOpTreeTerm.startsWith("\"") && assemblyOpTreeTerm.endsWith("\"")) {

			assemblyOpTreeTerm = assemblyOpTreeTerm.replaceAll("\"", "");

			return assemblyOpTreeTerm.equals(assemblyTerm);
		}

		else
			return nestedMatch(assemblyOpTreeTerm, assemblyTerm);
	}

	private boolean nestedMatch(String assemblyOpTreeTerm, String assemblyTerm) {

		String[] splitAssemblyOpTreeTerms = assemblyOpTreeTerm.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");

		String prefixes = "";

		for (String str : splitAssemblyOpTreeTerms) {

			if (!isAlphaNumeric(str))
				prefixes += "\\" + str;
		}

		String[] splitAssemblyTerms;

		if (prefixes.isEmpty()) {

			splitAssemblyTerms = new String[1];
			splitAssemblyTerms[0] = assemblyTerm;
		}

		else
			splitAssemblyTerms = assemblyTerm.split("(?=[" + prefixes + "])|(?<=[" + prefixes + "])");

		if (splitAssemblyOpTreeTerms.length != splitAssemblyTerms.length)
			return false;

		int i = 0;

		for (String term : splitAssemblyOpTreeTerms) {

			if (term.isEmpty() || splitAssemblyTerms[i].isEmpty()) {

				if (!(term.isEmpty() && splitAssemblyTerms[i].isEmpty()))
					return false;
			}

			// symbol
			else if (!isAlphaNumeric(term)) {

				if (!term.equals(splitAssemblyTerms[i]))
					return false;
			}

			// alphaNumeric
			else if (!term.equals(splitAssemblyTerms[i])){			
				
				ArrayList<String> assemblyOpTreeTerms = data.getAssemblyOpTree().getAssemblyOpTreeHash().get(term);

				// node
				if (assemblyOpTreeTerms != null) {
					
					boolean legit = false;

					for (String termFromHash : assemblyOpTreeTerms) {

						if (match(termFromHash, splitAssemblyTerms[i])) {

							legit = true;
							break;
						}
					}

					if (!legit)
						return false;
				}

				// is register or mnemonic
				else if (data.getRegisterHash().get(splitAssemblyTerms[i]) != null
						|| data.getMnemonicTable().get(splitAssemblyTerms[i]) != null)
					return false;

				else if (term.equals("HEX")) {

					if (!isHexNumber(splitAssemblyTerms[i]))
						return false;

					assemblyTermTypeHash.put(splitAssemblyTerms[i], term);
				}

				else if (term.equals("INT")) {

					if (!isNumeric(splitAssemblyTerms[i]))
						return false;

					assemblyTermTypeHash.put(splitAssemblyTerms[i], term);
				}

				else if (term.equals("LABEL")) {

					if (!isAlpha(splitAssemblyTerms[i]))
						return false;

					assemblyTermTypeHash.put(splitAssemblyTerms[i], term);
				}

				else 
					return false;
			}

			i++;
		}

		return true;
	}

	private MnemonicData getMnemData(String assemblyLine) throws AssemblerException {

		String[] assemblyLineSplit = assemblyLine.split("\\s+"); // space
		ArrayList<String> assemblyTermList = new ArrayList<String>();

		for (String assemblyTerm : assemblyLineSplit) {

			assemblyTerm = assemblyTerm.replaceAll("^,+", "");
			assemblyTerm = assemblyTerm.replaceAll(",+$", "");

			assemblyTermList.add(assemblyTerm);
		}

		MnemonicData mnemData = null;

		for (String assemblyTerm : assemblyTermList) {

			if (data.getMnemonicTable().get(assemblyTerm) != null) {

				mnemData = data.getMnemonicTable().get(assemblyTerm);
				break;
			}
		}

		return mnemData;
	}
	
	private boolean formatMatch(String mnemFormat) {

		String[] mnemFormatSplit = mnemFormat.split("\\s+"); // space
		ArrayList<String> mnemFormatList = new ArrayList<String>();

		for (String formatTerm : mnemFormatSplit) {

			formatTerm = formatTerm.replaceAll("^,+", "");
			formatTerm = formatTerm.replaceAll(",+$", "");

			mnemFormatList.add(formatTerm);
		}

		int i = 0;
		boolean found = false;
		boolean optional = false;

		for (ArrayList<String> path : legitAssemblyOpTreePaths) {

			for (String pathTerm : path) {

				if (i >= mnemFormatList.size())
					return false;

				if (pathTerm.equals(mnemFormatList.get(i)))
					found = true;

				else if ((pathTerm.equals("?")))
					optional = true;
				
			}
			
			if(found && !optional)	// assumes operands in opformat are not optional
				i++;
			
			else if (!found && !optional)
				return false;

			found = false;
			optional = false;
		}

		if (i != mnemFormatList.size())
			return false;

		return true;
	}

	private boolean correctSyntax(String format, String assemblyLine) {

		String[] formatSplit = format.split("(?=[,\\s]+)|(?<=[,\\s]+)");

		String regex = ".*";

		boolean inSpaces = false;

		for (String str : formatSplit) {

			if (str.matches("\\s+")) {

				if (!inSpaces) {
					
					regex += "\\s+";
					inSpaces = true;
				}
			}

			else {

				inSpaces = false;

				if (str.equals(","))
					regex += ",";

				else
					regex += "[^\\s,]+";
			}
		}

		regex += ".*";

		boolean legitSyntax = Pattern.matches(regex, assemblyLine);

		return legitSyntax;
	}

	private String getLabelString() { // assumes label in first path

		String label = null;
		boolean foundLabel = false;

		for (ArrayList<String> path : legitAssemblyOpTreePaths) {

			for (String term : path) {

				if (term.equals("LABEL"))
					foundLabel = true;

				if (foundLabel)
					label = term;
			}

			break;
		}

		return label;
	}

	private String relativeJumpInBinary(String insHashTerm, int bits) {

		int locationCounter = insAdrTable.get(insNumber + 1);
		int destination = symbolTable.get(insHashTerm);

		int jump = destination - locationCounter;

		String binary = Integer.toBinaryString(jump);

		if (binary.length() > bits)
			binary = binary.substring(binary.length() - bits);

		return binary;
	}
	
	private ArrayList<String> getRelevantOperands(String format) {

		ArrayList<String> relevantOperands = new ArrayList<String>();

		String[] formatSplit = format.split("[,\\s]+");

		int i = 0;

		for (ArrayList<String> path : legitAssemblyOpTreePaths) {

			for (String pathTerm : path) {

				if (pathTerm.equals(formatSplit[i])) {

					relevantOperands.add(getAssemblyOperand(path));
					i++;
					break;
				}
			}
		}

		return relevantOperands;
	}

	private String getAssemblyOperand(ArrayList<String> path) {

		String operand = path.get(path.size() - 1);

		operand = operand.replaceAll("\"", "");

		return operand;
	}

	private HashMap<String, String> mapInsFieldLabels(	//TODO
			ArrayList<String> relevantOperands, String insFieldLine)
			throws AssemblerException {

		HashMap<String, String> insHash = new HashMap<String, String>();

		String[] instructionLabels = insFieldLine.split("[\\s]+");

		if (relevantOperands.size() != instructionLabels.length) {

			String error = "Operand token mismatch between source assembly instruction operands and instruction field labels:\n\n";

			for (String operand : relevantOperands)
				error += operand + " ";

			error += "\n" + insFieldLine;

			throw new AssemblerException(error);
		}

		int i1 = 0;

		for (String insFieldToken : instructionLabels) {

			String operandToken = relevantOperands.get(i1);

			String[] splitInsTokens = insFieldToken.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");

			String prefixes = "";

			for (String str : splitInsTokens) {

				if (!isAlphaNumeric(str))
					prefixes += "\\" + str;
			}

			String[] splitOpTokens;

			if (prefixes.isEmpty()) {

				splitOpTokens = new String[1];
				splitOpTokens[0] = operandToken;
			}

			else
				splitOpTokens = operandToken.split("(?=[" + prefixes + "])|(?<=[" + prefixes + "])");

			if (splitOpTokens.length != splitInsTokens.length) {

				String error = "Syntax mismatch between instruction operands and instruction field labels:\n\n";

				for (String operand : relevantOperands)
					error += operand + " ";

				error += "\n" + insFieldLine;

				throw new AssemblerException(error);
			}

			int i2 = 0;

			for (String insTerm : splitInsTokens) {

				if (!isAlphaNumeric(insTerm)) {

					if (!insTerm.equals(splitOpTokens[i2])) {

						String error = "Could not map instruction fields to assembly line:\n\n"
								+ insFieldLine;

						throw new AssemblerException(error);
					}

				}

				else {

					String assemblyTerm = splitOpTokens[i2];
					insHash.put(insTerm, assemblyTerm);
				}

				i2++;
			}

			i1++;
		}

		return insHash;
	}
	
	private ArrayList<String> splitToMinAdrUnits(String binary) {

		ArrayList<String> binaryArray = new ArrayList<String>();

		int minAdrUnit = data.getMinAdrUnit();

		int index = 0;

		while (index < binary.length()) {

			binaryArray.add(binary.substring(index,	Math.min(index + minAdrUnit, binary.length())));

			index += minAdrUnit;
		}

		return binaryArray;
	}

	private String getHexObjCode(ArrayList<String> binaryArray) {

		String hexObjCode = "";

		int minAdrUnit = data.getMinAdrUnit();

		int noOfHexCharacters = (minAdrUnit / 8) * 2;

		if (data.getEndian().equals("big")) {

			for (String str : binaryArray) {

				String hex = binaryToHex(str);

				while (hex.length() < noOfHexCharacters)
					hex = "0" + hex;

				hexObjCode += hex + " ";
			}
		}

		else if (data.getEndian().equals("little")) {

			int counter = binaryArray.size() - 1;

			for (; counter >= 0; counter--) {

				String hex = binaryToHex(binaryArray.get(counter));

				while (hex.length() < noOfHexCharacters)
					hex = "0" + hex;

				hexObjCode += hex + " ";
			}
		}

		return hexObjCode;
	}

	public static String binaryFromIntFormatted(String intStr, int bits) throws AssemblerException {

		String binary = intToBinary(intStr);

		int initialLength = binary.length();
		int zerosNeeded = bits - initialLength;

		String zeros = "";

		for (; zerosNeeded > 0; zerosNeeded -= 1)
			zeros += "0";

		String finalString = zeros + binary;

		return finalString;
	}

	public static String binaryFromHexFormatted(String hex, int bits) throws AssemblerException {

		String binary = hexToBinary(hex);

		int initialLength = binary.length();
		int zerosNeeded = bits - initialLength;

		String zeros = "";

		for (; zerosNeeded > 0; zerosNeeded -= 1)
			zeros += "0";

		String finalString = zeros + binary;

		return finalString;
	}

	public static String binaryFormatted(String binary, int bits) {

		int initialLength = binary.length();

		int zerosNeeded = bits - initialLength;

		String zeros = "";

		for (; zerosNeeded > 0; zerosNeeded -= 1)
			zeros += "0";

		String finalString = zeros + binary;

		return finalString;
	}
	
	public static String hexToBinary(String s) {

		return new BigInteger(s, 16).toString(2);
	}

	public static String binaryToHex(String binary) {

		Long l = Long.parseLong(binary, 2);
		String hex = String.format("%X", l);

		return hex;
	}

	public static String intToBinary(String intStr) {

		int i = Integer.parseInt(intStr);

		String binary = Integer.toBinaryString(i);

		return binary;
	}

	private boolean isAlphaNumeric(String s) {

		String pattern = "[a-zA-Z0-9]*";

		if (s.matches(pattern))
			return true;

		return false;
	}

	private boolean isNumeric(String s) {

		String pattern = "[0-9]*";

		if (s.matches(pattern))
			return true;

		return false;
	}

	private boolean isAlpha(String s) {

		String pattern = "[a-zA-Z]*";

		if (s.matches(pattern))
			return true;

		return false;
	}
	
	private static boolean isHexNumber(String str) {
		
		try {
			
			Long.parseLong(str, 16);
			return true;
			
		} catch (NumberFormatException e) {
			
			return false;
		}
	}
}
