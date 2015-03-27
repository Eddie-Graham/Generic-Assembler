/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * <pre>
 * A two pass assembler.
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
	private boolean debugTree = false;

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

		// to account for last line
		insNumber++;
		insAdrTable.put(insNumber, locationCounter); 

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


		if (assemblyLine.equals(".data")){			
			if(dataDeclared)
				throw new AssemblerException(".data section already declared.");			
			dataDeclared = true;			
			atData = true;
			atText = false;
		}

		else if (assemblyLine.equals(".text")) {			
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
		
		if(debug)
			System.out.println(legitAssemblyOpTreePaths);

		if (legitAssemblyOpTreePaths.isEmpty())
			throw new AssemblerException("Assembly line not consistent with assemblyOpTree. Please check tree.");

		Mnemonic mnemData = getMnemData(assemblyLine);

		if (mnemData == null)
			throw new AssemblerException("Mnemonic not declared in MnemonicData section within specification file.");

		ArrayList<String> operandFormats = mnemData.getOperandsFormats();
		ArrayList<String> legitOpFormats = new ArrayList<String>();

		// Find operand format matches 
		for (String opFormat : operandFormats) {
			if (formatMatch(opFormat))
				legitOpFormats.add(opFormat);
		}

		if (legitOpFormats.isEmpty()) {
			String error = "Incorrectly formatted operands. Expected formats for mnemonic \""
					+ mnemData.getMnemonic() + "\":\n";
			for (String opFormat : operandFormats)
				error += "\n" + opFormat;			
			error += "\n\nIt is assumed that the operands specified above are NOT optional.\n"
					+ "Operand tree built from assembly line:\n\n" + legitAssemblyOpTreePaths;
			throw new AssemblerException(error);
		}
		
		ArrayList<String> relevantOperands = getRelevantOperands(legitOpFormats.get(0));		
		String foundOpFormat = null;

		// Match syntax of line (separator commas match)
		for(String opFormat: legitOpFormats){			
			if(correctSyntax(opFormat, assemblyLine, relevantOperands)){
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

		OperandFormat format = mnemData.getOperandFormatHash().get(foundOpFormat);
		ArrayList<String> instructionFormat = format.getInstructionFormat();
		int insSize = 0;

		for (String instruction : instructionFormat) {
			InstructionFormat insFormat = data.getInstructionFormatHash().get(instruction);
			ArrayList<String> instructions = insFormat.getFields();

			for (String field : instructions) {
				int bits = insFormat.getFieldBitHash().get(field);
				insSize += bits;
			}
		}

		int minAdrUnit = data.getMinAdrUnit();
		int noOfAdrUnits = insSize / minAdrUnit;
		insNumber++;
		insAdrTable.put(insNumber, locationCounter);
		
		// Find any relocation point labels
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

		if (assemblyLine.equals(".data")){			
			atData = true;
			atText = false;
		}
			

		else if (assemblyLine.equals(".text")) {			
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
		
		if(debug)
			System.out.println(objectCodeLine);
	}

	private void populateInstructionSecondPass(String assemblyLine) throws AssemblerException {

		if(debug){
			System.out.println("*****************************");
			System.out.println(assemblyLine);
		}

		legitAssemblyOpTreePaths = new ArrayList<ArrayList<String>>();
		assemblyTermTypeHash = new HashMap<String, String>();
		analyseWithAssemblyOpTree(assemblyLine);

		if(debug)
			System.out.println(legitAssemblyOpTreePaths);

		Mnemonic mnemData = getMnemData(assemblyLine);
		ArrayList<String> operandFormats = mnemData.getOperandsFormats();
		ArrayList<String> legitOpFormats = new ArrayList<String>();

		// Find operand format matches
		for (String opFormat : operandFormats) {
			if (formatMatch(opFormat))
				legitOpFormats.add(opFormat);
		}
		
		ArrayList<String> relevantOperands = getRelevantOperands(legitOpFormats.get(0));
		String foundOpFormat = null;

		// Match syntax of line (separator commas match)
		for(String opFormat: legitOpFormats){			
			if(correctSyntax(opFormat, assemblyLine, relevantOperands)){
				foundOpFormat = opFormat;
				break;
			}
		}

		OperandFormat format = mnemData.getOperandFormatHash().get(foundOpFormat);
		String opFieldEncodings = format.getOperandFieldEncodings();
		HashMap<String, String> insFieldHash = null;		

		if (opFieldEncodings != "") 
			insFieldHash = mapInsFieldLabels(relevantOperands, opFieldEncodings);

		ArrayList<String> instructionFormat = format.getInstructionFormat();
		String binary = "";
		insNumber++;

		if(debug){			
			System.out.println("opFieldHash: " + insFieldHash);
			System.out.println("assTypeHash: " + assemblyTermTypeHash);
		}

		for (String instruction : instructionFormat) {
			InstructionFormat insFormat = data.getInstructionFormatHash().get(instruction);
			ArrayList<String> instructions = insFormat.getFields();

			for (String field : instructions) {
				String binaryTemp = "";

				int bits = insFormat.getFieldBitHash().get(field);

				if (mnemData.getGlobalFieldEncodingHash().get(field) != null) // global
					binaryTemp = mnemData.getGlobalFieldEncodingHash().get(field);

				else if (format.getFieldBitHash().get(field) != null) // local
					binaryTemp = format.getFieldBitHash().get(field);

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
		
		if(debug)
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
		String rootNode = assemblyOpTree.getRootToken();		
		ArrayList<String> roots = assemblyOpTree.getAssemblyOpTreeHash().get(rootNode);
		ArrayList<ArrayList<String>> paths = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentPath = new ArrayList<String>();
		ArrayList<String> assemblyTokens = new ArrayList<String>();
		String[] assemblySplit = assemblyLine.split("\\s+"); // space

		for (String str : assemblySplit) {
			if (!str.matches(",+")) {
				str = str.replaceAll("^,+", "");
				str = str.replaceAll(",+$", "");
				assemblyTokens.add(str);
			}
		}

		for(String rootTokens: roots){
			ArrayList<String> rootTerm = new ArrayList<String>();
			rootTerm.add(rootTokens);
			
			try{
				if(analyseOperands(rootTerm, assemblyTokens, rootTerm, rootTerm, paths,
						currentPath))
						break;
			} catch(StackOverflowError e){
				throw new AssemblerException("StackOverflow: Check tree has no infinite loops.");
			}
		}
	}

	private boolean analyseOperands(ArrayList<String> tokens,
			ArrayList<String> assemblyTokens,
			ArrayList<String> tokensToAnalyse, ArrayList<String> fullExp,
			ArrayList<ArrayList<String>> paths, ArrayList<String> currentPath) {

		boolean done = false;

		if (debugTree) {
			System.out.println("--------------------");
			System.out.println("terms: " + tokens);
			System.out.println("paths: " + paths);
			System.out.println("curpath: " + currentPath);
			System.out.println("asslist: " + assemblyTokens);
			System.out.println("fulltermsIter: " + fullExp);
			System.out.println("termsIter: " + tokensToAnalyse);
		}

		for (String token : tokens) {

			if (debugTree)
				System.out.println(token);

			String[] furtherTokenSplit = token.split("\\s+");

			// If root expression
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

			// Single token
			else { 

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

					// Not leaf expression
					if (assemblyOpTreeToken != null) { 

						done = analyseOperands(assemblyOpTreeToken,
								assemblyTokens, tokensToAnalyse, fullExp,
								paths, newCurrentPath);

						if (done)
							return true;
					}

					// Leaf expression
					else { 
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
							tokensToAnalyse = removeFirstToken(tokensToAnalyse, newCurrentPath);
							assemblyTokens = removeFirstToken(assemblyTokens);
							newCurrentPath = new ArrayList<String>();

							if (tokensToAnalyse.isEmpty() || assemblyTokens.isEmpty()) {
								if (tokensToAnalyse.isEmpty() && !assemblyTokens.isEmpty())
									return false;
								else if (!tokensToAnalyse.isEmpty()	&& assemblyTokens.isEmpty()) {
									if (!validWithFullExp(fullExp, newPaths))
										return false;
								}

								// Valid with tree
								legitAssemblyOpTreePaths = newPaths; 
								return true;
							}

							done = analyseOperands(tokensToAnalyse,
									assemblyTokens, tokensToAnalyse, fullExp,
									newPaths, newCurrentPath);

							if (done)
								return true;
						}
					}
				}
			}
		}

		return done;
	}

	private ArrayList<String> removeFirstToken(ArrayList<String> tokensToAnalyseArray, ArrayList<String> currentPath) {

		ArrayList<String> newTermsIter = new ArrayList<String>();
		String newTokensToAnalyse = "";
		String tokensToAnalyse = tokensToAnalyseArray.get(0);
		String[] splitTokensToAnalyse = tokensToAnalyse.split("\\s+");
		boolean found = false;
		int index = 0;

		for (String token : splitTokensToAnalyse) {
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
				return tokensToAnalyseArray;

			if (found) {
				index++;
				break;
			}
			index++;
		}

		for (String token : splitTokensToAnalyse) {
			if (index <= 0)
				newTokensToAnalyse += token + " ";
			index--;
		}

		newTokensToAnalyse = newTokensToAnalyse.trim();

		if (newTokensToAnalyse != "")
			newTermsIter.add(newTokensToAnalyse);

		return newTermsIter;
	}

	private ArrayList<String> updateExp(
			ArrayList<String> updateExp,
			ArrayList<String> expToUpdate, String tokenToChange) {

		ArrayList<String> newTermsIter = new ArrayList<String>();
		String newExpStr = "";
		String exp = expToUpdate.get(0);
		String[] splitExp = exp.split("\\s+");
		boolean done = false;

		for (String token : splitExp) {
			if (token.equals(tokenToChange) && !done) {
				for (String str : updateExp) {					
					newExpStr += str + " ";
					done = true;
				}
			}
			else
				newExpStr += token + " ";
		}

		newExpStr = newExpStr.trim();
		newTermsIter.add(newExpStr);

		return newTermsIter;
	}

	private boolean validWithTokensToAnalyse(ArrayList<String> tokensToAnalyseArray,
			ArrayList<String> currentPath) {

		boolean legit = false;
		String tokensToAnalyse = tokensToAnalyseArray.get(0);
		String[] splitTokensToAnalyse = tokensToAnalyse.split("\\s+");

		for (String token : splitTokensToAnalyse) {
			String tempToken = "";
			
			if(token.charAt(token.length()-1) == '?' || token.charAt(token.length()-1) == '*' || token.charAt(token.length()-1) == '+')
				tempToken = token.substring(0, token.length()-1);			
			else
				tempToken = token;

			for (String pathTerm : currentPath) {
				if (tempToken.equals(pathTerm) || token.equals(pathTerm)) {					
					legit = true;
					break;
				}
			}

			if (legit)
				return true;

			else if (!(token.charAt(token.length() - 1) == '?')
					&& !(token.charAt(token.length() - 1) == '*'))
				return false;
		}

		return false;
	}

	private ArrayList<String> removeFirstToken(ArrayList<String> list) {

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
			ArrayList<String> fullExp,
			ArrayList<ArrayList<String>> newPaths) {

		String exp = fullExp.get(0);
		String[] splitExp = exp.split("\\s+");
		boolean pathsFinished = false;
		int i = 0;
		ArrayList<String> path = null;

		for (String token : splitExp) {

			if (pathsFinished) {
				if (!(token.charAt(token.length() - 1) == '?' || token.charAt(token.length() - 1) == '*'))
					return false;
			}

			else {
				path = newPaths.get(i);				
				String tempToken = "";
				
				if(token.charAt(token.length()-1) == '?' || token.charAt(token.length()-1) == '*' || token.charAt(token.length()-1) == '+')
					tempToken = token.substring(0, token.length()-1);				
				else
					tempToken = token;

				if (token.charAt(token.length() - 1) == '*') {
					
					while (legitPath(path, tempToken)) {
						i++;
						if (i > newPaths.size() - 1) {
							pathsFinished = true;
							break;
						}
						else
							path = newPaths.get(i);
					}
				}

				else if (!legitPath(path, tempToken)) {
					if (!(token.charAt(token.length() - 1) == '?' || token.charAt(token.length() - 1) == '*'))
						return false;
				}

				else {
					i++;
					if (i > newPaths.size() - 1)
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

			// Symbol
			else if (!isAlphaNumeric(term)) {
				if (!term.equals(splitAssemblyTerms[i]))
					return false;
			}

			// AlphaNumeric
			else if (!term.equals(splitAssemblyTerms[i])){			
				
				ArrayList<String> assemblyOpTreeTerms = data.getAssemblyOpTree().getAssemblyOpTreeHash().get(term);

				// Node
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

				// Is register or mnemonic
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

	private Mnemonic getMnemData(String assemblyLine) throws AssemblerException {

		String[] assemblyLineSplit = assemblyLine.split("\\s+"); 
		ArrayList<String> assemblyTermList = new ArrayList<String>();

		for (String assemblyTerm : assemblyLineSplit) {
			assemblyTerm = assemblyTerm.replaceAll("^,+", "");
			assemblyTerm = assemblyTerm.replaceAll(",+$", "");
			assemblyTermList.add(assemblyTerm);
		}

		Mnemonic mnemData = null;

		for (String assemblyTerm : assemblyTermList) {
			if (data.getMnemonicTable().get(assemblyTerm) != null) {
				mnemData = data.getMnemonicTable().get(assemblyTerm);
				break;
			}
		}

		return mnemData;
	}
	
	private boolean formatMatch(String mnemFormat) {

		String[] mnemFormatSplit = mnemFormat.split("\\s+"); 
		ArrayList<String> mnemFormatTokens = new ArrayList<String>();

		for (String token : mnemFormatSplit) {
			token = token.replaceAll("^,+", "");
			token = token.replaceAll(",+$", "");
			if(!token.isEmpty())
				mnemFormatTokens.add(token);
		}

		int i = 0;
		boolean found = false;
		boolean optional = false;

		for (ArrayList<String> path : legitAssemblyOpTreePaths) {

			for (String pathTerm : path) {

				if (i >= mnemFormatTokens.size())
					return false;

				if (pathTerm.equals(mnemFormatTokens.get(i)))
					found = true;

				else if ((pathTerm.equals("?")))
					optional = true;				
			}
			
			// Assumes nodes specified in operand format are not optional
			if(found && !optional)	
				i++;
			
			else if (!found && !optional)
				return false;

			found = false;
			optional = false;
		}

		if (i != mnemFormatTokens.size())
			return false;

		return true;
	}
	
	private boolean correctSyntax(String format, String assemblyLine, ArrayList<String> relevantOperands) {

		String[] formatSplit = format.split("\\s+");
		int noOfTokens = formatSplit.length;

		String regex = ".*";

		int i = 1;
		int i2 = 0;

		for (String str : formatSplit) {
			
			if(i > 1 && i <= noOfTokens)
				regex += "\\s+";
			
			String[] strSplit = str.split("((?=^[,]*)|(?<=^[,]*))|((?=[,]*$)|(?<=[,]*$))");
			
			for(String str2: strSplit){				
				if(!str2.isEmpty()){					
					if(str2.equals(","))
						regex += ",";				
					else{								
						regex += "("+ Pattern.quote(relevantOperands.get(i2))+")";
						i2++;
					}					
				}
			}			
			i++;
		}	

		boolean legitSyntax = Pattern.matches(regex, assemblyLine);

		return legitSyntax;
	}

	private String getLabelString() { 
		
		// Assumes relocation labels at beginning of instruction (in first path)

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
		
		ArrayList<String> relevantOps = new ArrayList<String>();
		String[] mnemFormatSplit = format.split("\\s+"); 
		ArrayList<String> mnemFormatTokens = new ArrayList<String>();

		for (String formatTerm : mnemFormatSplit) {
			formatTerm = formatTerm.replaceAll("^,+", "");
			formatTerm = formatTerm.replaceAll(",+$", "");
			if(!formatTerm.isEmpty())
				mnemFormatTokens.add(formatTerm);
		}

		int i = 0;
		boolean found = false;
		boolean optional = false;

		for (ArrayList<String> path : legitAssemblyOpTreePaths) {
			for (String pathTerm : path) {

				if (pathTerm.equals(mnemFormatTokens.get(i)))
					found = true;

				else if ((pathTerm.equals("?")))
					optional = true;				
			}
			
			// Assumes nodes in operand format are not optional
			if(found && !optional){					
				i++;
				relevantOps.add(getAssemblyOperand(path));
			}

			found = false;
			optional = false;
		}

		return relevantOps;
	}

	private String getAssemblyOperand(ArrayList<String> path) {

		String operand = path.get(path.size() - 1);
		operand = operand.replaceAll("\"", "");

		return operand;
	}

	private HashMap<String, String> mapInsFieldLabels(
			ArrayList<String> relevantOperands, String fieldEncodingLine)
			throws AssemblerException {

		HashMap<String, String> insHash = new HashMap<String, String>();
		String[] opFieldEncodings = fieldEncodingLine.split("\\s+");

		if (relevantOperands.size() != opFieldEncodings.length) {
			String error = "Token mismatch between source assembly operands and operand fields:\n\n";
			error += "Source assembly operands: ";			
			for (String operand : relevantOperands)
				error += operand + " ";
			error += "\nOperand field encodings:  " + fieldEncodingLine;			
			error += "\n\nField encodings should be mapped to the corresponding operand delimited by whitespace. "
					+ "\nExample input:\n\n"
					+ "mnem reg32, reg32"
					+ "\n\tmnem rm reg";
			throw new AssemblerException(error);
		}

		int i1 = 0;

		for (String op : opFieldEncodings) {
			String assemblyToken = relevantOperands.get(i1);
			String[] splitFieldTokens = op.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");
			String prefixes = "";

			for (String str : splitFieldTokens) {
				if (!isAlphaNumeric(str))
					prefixes += "\\" + str;
			}

			String[] splitAssemblyTokens;

			if (prefixes.isEmpty()) {
				splitAssemblyTokens = new String[1];
				splitAssemblyTokens[0] = assemblyToken;
			}

			else
				splitAssemblyTokens = assemblyToken.split("(?=[" + prefixes + "])|(?<=[" + prefixes + "])");

			if (splitAssemblyTokens.length != splitFieldTokens.length) {
				String error = "Syntax mismatch between instruction operands and field encodings:\n\n";
				error += "Source assembly operands:  ";				
				for (String operand : relevantOperands)
					error += operand + " ";				
				error += "\nOperand field encodings:  " + fieldEncodingLine;
				error += "\n\nSeparator commas should NOT be specified within the operand field encoding tokens, "
						+ "\nExample input:\n\n"
						+ "mnem reg32, reg32"
						+ "\n\tmnem rm reg";

				throw new AssemblerException(error);
			}

			int i2 = 0;

			for (String insTerm : splitFieldTokens) {
				if (!isAlphaNumeric(insTerm)) {
					if (!insTerm.equals(splitAssemblyTokens[i2])) {
						String error = "Could not map instruction fields to assembly line:\n\n"
								+ fieldEncodingLine;
						throw new AssemblerException(error);
					}
				}
				else {
					String assemblyTerm = splitAssemblyTokens[i2];
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

	public static boolean isAlphaNumeric(String s) {

		String pattern = "[a-zA-Z0-9]*";

		if (s.matches(pattern))
			return true;

		return false;
	}

	public static boolean isNumeric(String s) {

		String pattern = "[0-9]*";

		if (s.matches(pattern))
			return true;

		return false;
	}

	public static boolean isAlpha(String s) {

		String pattern = "[a-zA-Z]*";

		if (s.matches(pattern))
			return true;

		return false;
	}
	
	public static boolean isHexNumber(String str) {
		
		try {			
			Long.parseLong(str, 16);
			return true;			
		} catch (NumberFormatException e) {			
			return false;
		}
	}
}
