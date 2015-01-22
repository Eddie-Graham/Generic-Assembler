/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
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

	private boolean architecture, registers, mnemonicData, instructionFormat,
			adt, endian, minAddressableUnit;
	private boolean foundArchitecture, foundRegisters, foundMnemData,
			foundInsFormat, foundAdt, foundEndian, foundMinAdrUnit;
	private boolean atMnemName, working, atGlobalOpcodes, first, emptyLine,
			atMnemTypeHeader, atMnemType, abort;
	private boolean atLocalInsLabels, atLocalOpcodes, atLocalInsFormat;
	private boolean firstAdtEntry;
	private MnemonicData currentMnemonicData;
	private MnemType currentMnemType;

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

		architecture = false;
		registers = false;
		mnemonicData = false;
		instructionFormat = false;
		adt = false;
		endian = false;
		minAddressableUnit = false;
		
		foundArchitecture = false;
		foundRegisters = false;
		foundMnemData = false;
		foundInsFormat = false;
		foundAdt = false;
		foundEndian = false;
		foundMinAdrUnit = false;
		
		atMnemName = false;
		working = false;
		atGlobalOpcodes = false;
		first = false;		
		emptyLine = false;
		atMnemTypeHeader = false;
		atMnemType = false;
		abort = false;		
		atLocalInsLabels = false;
		atLocalOpcodes = false;		
		atLocalInsFormat = false;		

		firstAdtEntry = true;

		currentMnemonicData = null;
		currentMnemType = null;

		scanAssemblyFile(assemblyFile);
		scanSpecFile(specFile);
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
			System.out.println("File " + fileName + " not found.");
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
	 * </pre>
	 * 
	 * @param fileName - Specification file to be scanned
	 */
	private void scanSpecFile(String fileName) {

		Scanner inputFile = null;

		try {
			inputFile = new Scanner(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			System.out.println("File " + fileName + " not found.");
			System.exit(0);
		}

		int lineCounter = 0;

		while (inputFile.hasNextLine()) {

			String assemblyLine = inputFile.nextLine();
			lineCounter++;

			// Comments (;...) omitted
			String[] commentSplit = assemblyLine.split(";");
			assemblyLine = commentSplit[0];
			
			// Remove end whitespace
			assemblyLine = assemblyLine.replaceAll("\\s+$", ""); 

			try {
				scanLine(assemblyLine);
			} catch (AssemblerException e) {
				System.out.println("Exception at line " + lineCounter);
				System.out.println("Line: \"" + assemblyLine + "\"");
				System.out.println(e.getMessage());
				System.out.println();
			}
		}
		
		// If missing sections in specification file
		if (!(foundArchitecture && foundRegisters && foundMnemData
				&& foundInsFormat && foundAdt && foundEndian && foundMinAdrUnit)) {
			
			String missingSections = "";
			
			if(!foundArchitecture)
				missingSections += "\"architecture\" ";
			
			if(!foundRegisters)
				missingSections += "\"registers\" ";			
			
			if(!foundMnemData)
				missingSections += "\"mnemonicData\" ";			
			
			if(!foundInsFormat)
				missingSections += "\"instructionFormat\" ";			
			
			if(!foundAdt)
				missingSections += "\"adt\" ";			
			
			if(!foundEndian)
				missingSections += "\"endian\" ";
			
			if(!foundMinAdrUnit)
				missingSections += "\"minAddressableUnit\" ";
			
			missingSections = missingSections.trim();
			
			try {
				throw new AssemblerException("Section/s " + missingSections + " missing from specification file.");
			} catch (AssemblerException e) {
				System.out.println(e.getMessage());
			}
		}

		inputFile.close();
	}

	/**
	 * <pre>
	 * Scans line from specification file, determines what section in specification file 
	 * it belongs to and diverts it to relevant method for further analysis.
	 * </pre>
	 * 
	 * @param line - Line from specification file to be scanned
	 * @throws AssemblerException
	 */
	private void scanLine(String line) throws AssemblerException {

		// Section labels in specification file not case sensitive
		String lowerCaseLine = line.toLowerCase();

		if (lowerCaseLine.startsWith("architecture:"))
			setBooleanValues(true, false, false, false, false, false, false);

		else if (lowerCaseLine.startsWith("registers:"))
			setBooleanValues(false, true, false, false, false, false, false);

		else if (lowerCaseLine.startsWith("mnemonicdata:"))
			setBooleanValues(false, false, true, false, false, false, false);

		else if (lowerCaseLine.startsWith("instructionformat:"))
			setBooleanValues(false, false, false, true, false, false, false);

		else if (lowerCaseLine.startsWith("adt:"))
			setBooleanValues(false, false, false, false, true, false, false);

		else if (lowerCaseLine.startsWith("endian:"))
			setBooleanValues(false, false, false, false, false, true, false);
		
		else if (lowerCaseLine.startsWith("minaddressableunit:"))
			setBooleanValues(false, false, false, false, false, false, true);

		else if (architecture){
			
			foundArchitecture = true;
			analyseArchitecture(line);			
		}

		else if (registers){
			
			foundRegisters = true;
			analyseRegisters(line);			
		}

		else if (mnemonicData){
			
			foundMnemData = true;
			analyseMnemonicData(line);			
		}

		else if (instructionFormat){
			
			foundInsFormat = true;
			analyseInstructionFormat(line);			
		}

		else if (adt){
			
			foundAdt = true;
			analyseADT(line);			
		}

		else if (endian){
			
			foundEndian = true;
			analyseEndian(line);			
		}
		
		else if (minAddressableUnit) {

			foundMinAdrUnit = true;
			analyseMinAdrUnit(line);
		}
		
		else if (!(line.trim().length() == 0))
			throw new AssemblerException("Missing section header.");		
	}

	private void analyseMinAdrUnit(String line) throws AssemblerException {
		
		if (line.trim().length() == 0)
			return;
		
		line = line.trim();
		
		boolean legitMinAdrUnit = Pattern.matches("[0-9]+", line);

		if (!legitMinAdrUnit)
			throw new AssemblerException(
					"Min addressable unit syntax error, integer expected.");		
		
		int minAdrUnit = Integer.parseInt(line);
		
		data.setMinAdrUnit(minAdrUnit);		
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
	 * Analyses ADT line, expected format: 
	 * label : label label*
	 * </pre>
	 * 
	 * @param line - Adt line
	 * @throws AssemblerException if line syntax error
	 */
	private void analyseADT(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;
		
		line = line.trim();
		
		// Legit adt expression: 
		// (!(space|colon))+ space* colon space* (!(space|colon))+ (space* (!(space|colon))+)*
		boolean legitAdtExp = Pattern.matches(
				"[^\\s:]+\\s*:\\s*[^\\s:]+(\\s*[^\\s:]+)*", line);

		if (!legitAdtExp)
			throw new AssemblerException(
					"ADT syntax error, label : label label* expected.");

		ADT adt = data.getAdt();

		String[] colonSplit = line.split("(?=[:])|(?<=[:])");

		String label = colonSplit[0].trim();
		String terms = colonSplit[2].trim();

		// First entry must be root term
		if (firstAdtEntry) {

			String rootTerm = label;
			adt.setRootTerm(rootTerm);
			firstAdtEntry = false;
		}

		ArrayList<String> termsList = new ArrayList<String>();
		termsList.add(terms);

		// If label already exists in hash, then add to existing list, else put label in hash
		ArrayList<String> list = adt.getAdtHash().get(label);

		if (list != null)
			list.add(terms);

		else
			adt.getAdtHash().put(label, termsList);
	}

	/**<pre>
	 * Sets boolean values for which section of specification file is being analysed.
	 * </pre>
	 * 
	 * @param architecture
	 * @param registers
	 * @param mnemonicData
	 * @param instructionFormat
	 * @param adt
	 * @param endian
	 */
	private void setBooleanValues(boolean architecture, boolean registers,
			boolean mnemonicData, boolean instructionFormat, boolean adt,
			boolean endian, boolean minAddressableUnit) {

		this.architecture = architecture;
		this.registers = registers;
		this.mnemonicData = mnemonicData;
		this.instructionFormat = instructionFormat;
		this.adt = adt;
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

		line = line.trim();

		// Legit register expression:
		// (!space)+ space+ (!space)+
		boolean legitRegExp = Pattern.matches("[^\\s]+\\s+[^\\s]+", line);

		if (!legitRegExp)
			throw new AssemblerException(
					"Registers syntax error, regName regValue expected.");

		String[] tokens = line.split("\\s+");

		String regLabel = tokens[0];
		String regValue = tokens[1];

		char dataType = regValue.charAt(regValue.length() - 1);
		regValue = regValue.substring(0, regValue.length() - 1);

		// Binary
		if (dataType == 'B') {

			if (!isBinary(regValue))
				throw new AssemblerException("Registers syntax error, \""
						+ regValue + "\" is not binary.");

			data.getRegisterHash().put(regLabel, regValue);
		}

		// Hex
		else if (dataType == 'H') {

			try {
				regValue = Assembler.hexToBinary(regValue);
			} catch (NumberFormatException e) {
				throw new AssemblerException("Registers syntax error, \""
						+ regValue + "\" is not a valid hex number.");
			}

			data.getRegisterHash().put(regLabel, regValue);
		}

		// Integer
		else if (dataType == 'I') {

			try {
				regValue = Assembler.intToBinary(regValue);
			} catch (NumberFormatException e) {
				throw new AssemblerException("Registers syntax error, \""
						+ regValue + "\" is not a valid decimal number.");
			}

			data.getRegisterHash().put(regLabel, regValue);
		}

		else
			throw new AssemblerException(
					"Registers syntax error, last character of register value should indicate data type (\"B\", \"H\" or \"D\").");
	}

	/**
	 * <pre>
	 * Analyses mnemonic data, and diverts to relevant method for analysis, expected format:
	 * 
	 * mnemName
	 * 	globalOpcodes
	 * 
	 * 	mnemType
	 * 		insLabels
	 * 		localOpcodes
	 * 		insFormat
	 * 
	 * 	mnemType
	 * 		...
	 * 
	 * mnemName
	 * 	...
	 * </pre>
	 * 
	 * @param line - Mnemonic data line
	 * @throws AssemblerException if line format/syntax error
	 */
	private void analyseMnemonicData(String line) throws AssemblerException {

		if (line.trim().length() == 0) {

			if (abort)
				return;

			if (working)
				emptyLine = true;

			if (atMnemTypeHeader || atLocalInsLabels || atLocalOpcodes
					|| atLocalInsFormat) {

				abort = true;
				throw new AssemblerException(
						"Mnemonic data syntax error, line format error.");
			}

			return;
		}

		// New mnemonic (no whitespace at beginning)
		if (!line.startsWith(" ") && !line.startsWith("\t"))
			atMnemName = true;

		else if (abort)
			return;

		// Global opcodes (starts with tab and not passed an empty line)
		else if (Pattern.matches("\t[^\t\\s].*", line) && !emptyLine) {

			if (first) {

				atGlobalOpcodes = true;
				first = false;
			}
		}

		// Mnemonic type header (starts with tab and empty line passed)
		else if (Pattern.matches("\t[^\t\\s].*", line) && emptyLine) {

			if (!(atLocalInsLabels || atLocalOpcodes || atLocalInsFormat))
				atMnemTypeHeader = true;
		}

		// Mnemonic type data (starts with double tab and empty line passed)
		else if (Pattern.matches("\t\t[^\t\\s].*", line) && emptyLine)
			atMnemType = true;

		// Exception (indentation not recognised)
		else {

			abort = true;
			throw new AssemblerException(
					"Mnemonic data syntax error, line format error.");
		}

		if (atMnemName) {

			analyseMnemName(line);
			atMnemName = false;
		}

		// Exception (passed atMnemName but working flag not true)
		else if (!working) {

			abort = true;
			throw new AssemblerException(
					"Mnemonic data syntax error, line format error.");
		}

		else if (atGlobalOpcodes) {

			analyseGlobalOpcodes(line);
			atGlobalOpcodes = false;
		}

		else if (atMnemTypeHeader) {

			analyseMnemHeader(line);
			atMnemTypeHeader = false;
			atLocalInsLabels = true;
		}

		else if (atMnemType)
			analyseMnemType(line);

		// Exception
		else {

			abort = true;
			throw new AssemblerException(
					"Mnemonic data syntax error, line format error.");
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
					"Mnemonic data syntax error, mnemonic should only be single string (no spaces).");
		}

		currentMnemonicData = new MnemonicData();
		currentMnemonicData.setMnemonic(mnem);

		// Put mnemonic data in mnemonic hash table
		data.getMnemonicTable().put(mnem, currentMnemonicData);

		working = true;
	}
	
	/**
	 * <pre>
	 * Sets mnemonic header.
	 * </pre>
	 * 
	 * @param line - Mnemonic header line
	 */
	private void analyseMnemHeader(String line) {

		line = line.trim();

		currentMnemType = new MnemType();
		currentMnemType.setMnemType(line);

		currentMnemonicData.getMnemTypes().add(line);
		currentMnemonicData.getMnemTypeHash().put(line, currentMnemType);
	}
	
	/**
	 * <pre>
	 * Analyses mnemonic type data (instruction labels, local opcodes and instruction format).
	 * Expected format of local opcodes:
	 * codeLabel=codeValue(,codeLabel=codeValue)*	 
	 * </pre>
	 * 
	 * @param line - Mnemonic type line
	 * @throws AssemblerException if syntax or indentation error
	 */
	private void analyseMnemType(String line) throws AssemblerException {

		atMnemType = false;
		
		if (atLocalInsLabels) {

			line = line.trim();

			currentMnemType.setInsLabels(line);

			atLocalInsLabels = false;
			atLocalOpcodes = true;
		}

		else if (atLocalOpcodes) {

			line = line.trim();
			
			// If line is "--" then there are no local opcodes
			if (!line.equals("--")) {

				// Legit local opcode expression:
				// (!(space|equals|comma))+ space* equals space* (!(space|equals|comma))+ 
				// (space* comma space* (!(space|equals|comma))+ space* equals space* (!(space|equals|comma))+)*
				boolean legitLocalOpcodes = Pattern.matches(
								"[^\\s=,]+\\s*=\\s*[^\\s=,]+(\\s*,\\s*[^\\s=,]+\\s*=\\s*[^\\s=,]+)*", line);

				if (!legitLocalOpcodes) {

					abort = true;
					throw new AssemblerException(
							"Mnemonic data syntax error, codeLabel=codeValue(,codeLabel=codeValue)* expected.");
				}
				
				// Legit local opcodes so omit unnecessary spaces
				line = line.replaceAll("\\s+", "");

				String[] tokens = line.split(",");

				for (String token : tokens) {

					String[] elements = token.split("=");
					currentMnemType.getOpCodes().put(elements[0], elements[1]);
				}
			}

			atLocalOpcodes = false;
			atLocalInsFormat = true;
		}

		else if (atLocalInsFormat) {

			line = line.trim();

			String[] tokens = line.split("\\s+");

			for (String str : tokens)
				currentMnemType.getInstructionFormat().add(str);

			atLocalInsFormat = false;
			emptyLine = false;
		}
		
		// Exception
		else {

			abort = true;
			throw new AssemblerException(
					"Mnemonic data syntax error, indentation error.");
		}
	}

	/**
	 * <pre>
	 * Resets boolean values when about to work on a new mnemonic. 
	 * </pre>
	 */
	private void resetBooleanValues() {

		abort = false;

		atMnemName = false;
		atGlobalOpcodes = false;
		first = true;
		working = false;
		emptyLine = false;
		atMnemType = false;
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
	 * @param opcodes - Global opcodes line
	 * @throws AssemblerException if syntax error
	 */
	private void analyseGlobalOpcodes(String opcodes) throws AssemblerException {

		opcodes = opcodes.trim();

		// Legit global opcode expression:
		// (!(space|equals|comma))+ space* equals space* (!(space|equals|comma))+ 
		// (space* comma space* (!(space|equals|comma))+ space* equals space* (!(space|equals|comma))+)*
		boolean legitGlobalOpcodes = Pattern.matches(
						"[^\\s=,]+\\s*=\\s*[^\\s=,]+(\\s*,\\s*[^\\s=,]+\\s*=\\s*[^\\s=,]+)*", opcodes);

		if (!legitGlobalOpcodes) {

			abort = true;
			throw new AssemblerException(
					"Mnemonic data syntax error, codeLabel=codeValue(,codeLabel=codeValue)* expected.");
		}

		// Legit global opcodes so omit unnecessary spaces
		opcodes = opcodes.replaceAll("\\s+", "");

		String[] tokens = opcodes.split(",");

		for (String token : tokens) {

			String[] elements = token.split("=");
			currentMnemonicData.getGlobalOpCodes().put(elements[0], elements[1]);
		}
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
	private void analyseInstructionFormat(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		line = line.trim();

		// Legit instruction format:
		// (!(space|colon))+ space* colon space* (!(space|colon))+ openBracket 0-9+ closeBracket
		// (space* (!(space|colon))+ openBracket 0-9+ closeBracket)*		
		boolean legitInsFormat = Pattern.matches(
						"[^\\s:]+\\s*:\\s*[^\\s:]+\\([0-9]+\\)(\\s*[^\\s:]+\\([0-9]+\\))*", line);

		if (!legitInsFormat) {

			abort = true;
			throw new AssemblerException(
					"Instruction format syntax error, insName=opLabel(bitSize) (opLabel(bitSize))* expected.");
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
