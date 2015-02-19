/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * <pre>
 * This class parses both the specification and assembly files and stores the information
 * in the data source (DataSource.java).
 * </pre>
 * 
 * @author Eddie Graham
 * 
 */
public class FileParser {

	private DataSource data;

	private ArrayList<String> errorReport;

	private boolean architecture, registers, mnemonicData, instructionFormat,
			assemblyOpTree, endian, minAddressableUnit;
	private boolean foundArchitecture, foundRegisters, foundMnemData,
			foundInsFormat, foundAssemblyOpTree, foundEndian, foundMinAdrUnit;
	private boolean doneGlobalOpcodes, emptyLine, abort;
	private boolean foundFormatHeader, atLocalInsLabels, atLocalOpcodes, atLocalInsFormat;
	private boolean firstAssemblyOpTreeEntry;
	private MnemonicData currentMnemonicData;
	private MnemFormat currentMnemFormat;

	/**
	 * <pre>
	 * Constructor for class, initialises variables and calls methods which scan both files 
	 * ("scanAssemblyFile(assemblyFile)" and "scanSpecFile(specFile)").
	 * </pre>
	 * 
	 * @param specFile - Specification file to be scanned
	 * @param assemblyFile - Assembly file to be scanned
	 */
	public FileParser(String specFile, String assemblyFile) {

		data = new DataSource();

		errorReport = new ArrayList<String>();

		architecture = false;
		registers = false;
		mnemonicData = false;
		instructionFormat = false;
		assemblyOpTree = false;
		endian = false;
		minAddressableUnit = false;

		foundArchitecture = false;
		foundRegisters = false;
		foundMnemData = false;
		foundInsFormat = false;
		foundAssemblyOpTree = false;
		foundEndian = false;
		foundMinAdrUnit = false;

		doneGlobalOpcodes = false;
		emptyLine = false;
		abort = false;
		
		foundFormatHeader = true;
		atLocalInsLabels = false;
		atLocalOpcodes = false;
		atLocalInsFormat = false;

		firstAssemblyOpTreeEntry = true;

		currentMnemonicData = null;
		currentMnemFormat = null;
		
		scan(assemblyFile,specFile);
	}

	private void scan(String assemblyFile, String specFile) {
		
		scanAssemblyFile(assemblyFile);
		scanSpecFile(specFile);
		
		writeErrorReport();
		
	}

