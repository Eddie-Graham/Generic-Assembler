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
	private boolean first, second;

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

		first = false;
		second = false;

		objectCode = new ArrayList<String>();

		assemble();
	}

	private void assemble() {

		if (!data.isErrorInSpecFile()) {

			firstPass();

			insNumber++;
			insAdrTable.put(insNumber, locationCounter); // to account for last line

			insNumber = 0;

			secondPass();
		}

		else
			objectCode.add("Error in specification file. See \"spec_error_report.txt\"");

		writeObjectCodeToFile();
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

			if (assemblyLine.trim().length() > 0) {

				try {
					
					analyseLineFirstPass(assemblyLine);
					
				} catch (AssemblerException e) {
					
					String error = getErrorMessage(lineCounter, assemblyLine, e.getMessage());
					objectCode.add(error);
					writeObjectCodeToFile();
					System.exit(0);
				}
			}
		}
	}
	
	private void analyseLineFirstPass(String assemblyLine) throws AssemblerException {

		assemblyLine.replaceAll("\\s+$", ""); // remove end whitespace

		if (assemblyLine.startsWith("section .data"))
			setBooleanValues(true, false);

		else if (assemblyLine.startsWith("section .text")) {
			
			setBooleanValues(false, true);
			first = true;
		}

		else if (atData) 
			analyseDataFirstPass(assemblyLine);

		else if (atText) {

			if (first) {

				if (assemblyLine.startsWith("\tglobal main")) {

					first = false;
					second = true;
				}
			}

			else if (second) {

				if (assemblyLine.startsWith("main:"))
					second = false;
			}

			else
				analyseInstructionsFirstPass(assemblyLine);
		}
	}

	private void analyseDataFirstPass(String assemblyLine) throws AssemblerException {

		assemblyLine = assemblyLine.trim();

		boolean legitIntDataLine = Pattern.matches(
				"[A-Za-z0-9]+\\s+[0-9]+MAU\\s+[^\\s]+", assemblyLine);

		boolean legitAsciiDataLine = Pattern.matches(
				"[A-Za-z0-9]+\\s+.ascii\\s+\".+\"", assemblyLine);

		boolean legitUninitializedDataLine = Pattern.matches(
				"[A-Za-z0-9]+\\s+[0-9]+MAU", assemblyLine);

		String[] splitDataLine = assemblyLine.split("\\s+");
		String label = splitDataLine[0];

		if (!legitIntDataLine && !legitAsciiDataLine && !legitUninitializedDataLine)
			throw new AssemblerException(".data line incorrect syntax.");

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

		assemblyLine = assemblyLine.trim();

		analyseWithAssemblyOpTree(assemblyLine);

		if (legitAssemblyOpTreePaths.isEmpty())
			throw new AssemblerException("Line not consistent with assemblyOpTree");

		MnemonicData mnemData = getMnemData(assemblyLine);

		if (mnemData == null)
			throw new AssemblerException("Unable to find mnemonic in mnemonicData.");

		ArrayList<String> mnemFormats = mnemData.getMnemFormats();

		String mnemFormat = null;

		for (String format : mnemFormats) {

			if (formatMatch(format)) {

				mnemFormat = format; // find type
				break;
			}
		}

		if (mnemFormat == null) {

			String error = "Incorrectly formatted operands. Expected:\n";

			for (String format : mnemFormats)
				error += "\n" + format;

			throw new AssemblerException(error);
		}

		if (!correctSyntax(mnemFormat, assemblyLine)) {

			String error = "Assembly line syntax error. Check use of commas and spaces between operands. Expected syntax:\n";
			error += "\n" + mnemFormat;

			throw new AssemblerException(error);
		}

		MnemFormat format = mnemData.getMnemFormatHash().get(mnemFormat);

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

			if (assemblyLine.trim().length() > 0) {

				try {
					
					analyseLineSecondPass(assemblyLine);
					
				} catch (AssemblerException e) {
					
					String error = getErrorMessage(lineCounter, assemblyLine, e.getMessage());
					objectCode.add(error);
					writeObjectCodeToFile();
					System.exit(0);
				}
			}
		}
	}

	private void analyseLineSecondPass(String assemblyLine) throws AssemblerException {

		assemblyLine.replaceAll("\\s+$", ""); // remove end whitespace

		if (assemblyLine.startsWith("section .data"))
			setBooleanValues(true, false);

		else if (assemblyLine.startsWith("section .text")) {
			
			setBooleanValues(false, true);
			first = true;
		}

		else if (atData)
			populateDataSecondPass(assemblyLine);

		else if (atText) {

			if (first) {

				if (assemblyLine.startsWith("\tglobal main")) {

					first = false;
					second = true;
				}
			}

			else if (second) {

				if (assemblyLine.startsWith("main:"))
					second = false;
			}

			else
				populateInstructionSecondPass(assemblyLine);
		}
	}

	private void populateDataSecondPass(String assemblyLine) throws AssemblerException {

		assemblyLine = assemblyLine.trim();

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

		assemblyLine = assemblyLine.trim();

		System.out.println("*****************************");
		System.out.println(assemblyLine);

		legitAssemblyOpTreePaths = new ArrayList<ArrayList<String>>();
		assemblyTermTypeHash = new HashMap<String, String>();

		analyseWithAssemblyOpTree(assemblyLine);

		System.out.println(legitAssemblyOpTreePaths);

		MnemonicData mnemData = getMnemData(assemblyLine);

		ArrayList<String> mnemFormats = mnemData.getMnemFormats();

		String mnemFormat = null;

		for (String format : mnemFormats) {

			if (formatMatch(format)) {

				mnemFormat = format; // find type
				break;
			}
		}

		MnemFormat format = mnemData.getMnemFormatHash().get(mnemFormat);

		String insFieldLabels = format.getInsFieldLabels();

		HashMap<String, String> insFieldHash = null;
		String relevantOperands = null;

		if (insFieldLabels != "") {

			relevantOperands = getRelevantOperands(mnemFormat);
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

	private void writeObjectCodeToFile() {

		File file = null;

		try {
			
			file = new File("object.txt");
			file.createNewFile();
			
		} catch (Exception e) {
			
			e.printStackTrace();
		}

		try {
			
			FileWriter writer = new FileWriter(file);

			for (String line : objectCode)
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
		msg += "\n";
		msg += "\n";
		msg += message + "\n\n";

		return msg;
	}

	private void setBooleanValues(boolean atData, boolean atText) {

		this.atData = atData;
		this.atText = atText;
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

		ArrayList<String> rootTerm = new ArrayList<String>();
		rootTerm.add(assemblyOpTreeRoot);

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

		analyseOperands(rootTerm, assemblyList, rootTerm, fullTermsIter, paths,
				currentPath, assemblyOpTreeRoot);
	}

	private boolean analyseOperands(ArrayList<String> parseTerms,
			ArrayList<String> assemblyListIter,
			ArrayList<String> parseTermsIter,
			ArrayList<String> fullParseTermsIter,
			ArrayList<ArrayList<String>> paths, ArrayList<String> currentPath,
			String parent) {

		boolean done = false;

		if (debug) {
			System.out.println("--------------------");
			System.out.println("terms: " + parseTerms);
			System.out.println("parent: " + parent);
			System.out.println("paths: " + paths);
			System.out.println("curpath: " + currentPath);
			System.out.println("asslist: " + assemblyListIter);
			System.out.println("fulltermsIter: " + fullParseTermsIter);
			System.out.println("termsIter: " + parseTermsIter);
		}

		for (String parseTerm : parseTerms) {

			if (debug)
				System.out.println(parseTerm);

			String[] splitParseTerm = parseTerm.split("\\s+");

			if (splitParseTerm.length > 1) { // more than one term, update iter

				ArrayList<String> splitParseTermList = new ArrayList<String>();

				for (String str : splitParseTerm)
					splitParseTermList.add(str);

				ArrayList<String> newParseTermsIter = updateTermsIter(splitParseTermList, parseTermsIter, parent);
				ArrayList<String> newFullParseTermsIter = updateTermsIter(splitParseTermList, fullParseTermsIter, parent);

				done = analyseOperands(splitParseTermList, assemblyListIter,
						newParseTermsIter, newFullParseTermsIter, paths,
						currentPath, parent);

				if (done)
					return true;
			}

			else { // one term

				String tempParseTerm = parseTerm.replaceAll("\\?|\\*|\\+", "");

				if (parseTerm.charAt(parseTerm.length() - 1) == '+') {

					ArrayList<String> oneOrMoreParseTerm = new ArrayList<String>();
					String oneOrMore = tempParseTerm + " " + tempParseTerm + "*";
					oneOrMoreParseTerm.add(oneOrMore);

					ArrayList<String> newParseTermsIter = updateTermsIter(oneOrMoreParseTerm, parseTermsIter, parseTerm);
					ArrayList<String> newFullParseTermsIter = updateTermsIter(oneOrMoreParseTerm, fullParseTermsIter, parseTerm);

					done = analyseOperands(oneOrMoreParseTerm,
							assemblyListIter, newParseTermsIter,
							newFullParseTermsIter, paths, currentPath, parent);

					if (done)
						return true;
				}
				
				else{

					ArrayList<String> assemblyOpTreeTerms = data.getAssemblyOpTree().getAssemblyOpTreeHash().get(tempParseTerm);

					ArrayList<String> newCurrentPath = clone(currentPath);
//					newCurrentPath.add(parseTerm);
				
					if(parseTerm.charAt(parseTerm.length()-1) == '?')
						newCurrentPath.add(parseTerm);
				
					newCurrentPath.add(tempParseTerm);

					if (assemblyOpTreeTerms != null) { // not leaf

						if (!(parent.charAt(parent.length() - 1) == '*')) {

							ArrayList<String> parseTerm1 = new ArrayList<String>();
							parseTerm1.add(parseTerm);

							ArrayList<String> newParseTermsIter = updateTermsIter(parseTerm1, parseTermsIter, parent);
							ArrayList<String> newFullParseTermsIter = updateTermsIter(parseTerm1, fullParseTermsIter, parent);

							done = analyseOperands(assemblyOpTreeTerms, assemblyListIter,
									newParseTermsIter, newFullParseTermsIter,
									paths, newCurrentPath, parseTerm);
						}

						else
							done = analyseOperands(assemblyOpTreeTerms, assemblyListIter,
									parseTermsIter, fullParseTermsIter, paths,
									newCurrentPath, parent);

						if (done)
							return true;
					}

					else { // leaf

						String assemblyTerm = assemblyListIter.get(0);

						if (match(parseTerm, assemblyTerm, newCurrentPath)) {

							if (debug)
								System.out.println("found: " + parseTerm);

							if (!legitIter(parseTermsIter, newCurrentPath))
								return false;

							ArrayList<ArrayList<String>> newPaths = clone2(paths);
							newPaths.add(newCurrentPath);

							parseTermsIter = updateTermsIter(parseTermsIter,newCurrentPath);
							assemblyListIter = removeFirstElement(assemblyListIter);

							newCurrentPath = new ArrayList<String>();

							if (parseTermsIter.isEmpty() || assemblyListIter.isEmpty()) {

								if (parseTermsIter.isEmpty() && !assemblyListIter.isEmpty())
									return false;

								else if (!parseTermsIter.isEmpty() && assemblyListIter.isEmpty()) {

									if (!legitWithFullTermsIter(fullParseTermsIter,	newPaths))
										return false;
								}

								legitAssemblyOpTreePaths = newPaths; // legit

								return true;
							}

							done = analyseOperands(parseTermsIter,
									assemblyListIter, parseTermsIter,
									fullParseTermsIter, newPaths, newCurrentPath,
									data.getAssemblyOpTree().getRootToken());

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

	private ArrayList<String> updateTermsIter(ArrayList<String> parseTermsIter, ArrayList<String> currentPath) {

		ArrayList<String> newTermsIter = new ArrayList<String>();
		String newIterStr = "";

		String termsIter = parseTermsIter.get(0);
		String[] splitTermsIter = termsIter.split("\\s+");

		boolean found = false;
		int index = 0;

		for (String iterTerm : splitTermsIter) {

			String tempIterTerm = iterTerm.replaceAll("\\?|\\*", "");

			for (String pathTerm : currentPath) {

				if (tempIterTerm.equals(pathTerm)) {
					
					found = true;
					break;
				}
			}

			if (found && iterTerm.charAt(iterTerm.length() - 1) == '*')
				return parseTermsIter;

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

	private ArrayList<String> updateTermsIter(
			ArrayList<String> splitParseTermList,
			ArrayList<String> parseTermsIter, String parent) {

		ArrayList<String> newTermsIter = new ArrayList<String>();

		String newIterStr = "";

		String termsIter = parseTermsIter.get(0);
		String[] splitTermsIter = termsIter.split("\\s+");

		boolean done = false;

		for (String iterTerm : splitTermsIter) {

			if (iterTerm.equals(parent) && !done) {

				for (String str : splitParseTermList) {
					
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

	private boolean legitIter(ArrayList<String> parseTermsIter,
			ArrayList<String> newCurrentPath) {

		boolean legit = false;

		String termsIter = parseTermsIter.get(0);
		String[] splitTermsIter = termsIter.split("\\s+");

		for (String iterTerm : splitTermsIter) {

			String rawIterTerm = iterTerm.replaceAll("\\?|\\*", "");

			for (String pathTerm : newCurrentPath) {

				if (rawIterTerm.equals(pathTerm)) {
					
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

	private boolean legitWithFullTermsIter(
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
				String tempIterTerm = iterTerm.replaceAll("\\?|\\*", "");

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

	private boolean match(String assemblyOpTreeTerm, String assemblyTerm,
			ArrayList<String> currentPath) {

		if (assemblyOpTreeTerm.startsWith("\"") && assemblyOpTreeTerm.endsWith("\"")) {

			assemblyOpTreeTerm = assemblyOpTreeTerm.replaceAll("\"", "");

			return assemblyOpTreeTerm.equals(assemblyTerm);
		}

		else
			return nestedMatch(assemblyOpTreeTerm, assemblyTerm, currentPath);
	}

	private boolean nestedMatch(String assemblyOpTreeTerm, String assemblyTerm,
			ArrayList<String> currentPath) {

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

			else if (!isAlphaNumeric(term)) {

				if (!term.equals(splitAssemblyTerms[i]))
					return false;
			}

			else if (term.equals(splitAssemblyTerms[i])) {

			}

			else {
				
				ArrayList<String> assemblyOpTreeTerms = data.getAssemblyOpTree().getAssemblyOpTreeHash().get(term);

				if (assemblyOpTreeTerms != null) {
					
					boolean legit = false;

					for (String termFromHash : assemblyOpTreeTerms) {

						if (match(termFromHash, splitAssemblyTerms[i], currentPath)) {

							legit = true;
							break;
						}
					}

					if (!legit)
						return false;
				}

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

		currentPath.add(assemblyTerm);

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

		String[] splitFormat = mnemFormat.split("[,\\s]+");

		int i = 0;
		boolean legit = false;

		for (ArrayList<String> path : legitAssemblyOpTreePaths) {

			for (String pathTerm : path) {

				if (i >= splitFormat.length)
					return false;

				if (pathTerm.equals(splitFormat[i])) {

					legit = true;
					i++;
					break;
				}

				else if ((pathTerm.charAt(pathTerm.length() - 1) == '?')) {

					legit = true;
					break;
				}
			}

			if (!legit)
				return false;

			legit = false;
		}

		if (i != splitFormat.length)
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

	private String getRelevantOperands(String format) {

		String operands = "";
		String[] formatSplit = format.split("[,\\s]+");

		int i = 0;

		for (ArrayList<String> path : legitAssemblyOpTreePaths) {

			for (String pathTerm : path) {

				if (pathTerm.equals(formatSplit[i])) {

					operands += getAssemblyOperand(path) + " ";
					i++;
					break;
				}
			}
		}

		return operands.trim();
	}

	private String getAssemblyOperand(ArrayList<String> path) {

		String operand = path.get(path.size() - 1);

		operand = operand.replaceAll("\"", "");

		return operand;
	}
	
	private HashMap<String, String> mapInsFieldLabels(String assemblyLine,
			String insLabels) throws AssemblerException {

		HashMap<String, String> insHash = new HashMap<String, String>();

		insLabels = insLabels.replaceAll("\\s+", " ");

		String[] splitInsTerms = insLabels.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");

		String prefixes = "";

		for (String str : splitInsTerms) {

			if (!isAlphaNumeric(str))
				prefixes += "\\" + str;
		}

		String[] splitAssemblyTerms = assemblyLine.split("(?=[" + prefixes + "])|(?<=[" + prefixes + "])");

		// System.out.println(Arrays.toString(splitAssemblyTerms));
		// System.out.println(Arrays.toString(splitInsTerms));

		if (splitAssemblyTerms.length != splitInsTerms.length) {

			String error = "Could not map instruction fields to assembly line:\n\n"
					+ insLabels;

			throw new AssemblerException(error);
		}

		int i = 0;

		for (String insTerm : splitInsTerms) {

			if (!isAlphaNumeric(insTerm)) {

				if (!insTerm.equals(splitAssemblyTerms[i])) {

					String error = "Could not map instruction fields to assembly line:\n\n"
							+ insLabels;

					throw new AssemblerException(error);
				}

			}

			else {

				String assemblyTerm = splitAssemblyTerms[i];
				insHash.put(insTerm, assemblyTerm);
			}

			i++;
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

	private ArrayList<ArrayList<String>> clone2(ArrayList<ArrayList<String>> sourceList) {

		ArrayList<ArrayList<String>> newList = new ArrayList<ArrayList<String>>();

		for (ArrayList<String> list : sourceList) {

			ArrayList<String> temp = new ArrayList<String>();

			for (String str : list)
				temp.add(str);

			newList.add(temp);
		}

		return newList;
	}

	private ArrayList<String> clone(ArrayList<String> sourceList) {

		ArrayList<String> newList = new ArrayList<String>();

		for (String str : sourceList)
			newList.add(str);

		return newList;
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

//	public static String hexToBinary(String hex) {
//
//		int i = 0;
//
//		i = Integer.parseInt(hex, 16);
//
//		String binary = Integer.toBinaryString(i);
//
//		return binary;
//	}
	
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
	
//	public static String intToBinary(String intStr) throws AssemblerException {
//
//		BigInteger bigInt =  new BigInteger(intStr, 16);		
//
//		String binary = bigInt.toString(2);
//
//		return binary;
//	}

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