	private void writeErrorReport() {
		
		File file = null;

		try {			
			file = new File("error.txt");
			file.createNewFile();			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			FileWriter writer = new FileWriter(file);
			
			for(String line: errorReport)
				writer.write(line + "\n");		
			
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		
	}

	/**
	 * <pre>
	 * Scans assembly file (line by line) and stores raw data in data source.
	 * </pre>
	 * 
	 * @param fileName - Assembly file to be scanned
	 */
	private void scanAssemblyFile(String fileName) {

		Scanner inputFile = null;

		try {
			inputFile = new Scanner(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("Assembly file \"" + fileName + "\" not found.");
			System.exit(0);
		}

		while (inputFile.hasNextLine()) {

			String line = inputFile.nextLine();
			data.getAssemblyCode().add(line);
		}

		inputFile.close();
	}

	/**
	 * <pre>
	 * Scans specification file (line by line) and stores parsed data in data source.
	 * MnemonicData is last section scanned for error checking purposes.
	 * </pre>
	 * 
	 * @param fileName - Specification file to be scanned
	 */
	private void scanSpecFile(String fileName) {

		Scanner inputFile = null;
		Scanner inputFile2 = null;

		try {
			inputFile = new Scanner(new FileInputStream(fileName));
			inputFile2 = new Scanner(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("Specification file \"" + fileName + "\" not found.");
			System.exit(0);
		}

		int lineCounter = 0;
		
		while (inputFile.hasNextLine()) {

			String fullSpecLine = inputFile.nextLine();
			String specLine = fullSpecLine;
			
			lineCounter++;

			// Comments (;...) omitted
			String[] commentSplit = specLine.split(";");
			specLine = commentSplit[0];

			// Remove end whitespace
			specLine = specLine.replaceAll("\\s+$", "");

			try {
				scanLine(specLine);
			} catch (AssemblerException e) {
//				errorExists = true;
				data.setErrorInSpecFile(true);
				String error = getErrorMessage(lineCounter, fullSpecLine, e.getMessage());
				errorReport.add(error);

			}
		}
		
		inputFile.close();
		
		lineCounter = 0;
		String fullSpecLine = null;
		
		while (inputFile2.hasNextLine()) {

			fullSpecLine = inputFile2.nextLine();
			String specLine = fullSpecLine;
			
			lineCounter++;

			// Comments (;...) omitted
			String[] commentSplit = specLine.split(";");
			specLine = commentSplit[0];

			// Remove end whitespace
			specLine = specLine.replaceAll("\\s+$", "");

			try {
				scanLineForMnemonicData(specLine);
			} catch (AssemblerException e) {

				data.setErrorInSpecFile(true);
				String error = getErrorMessage(lineCounter, fullSpecLine, e.getMessage());
				errorReport.add(error);
				
				resetBooleanValues();
				abort = true;
				foundFormatHeader = true;

			}
		}
		
		if(!foundFormatHeader){			
			
			try {
				throw new AssemblerException(
						"MnemonicData error: Mnemonic format missing for mnemonic \"" 
						+ currentMnemonicData.getMnemonic() +"\".\n"
						+ getMnemDataErrorMessage());
			} catch (AssemblerException e) {
				data.setErrorInSpecFile(true);
				String error = getErrorMessage(lineCounter, fullSpecLine, e.getMessage());
				errorReport.add(error);
			}
		}
		
		inputFile2.close();

		// If missing sections in specification file
		if (!(foundArchitecture && foundRegisters && foundMnemData
				&& foundInsFormat && foundAssemblyOpTree && foundEndian && foundMinAdrUnit)) {

			String missingSections = "";

			if (!foundArchitecture)
				missingSections += "\"architecture\" ";

			if (!foundRegisters)
				missingSections += "\"registers\" ";

			if (!foundMnemData)
				missingSections += "\"mnemonicData\" ";

			if (!foundInsFormat)
				missingSections += "\"instructionFormat\" ";

			if (!foundAssemblyOpTree)
				missingSections += "\"assemblyOpTree\" ";

			if (!foundEndian)
				missingSections += "\"endian\" ";

			if (!foundMinAdrUnit)
				missingSections += "\"minAddressableUnit\" ";

			missingSections = missingSections.trim();

			try {
				throw new AssemblerException("Section/s " + missingSections
						+ " missing from specification file.");
			} catch (AssemblerException e) {
				data.setErrorInSpecFile(true);
				String error = e.getMessage();
				errorReport.add(error);
			}
		}
	}

	private String getErrorMessage(int lineCounter, String fullSpecLine, String message) {
		
		String msg = "------------------------------------------\n";
		msg += "Exception at line " + lineCounter + " :\n";
		msg += "\n";
		msg += fullSpecLine + "\n";
		msg += "\n";
		msg += "\n";
		msg += message + "\n\n";
		
		return msg;
	}

	private void scanLineForMnemonicData(String specLine) throws AssemblerException {
		
		String lowerCaseLine = specLine.toLowerCase();

		if (lowerCaseLine.startsWith("architecture:"))
			setBooleanValues(true, false, false, false, false, false, false);

		else if (lowerCaseLine.startsWith("registers:"))
			setBooleanValues(false, true, false, false, false, false, false);

		else if (lowerCaseLine.startsWith("mnemonicdata:"))
			setBooleanValues(false, false, true, false, false, false, false);

		else if (lowerCaseLine.startsWith("instructionformat:"))
			setBooleanValues(false, false, false, true, false, false, false);

		else if (lowerCaseLine.startsWith("assemblyoptree:"))
			setBooleanValues(false, false, false, false, true, false, false);

		else if (lowerCaseLine.startsWith("endian:"))
			setBooleanValues(false, false, false, false, false, true, false);

		else if (lowerCaseLine.startsWith("minaddressableunit:"))
			setBooleanValues(false, false, false, false, false, false, true);

		else if (architecture);

		else if (registers);			

		else if (mnemonicData)
			analyseMnemonicData(specLine);

		else if (instructionFormat);

		else if (assemblyOpTree);

		else if (endian);

		else if (minAddressableUnit);

		else if (!(specLine.trim().length() == 0))
			throw new AssemblerException("Missing section header.");
		
	}

	/**
	 * <pre>
	 * Scans line from specification file, determines what section in specification file 
	 * it belongs to and diverts it to relevant method for further analysis.
	 * </pre>
	 * 
	 * @param specLine - Line from specification file to be scanned
	 * @throws AssemblerException
	 */
	private void scanLine(String specLine) throws AssemblerException {

		// Section labels in specification file not case sensitive
		String lowerCaseLine = specLine.toLowerCase();

		if (lowerCaseLine.startsWith("architecture:"))
			setBooleanValues(true, false, false, false, false, false, false);

		else if (lowerCaseLine.startsWith("registers:"))
			setBooleanValues(false, true, false, false, false, false, false);

		else if (lowerCaseLine.startsWith("mnemonicdata:"))
			setBooleanValues(false, false, true, false, false, false, false);

		else if (lowerCaseLine.startsWith("instructionformat:"))
			setBooleanValues(false, false, false, true, false, false, false);

		else if (lowerCaseLine.startsWith("assemblyoptree:"))
			setBooleanValues(false, false, false, false, true, false, false);

		else if (lowerCaseLine.startsWith("endian:"))
			setBooleanValues(false, false, false, false, false, true, false);

		else if (lowerCaseLine.startsWith("minaddressableunit:"))
			setBooleanValues(false, false, false, false, false, false, true);

		else if (architecture)
			analyseArchitecture(specLine);

		else if (registers)
			analyseRegisters(specLine);

		else if (mnemonicData);

		else if (instructionFormat)
			analyseInstructionFormat(specLine);

		else if (assemblyOpTree)
			analyseAssemblyOpTree(specLine);

		else if (endian)
			analyseEndian(specLine);

		else if (minAddressableUnit)
			analyseMinAdrUnit(specLine);

		else if (!(specLine.trim().length() == 0))
			throw new AssemblerException("Missing section header.");
	}

	private void analyseMinAdrUnit(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		foundMinAdrUnit = true;

		line = line.trim();

		boolean legitMinAdrUnit = Pattern.matches("[0-9]+", line);

		if (!legitMinAdrUnit)
			throw new AssemblerException(
					"Min addressable unit syntax error, single integer expected.");

		int minAdrUnit = Integer.parseInt(line);

		data.setMinAdrUnit(minAdrUnit);

		minAddressableUnit = false;
	}

	/**
	 * <pre>
	 * Sets endian, "big" or "little" (not case sensitive).
	 * </pre>
	 * 
	 * @param line - Endian line
	 * @throws AssemblerException if endian does not equal "little" or "big"
	 */
	private void analyseEndian(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		foundEndian = true;

		line = line.trim();
		line = line.toLowerCase();

		if (line.equals("big"))
			data.setEndian("big");

		else if (line.equals("little"))
			data.setEndian("little");

		else
			throw new AssemblerException(
					"Endian not recognised, \"big\" or \"little\" expected.");

		endian = false;
	}

	/**
	 * <pre>
	 * Analyses AssemblyOpTree line, expected format: 
	 * label : label label*
	 * </pre>
	 * 
	 * @param line - assemblyOpTree line
	 * @throws AssemblerException if line syntax error
	 */
	private void analyseAssemblyOpTree(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		foundAssemblyOpTree = true;

		line = line.trim();

		// Legit assemblyOpTree expression:
		// (letters|numbers)+ space* colon space* (!(space|colon))+ (space*
		// (!(space|colon))+)*
		boolean legitAssemblyOpTreeExp = Pattern.matches(
				"[a-zA-Z0-9]+\\s*:\\s*[^\\s:]+(\\s*[^\\s:]+)*", line);

		if (!legitAssemblyOpTreeExp)
			throw new AssemblerException(
					"AssemblyOpTree syntax error.");	//TODO

		AssemblyOpTree assemblyOpTree = data.getAssemblyOpTree();

		String[] assemblyOpTreeTokens = line.split("[^A-Za-z0-9]+");

		for (String assemblyOpTreeToken : assemblyOpTreeTokens)
			assemblyOpTree.getAssemblyOpTreeTokens().add(assemblyOpTreeToken);

		String[] colonSplit = line.split("(?=[:])|(?<=[:])");

		String label = colonSplit[0].trim();
		String terms = colonSplit[2].trim();

		// First entry must be root term
		if (firstAssemblyOpTreeEntry) {

			String rootToken = label;
			assemblyOpTree.setRootToken(rootToken);
			firstAssemblyOpTreeEntry = false;
		}

		ArrayList<String> termsList = new ArrayList<String>();
		termsList.add(terms);

		// If label already exists in hash, then add to existing list, else put
		// label in hash
		ArrayList<String> list = assemblyOpTree.getAssemblyOpTreeHash().get(label);

		if (list != null)
			list.add(terms);

		else
			assemblyOpTree.getAssemblyOpTreeHash().put(label, termsList);
	}

	/**
	 * <pre>
	 * Sets boolean values for which section of specification file is being analysed.
	 * </pre>
	 * 
	 * @param architecture
	 * @param registers
	 * @param mnemonicData
	 * @param instructionFormat
	 * @param assemblyOpTree
	 * @param endian
	 */
	private void setBooleanValues(boolean architecture, boolean registers,
			boolean mnemonicData, boolean instructionFormat, boolean assemblyOpTree,
			boolean endian, boolean minAddressableUnit) {

		this.architecture = architecture;
		this.registers = registers;
		this.mnemonicData = mnemonicData;
		this.instructionFormat = instructionFormat;
		this.assemblyOpTree = assemblyOpTree;
		this.endian = endian;
		this.minAddressableUnit = minAddressableUnit;
	}

	/**
	 * <pre>
	 * Sets architecture name.
	 * </pre>
	 * 
	 * @param line - Architecture line
	 */
	private void analyseArchitecture(String line) {

		if (line.trim().length() == 0)
			return;

		foundArchitecture = true;

		data.setArchitecture(line.trim());

		architecture = false;
	}

	/**
	 * <pre>
	 * Sets register name and value (value is converted to binary before it is stored
	 * in data source), expected format: 
	 * regName regValue 
	 * RegValue must end in "B", "H" or "D" to indicate data type. 
	 * "B" means value is binary.
	 * "H" means value is hexadecimal.
	 * "I" means value is an integer.
	 * </pre>
	 * 
	 * @param line - Register line
	 * @throws AssemblerException if line syntax error or register value not valid
	 */
	private void analyseRegisters(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		foundRegisters = true;

		line = line.trim();

		// Legit register expression:
		// (!space)+ space+ (!space)+
		boolean legitRegExp = Pattern.matches("[^\\s]+\\s+[^\\s]+", line);

		if (!legitRegExp)
			throw new AssemblerException(
					"Register syntax error, <registerName> <value><B/H/I> expected. For example:\n" +
					"\n" +
					"eax    000B");	

		String[] tokens = line.split("\\s+");

		String regLabel = tokens[0];
		String valueAndType = tokens[1];

		char dataType = valueAndType.charAt(valueAndType.length() - 1);
		String regValue = valueAndType.substring(0, valueAndType.length() - 1);

		// Binary
		if (dataType == 'B') {
			
			if(regValue.isEmpty())
				throw new AssemblerException("Register syntax error, <registerName> <value><B/H/I> expected.\n" +
						"Binary value missing.\n");

			if (!isBinary(regValue))
				throw new AssemblerException("\"" + regValue + "\" is not a valid binary value.");

			data.getRegisterHash().put(regLabel, regValue);
		}

		// Hex
		else if (dataType == 'H') {
			
			if(regValue.isEmpty())
				throw new AssemblerException("Register syntax error, <registerName> <value><B/H/I> expected.\n" +
						"Hex value missing.\n");

			try {
				regValue = Assembler.hexToBinary(regValue);
			} catch (NumberFormatException e) {
				throw new AssemblerException("\"" + regValue + "\" is not a valid hex value.");
			}

			data.getRegisterHash().put(regLabel, regValue);
		}

		// Integer
		else if (dataType == 'I') {
			
			if(regValue.isEmpty())
				throw new AssemblerException("Register syntax error, <registerName> <value><B/H/I> expected.\n" +
						"Integer value missing.\n");

			try {
				regValue = Assembler.intToBinary(regValue);
			} catch (NumberFormatException e) {
				throw new AssemblerException("\"" + regValue + "\" is not a valid integer.");
			}

			data.getRegisterHash().put(regLabel, regValue);
		}

		else
			throw new AssemblerException(
					"Register syntax error, <registerName> <value><B/H/I> expected.\n" +
					"Last character of second string (\"" + valueAndType + "\") should indicate data type (\"B\", \"H\" or \"I\").");
	}

	/**
	 * <pre>
	 * Analyses mnemonic data, and diverts line to relevant method for analysis, expected line format:
	 * 
	 * <mnemonicName>
	 * 	<globalOpcodes>
	 * 
	 * 	<mnemFormat>
	 * 		<instructionFieldLabels>
	 * 		<localOpcodes>
	 * 		<instructionFormat>
	 * 
	 * 	<mnemFormat>
	 * 		...
	 * 
	 * <mnemonicName>
	 * 	...
	 * </pre>
	 * 
	 * @param line - Mnemonic data line
	 * @throws AssemblerException if line format/syntax error
	 */	
	private void analyseMnemonicData(String line) throws AssemblerException {
		
		try{
			currentMnemonicData.getRawLines().add(line);
		} catch(NullPointerException e){}

		if (line.trim().length() == 0) {
			
			emptyLine = true;

			if (abort)
				return;			

			if (atLocalInsLabels || atLocalOpcodes || atLocalInsFormat)
				checkWhatLineExpected();			

			return;
		}

		foundMnemData = true;

		// New mnemonic (no whitespace at beginning)
		if (Pattern.matches("[^\t\\s].*", line) && emptyLine && foundFormatHeader && !atLocalInsLabels && !atLocalOpcodes && !atLocalInsFormat){
			
			emptyLine = false;
			
			analyseMnemName(line);
			
			currentMnemonicData.getRawLines().add(line);			
		}			

		else if (abort)
			return;
		
		else if(currentMnemonicData == null){
			
			abort = true;
			
			throw new AssemblerException("MnemonicData error: Mnemonic name not declared.");
		}

		// Global opcodes (starts with tab and not passed an empty line)
		else if (Pattern.matches("\t[^\t\\s].*", line) && !emptyLine && !doneGlobalOpcodes) {

			emptyLine = false;
			
			analyseGlobalOpcodes(line);
			
			doneGlobalOpcodes = true;
		}

		// Mnemonic format header (starts with tab and empty line passed)
		else if (Pattern.matches("\t[^\t\\s].*", line) && emptyLine) {
			
			emptyLine = false;

			analyseMnemFormatHeader(line);
			
			atLocalInsLabels = true;
			foundFormatHeader = true;			
			
			try{
				currentMnemFormat.addToRawLineString(line);
			} catch(NullPointerException e){}
		}

		// Mnemonic format data (starts with double tab and empty line passed)
		else if (Pattern.matches("\t\t[^\t\\s].*", line) && (atLocalInsLabels || atLocalOpcodes || atLocalInsFormat)){
			
			try{
				currentMnemFormat.addToRawLineString(line);
			} catch(NullPointerException e){}

			analyseMnemFormat(line);
		}

		// Exception (indentation not recognised)
		else {

			abort = true;
			
			checkWhatLineExpected();
		}
	}

	private void checkWhatLineExpected() throws AssemblerException {
		
		if(!foundFormatHeader){
			
			abort = true;
			
			throw new AssemblerException(
					"MnemonicData error: Mnemonic format missing for mnemonic \"" 
					+ currentMnemonicData.getMnemonic() +"\".\n"
					+ getMnemDataErrorMessage());
		}
		
		else if (atLocalInsLabels){
			
			abort = true;
			
			throw new AssemblerException(
					"MnemonicData error: Line format/indentation error, instruction field labels line expected.\n"
					+ getMnemDataErrorMessage());
		}
		
		else if (atLocalOpcodes){
			
			abort = true;
			
			throw new AssemblerException(
					"MnemonicData error: Line format/indentation error, local opcodes line expected.\n"
					+ getMnemDataErrorMessage());
		}
		
		else if (atLocalInsFormat){
			
			abort = true;
			
			throw new AssemblerException(
					"MnemonicData error: Line format/indentation error, instruction format line expected.\n"
					+ getMnemDataErrorMessage());
		}	
		
		else{
			
			abort = true;
			
			throw new AssemblerException(
					"MnemonicData error: Line format error, empty line expected.\n"
					+ getMnemDataErrorMessage());
		}		
	}

	/**
	 * <pre>
	 * Sets mnemonic name, expected format:
	 * mnemName
	 * </pre>
	 * 
	 * @param line - Mnemonic name line
	 * @throws AssemblerException if syntax error
	 */
	private void analyseMnemName(String line) throws AssemblerException {

		// Reset boolean values for new mnemonic
		resetBooleanValues();

		String mnem = line.trim();

		// Legit mnemonic name expression:
		// (!space)+
		boolean legitMnemName = Pattern.matches("[^\\s]+", mnem);

		if (!legitMnemName) {

			abort = true;
			throw new AssemblerException(
					"MnemonicData error: Mnemonic name should only be single token (no spaces).");
		}

		currentMnemonicData = new MnemonicData();
		currentMnemonicData.setMnemonic(mnem);

		// Put mnemonic data in mnemonic hash table
		data.getMnemonicTable().put(mnem, currentMnemonicData);
	}

	/**
	 * <pre>
	 * Sets mnemonic header.
	 * </pre>
	 * 
	 * @param line - Mnemonic header line
	 * @throws AssemblerException if format token does not exist in AssemblyOpTree
	 */
	private void analyseMnemFormatHeader(String line) throws AssemblerException {

		line = line.trim();

		String[] formatTokens = line.split("[^A-Za-z0-9]+");

		ArrayList<String> assemblyOpTreeTokens = data.getAssemblyOpTree().getAssemblyOpTreeTokens();

		for (String formatToken : formatTokens) {

			if (!assemblyOpTreeTokens.contains(formatToken)){
				abort = true;
				throw new AssemblerException("MnemonicData error: Mnemonic format token \"" + formatToken + "\" not found in AssemblyOpTree.");
			}
		}

		currentMnemFormat = new MnemFormat();
		currentMnemFormat.setMnemFormat(line);
		currentMnemonicData.getMnemFormats().add(line);
		currentMnemonicData.getMnemFormatHash().put(line, currentMnemFormat);
	}

	/**
	 * <pre>
	 * Analyses mnemonic format data (instruction labels, local opcodes and instruction format).
	 * Expected format of local opcodes:
	 * codeLabel=codeValue(,codeLabel=codeValue)*
	 * </pre>
	 * 
	 * @param line - Mnemonic format line
	 * @throws AssemblerException if syntax or indentation error
	 */
	private void analyseMnemFormat(String line) throws AssemblerException {

		if (atLocalInsLabels) {

			line = line.trim();

			currentMnemFormat.setInsFieldLabels(line);

			atLocalInsLabels = false;
			atLocalOpcodes = true;
		}

		else if (atLocalOpcodes) {

			line = line.trim();

			// If line is "--" then there are no local opcodes
			if (!line.equals("--")) {

				// Legit local opcode expression:
				// (!(space|equals|comma))+ space* equals space*
				// (!(space|equals|comma))+
				// (space* comma space* (!(space|equals|comma))+ space* equals
				// space* (!(space|equals|comma))+)*
				boolean legitLocalOpcodes = Pattern.matches(
								"[A-Za-z0-9]+\\s*=\\s*[A-Za-z0-9]+(\\s*,\\s*[A-Za-z0-9]+\\s*=\\s*[A-Za-z0-9]+)*",
								line);

				if (!legitLocalOpcodes) {

					abort = true;
					throw new AssemblerException(
							"MnemonicData error: Local opcodes syntax error," +
							"\n<fieldName>=<value> or \"--\" (if no local opcodes) expected.");
				}

				// Legit local opcodes so omit unnecessary spaces
				line = line.replaceAll("\\s+", "");

				String[] tokens = line.split(",");

				for (String token : tokens) {

					String[] elements = token.split("=");
					currentMnemFormat.getOpcodes().put(elements[0], elements[1]);
				}
			}

			atLocalOpcodes = false;
			atLocalInsFormat = true;
		}

		else if (atLocalInsFormat) {

			line = line.trim();

			String[] tokens = line.split("\\s+");

			for (String str : tokens)
				currentMnemFormat.getInstructionFormat().add(str);

			endOfFormatErrorCheck();

			atLocalInsFormat = false;
		}
	}
	
	private String getMnemDataErrorMessage(){
		
		ArrayList<String> rawLines = currentMnemonicData.getRawLines();
		
		int noOfLines = rawLines.size();
		int maxLineLength = 0;
		
		String msg = "";		
		
		for(String str: rawLines){	
			
			str = str.replaceAll("\\s+$", "");			
			
			if(str.length() > maxLineLength)
				maxLineLength = str.length();
			
			msg += "\n" + str;
		}	
		
		int lastLineLength = rawLines.get(noOfLines-1).replaceAll("\\s+$", "").length();
		int noOfSpaces = 0;
		String whiteSpace = "\t\t\t";
		
		if(lastLineLength == 0)
			noOfSpaces = maxLineLength;	
		
		else
			noOfSpaces = maxLineLength - lastLineLength;
		
		for(; noOfSpaces > 0; noOfSpaces -= 1)
			whiteSpace += " ";
		
		msg += whiteSpace + "<---";		
		
		return msg;		
	}

	/**
	 * <pre>
	 * Error checking of mnemonic data
	 * </pre>
	 * 
	 * @throws AssemblerException
	 */
	private void endOfFormatErrorCheck() throws AssemblerException {

		ArrayList<String> instructionFormat = currentMnemFormat.getInstructionFormat();

		for (String instruction : instructionFormat) {

			InstructionFormatData insFormat = data.getInstructionFormat().get(instruction);

			if (insFormat == null) {
				
				abort = true;

				throw new AssemblerException(
						currentMnemFormat.getRawLinesString()
								+ "\nMnemonicData error: Instruction \"" + instruction
								+ "\" does not exist in instructionFormat.");
			}

			ArrayList<String> instructions = insFormat.getOperands();

			for (String field : instructions) {

				int bits = insFormat.getOperandBitHash().get(field);

				if (currentMnemonicData.getGlobalOpCodes().get(field) != null) {

					String opcode = currentMnemonicData.getGlobalOpCodes().get(field);
					int noOfBits = opcode.length();

					if (noOfBits > bits){
						
						abort = true;
						
						throw new AssemblerException(
								currentMnemFormat.getRawLinesString()+
										"\nMnemonicData error: Field \""
										+ field
										+ "\" in \""
										+ currentMnemonicData.getMnemonic()
										+ "\" global opcodes ("
										+ currentMnemonicData.getRawGlobalOpcodesString()
										+ ")\nexceeds expected " + bits
										+ " bits in instruction format \""
										+ instruction + "\" ("
										+ insFormat.getRawLineString() + ").");
						
					}
				}

				else if (currentMnemFormat.getOpcodes().get(field) != null) {

					String opcode = currentMnemFormat.getOpcodes().get(field);
					int noOfBits = opcode.length();

					if (noOfBits > bits){
						
						abort = true;
						
						throw new AssemblerException(
								currentMnemFormat.getRawLinesString()
										+ "\nMnemonicData error: Field \""
										+ field
										+ "\" in local opcodes for \"" 
										+ currentMnemonicData.getMnemonic()
										+ "\" format \""
										+ currentMnemFormat.getMnemFormat()
										+ "\""
										+ "\nexceeds expected "
										+ bits
										+ " bits in instruction format \""
										+ instruction + "\" ("
										+ insFormat.getRawLineString() + ").");
					}
				}

				else if (existsInInsFieldLabels(currentMnemFormat.getInsFieldLabels(), field));

				else {
					
					abort = true;

					throw new AssemblerException(
									"Field \""
									+ field
									+ "\" in instruction format \""
									+ instruction
									+ "\" ("
									+ insFormat.getRawLineString()
									+ ")\nnot found within global \""
									+ currentMnemonicData.getMnemonic()
									+ "\" opcodes ("
									+ currentMnemonicData.getRawGlobalOpcodesString()
									+ ") or in \""
									+ currentMnemonicData.getMnemonic()
									+ "\" format \""
									+ currentMnemFormat.getMnemFormat()
									+ "\":\n\n"
									+ currentMnemFormat.getRawLinesString());
				}
			}
		}
	}

	private boolean existsInInsFieldLabels(String insFieldLabels, String field) {

		String[] insFieldLabelTokens = insFieldLabels.split("[^a-zA-Z0-9]+");

		for (String insFieldLabel : insFieldLabelTokens) {

			if (insFieldLabel.equals(field))
				return true;
		}

		return false;
	}

	/**
	 * <pre>
	 * Resets boolean values when about to work on a new mnemonic.
	 * </pre>
	 */
	private void resetBooleanValues() {

		abort = false;

		doneGlobalOpcodes = false;
		emptyLine = false;
		
		foundFormatHeader = false;
		atLocalInsLabels = false;
		atLocalOpcodes = false;
		atLocalInsFormat = false;

		currentMnemonicData = null;
	}

	/**
	 * <pre>
	 * Sets global opcodes, expected format:
	 * codeLabel=codeValue(,codeLabel=codeValue)*
	 * </pre>
	 * 
	 * @param line - Global opcodes line
	 * @throws AssemblerException if syntax error
	 */
	private void analyseGlobalOpcodes(String line) throws AssemblerException {

		line = line.trim();

		// Legit global opcode expression:
		// (!(space|equals|comma))+ space* equals space*
		// (!(space|equals|comma))+
		// (space* comma space* (!(space|equals|comma))+ space* equals space*
		// (!(space|equals|comma))+)*
		boolean legitGlobalOpcodes = Pattern
				.matches("[^\\s=,]+\\s*=\\s*[^\\s=,]+(\\s*,\\s*[^\\s=,]+\\s*=\\s*[^\\s=,]+)*",
						line);

		if (!legitGlobalOpcodes) {

			abort = true;
			throw new AssemblerException(
					"MnemonicData error: Global opcodes syntax error, <fieldName>=<value> expected.");
		}

		// Legit global opcodes so omit unnecessary spaces
		line = line.replaceAll("\\s+", "");

		String[] tokens = line.split(",");

		for (String token : tokens) {

			String[] elements = token.split("=");
			currentMnemonicData.getGlobalOpCodes().put(elements[0], elements[1]);
		}

		currentMnemonicData.setRawGlobalOpcodesString(line);
	}

	/**
	 * <pre>
	 * Sets instruction formats, expected format:
	 * insName=opLabel(bitSize) (opLabel(bitSize))*
	 * </pre>
	 * 
	 * @param line - Instruction format line
	 * @throws AssemblerException if syntax error
	 */
	private void analyseInstructionFormat(String line)
			throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		foundInsFormat = true;

		line = line.trim();

		// Legit instruction format:
		// (!(space|colon))+ space* colon space* (letters|numbers)+ openBracket
		// 0-9+ closeBracket
		// (space* (letter|numbers)+ openBracket 0-9+ closeBracket)*
		boolean legitInsFormat = Pattern
				.matches("[^\\s:]+\\s*:\\s*[a-zA-Z0-9]+\\([0-9]+\\)(\\s*[a-zA-Z0-9]+\\([0-9]+\\))*",
						line);

		if (!legitInsFormat) {

			abort = true;
			throw new AssemblerException(
					"Instruction format syntax error, <instructionName> : <fieldName>(<bitLength>) expected. For example:\n" +
					"\n" +
					"opcode : op(6) d(1) s(1)");
		}

		InstructionFormatData insF = new InstructionFormatData();

		String[] tokens = line.split(":");

		String insName = tokens[0].trim();
		String operands = tokens[1].trim();

		String[] operandTokens = operands.split("\\s+");

		for (String operand : operandTokens) {

			String[] tokenTerms = operand.split("\\(|\\)");

			String op = tokenTerms[0];
			int bitSize = Integer.parseInt(tokenTerms[1]);

			insF.getOperands().add(op);
			insF.getOperandBitHash().put(op, bitSize);
		}

		insF.setInstructionName(insName);
		insF.setRawLineString(line.trim());
		data.getInstructionFormat().put(insName, insF);
	}

	/**
	 * <pre>
	 * Returns true if string represents a binary number (0's and 1's), else false.
	 * </pre>
	 * 
	 * @param s - String
	 * @return true if binary, else false
	 */
	private boolean isBinary(String s) {

		String pattern = "[0-1]*$";

		if (s.matches(pattern))
			return true;

		return false;
	}

	/**
	 * <pre>
	 * Get data source that this file parser created.
	 * </pre>
	 * 
	 * @return data
	 */
	public DataSource getData() {
		return data;
	}
}
