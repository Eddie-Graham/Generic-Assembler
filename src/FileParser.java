/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
	private boolean architectureDeclared, registersDeclared, mnemDataDeclared,
			insFormatDeclared, assemblyOpTreeDeclared, endianDeclared,
			minAdrUnitDeclared;
	private boolean doneGlobalOpcodes, emptyLine, abortMnem;
	private boolean foundFormatHeader, atOperandFieldEncodings, atLocalFieldEncodings,
			atInsFormat;
	private boolean firstAssemblyOpTreeEntry;
	private String rootOpTreeEntry;
	private Mnemonic currentMnemonic;
	private OperandFormat currentMnemFormat;

	/**
	 * <pre>
	 * Constructor for class, initialises variables and calls methods which scan both files 
	 * ("scanAssemblyFile(assemblyFile)" and "scanSpecFile(specFile)").
	 * </pre>
	 * 
	 * @param specFile
	 * @param assemblyFile
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
		
		architectureDeclared = false;
		registersDeclared = false;
		mnemDataDeclared = false;
		insFormatDeclared = false;
		assemblyOpTreeDeclared = false;
		endianDeclared = false;
		minAdrUnitDeclared = false;

		doneGlobalOpcodes = false;
		emptyLine = true;
		abortMnem = false;

		foundFormatHeader = true;
		atOperandFieldEncodings = false;
		atLocalFieldEncodings = false;
		atInsFormat = false;

		firstAssemblyOpTreeEntry = true;
		rootOpTreeEntry = "";

		currentMnemonic = null;
		currentMnemFormat = null;

		scan(assemblyFile, specFile);
	}

	private void scan(String assemblyFile, String specFile) {

		scanAssemblyFile(assemblyFile);		
		scanSpecFile(specFile);
		
		if(!errorReport.isEmpty()){			
			ArrayList<String> empty = new ArrayList<String>();
			empty.add("Error in specification file, see \"spec_error_report.text\".");			
			Assembler.writeLinesToFile("object_code.txt", empty);
			Assembler.writeLinesToFile("spec_error_report.txt", errorReport);			
			System.exit(0);
		}
		
		else
			errorReport.add("No errors found within specification file.");
		
		Assembler.writeLinesToFile("spec_error_report.txt", errorReport);
	}

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

	private void scanSpecFile(String fileName) {

		// Two scanners as file is parsed twice. MnemonicData is analysed last for
		// error checking
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

			try {
				specLine = commentSplit[0];
			} catch (ArrayIndexOutOfBoundsException e) {
				specLine = "";
			}

			// Remove end whitespace
			specLine = specLine.replaceAll("\\s+$", "");

			try {
				scanLine(specLine, false, false, true, false, false, false, false, false);
			} catch (AssemblerException e) {
				String error = getErrorMessage(lineCounter, fullSpecLine, e.getMessage());
				errorReport.add(error);
			}
		}
		
		if (!(foundArchitecture && foundRegisters && foundInsFormat
				&& foundAssemblyOpTree && foundEndian && foundMinAdrUnit)) {
			String missingSections = "";
			if (!foundArchitecture)
				missingSections += "\"architecture\" ";
			if (!foundRegisters)
				missingSections += "\"registers\" ";
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
				String error = e.getMessage();
				errorReport.add(error);
			}
		}		
		
		if(!errorReport.isEmpty()){			
			ArrayList<String> empty = new ArrayList<String>();
			empty.add("Error in specification file, see \"spec_error_report.text\".");			
			Assembler.writeLinesToFile("spec_error_report.txt", errorReport);
			Assembler.writeLinesToFile("object_code.txt", empty);
			System.exit(0);			
		}

		inputFile.close();

		lineCounter = 0;
		String fullSpecLine = null;		
		resetDeclarationBooleans();

		while (inputFile2.hasNextLine()) {
			fullSpecLine = inputFile2.nextLine();
			String specLine = fullSpecLine;
			lineCounter++;
			String[] commentSplit = specLine.split(";");

			try {
				specLine = commentSplit[0];
			} catch (ArrayIndexOutOfBoundsException e) {
				specLine = "";
			}

			specLine = specLine.replaceAll("\\s+$", "");

			try {
				scanLine(specLine, true, true, false, true, true, true, true, true);
			} catch (AssemblerException e) {
				String error = getErrorMessage(lineCounter, fullSpecLine, e.getMessage());
				errorReport.add(error);
				resetBooleanValues();
				abortMnem = true;
				foundFormatHeader = true;
			}
		}
		
		inputFile2.close();
		
		// Run one last time with empty line to catch any error at end of mnemonic data section
		try {
			scanLine("", true, true, false, true, true, true, true, true);

		} catch (AssemblerException e) {
			String error = getErrorMessage(lineCounter, fullSpecLine, e.getMessage());
			errorReport.add(error);
			resetBooleanValues();
			abortMnem = true;
			foundFormatHeader = true;
		}

		// If missing sections in specification file
		if (!foundMnemData) {
			String missingSections = "\"mnemonicData\"";
			try {				
				throw new AssemblerException("Section/s " + missingSections		
						+ " missing from specification file.");				
			} catch (AssemblerException e) {				
				String error = e.getMessage();
				errorReport.add(error);
			}
		}
	}

	private void resetDeclarationBooleans() {
		
		architectureDeclared = false;
		registersDeclared = false;
		insFormatDeclared = false;
		assemblyOpTreeDeclared = false;
		endianDeclared = false;
		minAdrUnitDeclared = false;
		mnemDataDeclared = false;
	}

	private String getErrorMessage(int lineCounter, String fullSpecLine, String message) {

		String msg = "------------------------------------------\n";
		msg += "Exception at line " + lineCounter + " :\n";
		msg += "\n";
		msg += fullSpecLine + "\n";
		msg += "------------------------------------------\n";
		msg += "\n";
		msg += message + "\n\n";

		return msg;
	}
	
	private void scanLine(String specLine, boolean ignoreArchitecture,
			boolean ignoreRegisters, boolean ignoreMnemonicData,
			boolean ignoreInstructionFormat, boolean ignoreAssemblyOpTree,
			boolean ignoreEndian, boolean ignoreMinAddressableUnit,
			boolean ignoreInstructionSize) throws AssemblerException {

		// Section labels in specification file not case sensitive
		String lowerCaseLine = specLine.toLowerCase();

		if (lowerCaseLine.startsWith("architecture:")){			
			if(architectureDeclared)
				throw new AssemblerException("Architecture section already declared.");			
			architectureDeclared = true;			
			setBooleanValues(true, false, false, false, false, false, false);
		}

		else if (lowerCaseLine.startsWith("registers:")){			
			if(registersDeclared)
				throw new AssemblerException("Registers section already declared.");			
			registersDeclared = true;			
			setBooleanValues(false, true, false, false, false, false, false);
		}

		else if (lowerCaseLine.startsWith("mnemonicdata:")){			
			if(mnemDataDeclared)
				throw new AssemblerException("MnemonicData section already declared.");			
			mnemDataDeclared = true;			
			setBooleanValues(false, false, true, false, false, false, false);
		}

		else if (lowerCaseLine.startsWith("instructionformat:")){			
			if(insFormatDeclared)
				throw new AssemblerException("Architecture section already declared.");			
			insFormatDeclared = true;		
			setBooleanValues(false, false, false, true, false, false, false);
		}

		else if (lowerCaseLine.startsWith("assemblyoptree:")){			
			if(assemblyOpTreeDeclared)
				throw new AssemblerException("AssemblyOpTree section already declared.");			
			assemblyOpTreeDeclared = true;		
			setBooleanValues(false, false, false, false, true, false, false);
		}

		else if (lowerCaseLine.startsWith("endian:")){			
			if(endianDeclared)
				throw new AssemblerException("Endian section already declared.");			
			endianDeclared = true;		
			setBooleanValues(false, false, false, false, false, true, false);
		}

		else if (lowerCaseLine.startsWith("minaddressableunit:")){			
			if(minAdrUnitDeclared)
				throw new AssemblerException("Architecture section already declared.");			
			minAdrUnitDeclared = true;			
			setBooleanValues(false, false, false, false, false, false, true);
		}

		else if (architecture){			
			if(!ignoreArchitecture)
				analyseArchitecture(specLine);
		}

		else if (registers){			
			if(!ignoreRegisters)
				analyseRegisters(specLine);
		}

		else if (mnemonicData){			
			if(!ignoreMnemonicData)
				analyseMnemonicData(specLine);
		}

		else if (instructionFormat){			
			if(!ignoreInstructionFormat)
				analyseInstructionFormat(specLine);
		}

		else if (assemblyOpTree){			
			if(!ignoreAssemblyOpTree)
				analyseAssemblyOpTree(specLine);
		}

		else if (endian){			
			if(!ignoreEndian)
				analyseEndian(specLine);
		}

		else if (minAddressableUnit){			
			if(!ignoreMinAddressableUnit)
				analyseMinAddressableUnit(specLine);	
		}
		
		else{			
			if(specLine.trim().length() != 0)
				throw new AssemblerException("No section header.");
		}			
	}

	private void analyseMinAddressableUnit(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;
		
		if(foundMinAdrUnit)
			throw new AssemblerException("MinAddressableUnit error: Minimum addressable unit already specified.");

		foundMinAdrUnit = true;
		line = line.trim();

		boolean legitMinAdrUnit = Pattern.matches("[0-9]+", line);

		if (!legitMinAdrUnit)
			throw new AssemblerException(
					"MinAddressableUnit error: Syntax error, single integer expected.");

		int minAdrUnit = Integer.parseInt(line);
		
		if(minAdrUnit <= 0)
			throw new AssemblerException("MinAddressableUnit error: Minimum addressable unit must be greater than 0.");

		data.setMinAdrUnit(minAdrUnit);
	}

	private void analyseEndian(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		if(foundEndian)
			throw new AssemblerException("Endian error: Endian already specified.");
		
		foundEndian = true;
		line = line.trim();
		line = line.toLowerCase();

		if (line.equals("big"))
			data.setEndian("big");

		else if (line.equals("little"))
			data.setEndian("little");

		else
			throw new AssemblerException(
					"Endian error: Endian not recognised, \"big\" or \"little\" expected.");
	}
	
	private void analyseAssemblyOpTree(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		foundAssemblyOpTree = true;
		line = line.trim();		
		
		boolean legitAssemblyOpTreeExp = Pattern.matches("[^:]+:[^:]+", line);

		if (!legitAssemblyOpTreeExp)
			throw new AssemblerException("AssemblyOpTree error: Line syntax error, expected format <node> : <expression>");
		
		AssemblyOpTree assemblyOpTree = data.getAssemblyOpTree();
		String[] assemblyOpTreeTokens = line.split("[^A-Za-z0-9]+");

		for (String assemblyOpTreeToken : assemblyOpTreeTokens)
			assemblyOpTree.getAssemblyOpTreeTokens().add(assemblyOpTreeToken);

		String[] colonSplit = line.split(":");
		String node = colonSplit[0].trim();
		
		if(node.equals("LABEL") || node.equals("INT") || node.equals("HEX"))
			throw new AssemblerException("AssemblyOpTree error: Node can not be keyword \"LABEL\", \"INT\" or \"HEX\".");
		
		boolean legitNode = Pattern.matches("[a-zA-Z0-9]+", node);
		
		if(!legitNode)
			throw new AssemblerException("AssemblyOpTree error: Node error, should be alphanumeric token, expected format <node> : <expression>");
		
		String expression = colonSplit[1].trim();

		// First entry must be root term
		if (firstAssemblyOpTreeEntry || node.equals(rootOpTreeEntry)) {
			
			// Legit assemblyOpTree expression:
			// (letters|numbers)+ space* colon space* (!(space|colon))+ (space*
			// (!(space|colon))+)*
			boolean legitRootExp = Pattern.matches(
					"[^\\s:]+(\\s*[^\\s:]+)*", expression);

			if (!legitRootExp)
				throw new AssemblerException("AssemblyOpTree error: Root expression syntax error.");

			if(firstAssemblyOpTreeEntry){
				rootOpTreeEntry = node;
				assemblyOpTree.setRootToken(node);
				firstAssemblyOpTreeEntry = false;
			}
		}
		
		else{
			// Single token
			boolean legitNonRootExp = Pattern.matches("[^\\s:]+", expression);

			if (!legitNonRootExp)
				throw new AssemblerException("AssemblyOpTree error: Non root expressions should only consist of a single token.");
			
			else if (expression.charAt(expression.length() - 1) == '*'
					|| expression.charAt(expression.length() - 1) == '+'
					|| expression.charAt(expression.length() - 1) == '?')
				throw new AssemblerException(
						"AssemblyOpTree error: Wildcards (\"*\", \"+\" or \"?\") can only be applied to tokens in root node expression (\""
								+ data.getAssemblyOpTree().getRootToken()
								+ "\").");
		}

		assemblyOpTree.getAssemblyOpTreeTokens().add(node);
		assemblyOpTree.getAssemblyOpTreeTokens().add(expression);

		// If node already exists in tree, then add to existing node list, else put
		// node in hash
		ArrayList<String> list = assemblyOpTree.getAssemblyOpTreeHash().get(node);

		if (list != null)
			list.add(expression);

		else{			
			ArrayList<String> termsList = new ArrayList<String>();
			termsList.add(expression);
			assemblyOpTree.getAssemblyOpTreeHash().put(node, termsList);
		}
	}

	private void setBooleanValues(boolean architecture, boolean registers,
			boolean mnemonicData, boolean instructionFormat,
			boolean assemblyOpTree, boolean endian, boolean minAddressableUnit) {

		this.architecture = architecture;
		this.registers = registers;
		this.mnemonicData = mnemonicData;
		this.instructionFormat = instructionFormat;
		this.assemblyOpTree = assemblyOpTree;
		this.endian = endian;
		this.minAddressableUnit = minAddressableUnit;
	}

	private void analyseArchitecture(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;
		
		if(foundArchitecture)
			throw new AssemblerException("Architecture error: Architecture name already specified.");

		foundArchitecture = true;
		data.setArchitecture(line.trim());
	}

	private void analyseRegisters(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		foundRegisters = true;
		line = line.trim();

		// Valid register expression:
		// (!space)+ space+ (!space)+
		boolean legitRegExp = Pattern.matches("[^\\s]+\\s+[^\\s]+", line);

		if (!legitRegExp)
			throw new AssemblerException(
					"Registers error: Syntax error, <registerName> <value><B/H/I> expected. For example:\n"
							+ "\n" 
							+ "eax    000B");

		String[] tokens = line.split("\\s+");

		String regName = tokens[0];
		String valueAndBase = tokens[1];		
		String regValue = getBinaryFromBase(valueAndBase);
		
		if(data.getRegisterHash().get(regName) != null)
			throw new AssemblerException("Registers error: Register \"" + regName + "\" already defined.");
		
		data.getRegisterHash().put(regName, regValue);
	}
	
	private String getBinaryFromBase(String valueAndBase) throws AssemblerException{
		
		char base = valueAndBase.charAt(valueAndBase.length() - 1);
		String value = valueAndBase.substring(0, valueAndBase.length() - 1);

		// Binary
		if (base == 'B') {
			if (value.isEmpty())
				throw new AssemblerException(
						"Value error: Syntax error, <value><B/H/I> expected.\n"
								+ "Binary value missing.");
			if (!isBinary(value))
				throw new AssemblerException("Value error: \"" + value
						+ "\" is not a valid binary value.");
		}

		// Hex
		else if (base == 'H') {
			if (value.isEmpty())
				throw new AssemblerException(
						"Value error: Syntax error, <value><B/H/I> expected.\n"
								+ "Hex value missing.");

			try {				
				value = Assembler.hexToBinary(value);				
			} catch (NumberFormatException e) {				
				throw new AssemblerException("Value error: \"" + value
						+ "\" is not a valid hex value.");
			}
		}

		// Integer
		else if (base == 'I') {
			if (value.isEmpty())
				throw new AssemblerException(
						"Value error: Syntax error, <value><B/H/I> expected.\n"
								+ "Integer value missing.");

			try {				
				value = Assembler.intToBinary(value);				
			} catch (NumberFormatException e) {				
				throw new AssemblerException("Value error: \"" + value
						+ "\" is not a valid integer.");
			}
		}

		else
			throw new AssemblerException(
					"Value error: Syntax error, <value><B/H/I> expected.\n"
							+ "Last character of second string (\""
							+ valueAndBase
							+ "\") should indicate data type (\"B\", \"H\" or \"I\")." 
							+ "\nB indicates value is binary, H indicates hexadecimal and I indicates integer.");
		
		return value;
	}

	private void analyseMnemonicData(String line) throws AssemblerException {

		try {			
			currentMnemonic.addToRawLines(line);			
		} catch (NullPointerException e) {}

		if (line.trim().length() == 0) {
			emptyLine = true;
			
			if (abortMnem)
				return;
			
			if (atOperandFieldEncodings || atLocalFieldEncodings || atInsFormat)
				checkWhatLineExpected();
			return;
		}

		foundMnemData = true;

		// New mnemonic (no whitespace at beginning)
		if (Pattern.matches("[^\t\\s].*", line) && emptyLine
				&& foundFormatHeader && !atOperandFieldEncodings && !atLocalFieldEncodings
				&& !atInsFormat) {
			emptyLine = false;
			analyseMnemName(line);
			currentMnemonic.addToRawLines(line);
		}

		else if (abortMnem)
			return;

		else if (currentMnemonic == null) {
			abortMnem = true;
			throw new AssemblerException(
					"MnemonicData error: Mnemonic name not declared.");
		}

		// Global field encodings (starts with tab and not passed an empty line)
		else if (Pattern.matches("\t[^\t\\s].*", line) && !emptyLine
				&& !doneGlobalOpcodes) {
			analyseGlobalFieldEncodings(line);
			doneGlobalOpcodes = true;
		}

		// Operand format (starts with tab and empty line passed)
		else if (Pattern.matches("\t[^\t\\s].*", line)) {

			if(!emptyLine)
				checkWhatLineExpected();				
			emptyLine = false;
			analyseOperandFormat(line);
			atOperandFieldEncodings = true;
			foundFormatHeader = true;

			try {				
				currentMnemFormat.addToRawLineString(line);				
			} catch (NullPointerException e) {}
		}

		// Operand format data (starts with double tab and empty line passed)
		else if (Pattern.matches("\t\t[^\t\\s].*", line)
				&& (atOperandFieldEncodings || atLocalFieldEncodings || atInsFormat)) {

			try {				
				currentMnemFormat.addToRawLineString(line);				
			} catch (NullPointerException e) {}

			analyseOperandFormatData(line);
		}

		// Exception (indentation not recognised)
		else
			checkWhatLineExpected();
	}

	private void checkWhatLineExpected() throws AssemblerException {

		abortMnem = true;

		if (atOperandFieldEncodings) {
			abortMnem = true;
			throw new AssemblerException(
					"MnemonicData error: Line format or indentation error, instruction field labels line expected.\n"
							+ getMnemDataErrorMessage());
		}

		else if (atLocalFieldEncodings) {
			throw new AssemblerException(
					"MnemonicData error: Line format or indentation error, local opcodes line expected.\n"
							+ getMnemDataErrorMessage());
		}

		else if (atInsFormat) {
			throw new AssemblerException(
					"MnemonicData error: Line format or indentation error, instruction format line expected.\n"
							+ getMnemDataErrorMessage());
		}
		
		if (!emptyLine){
			throw new AssemblerException(
					"MnemonicData error: Line format error, empty line expected.\n"
							+ getMnemDataErrorMessage());
		}
		
		if (!foundFormatHeader) {
			throw new AssemblerException(
					"MnemonicData error: Line format or indentation error, operand format line expected.\nOperand format missing for mnemonic \""
							+ currentMnemonic.getMnemonic()
							+ "\".\n"
							+ getMnemDataErrorMessage());
		}
		
		else{
		}
	}

	private void analyseMnemName(String line) throws AssemblerException {

		// Reset boolean values for new mnemonic
		resetBooleanValues();
		String mnem = line.trim();

		// Legit mnemonic name expression:
		// (!space)+
		boolean legitMnemName = Pattern.matches("[^\\s]+", mnem);

		if (!legitMnemName) {
			abortMnem = true;			
			throw new AssemblerException(
					"MnemonicData error: Mnemonic name syntax error, should only be single token (no spaces).");
		}

		currentMnemonic = new Mnemonic();
		currentMnemonic.setMnemonic(mnem);
		
		if(data.getMnemonicTable().get(mnem) != null)
			throw new AssemblerException(
					"MnemonicData error: Mnemonic name \"" + mnem + "\" already defined.");

		// Put mnemonic data in mnemonic hash table
		data.getMnemonicTable().put(mnem, currentMnemonic);
	}

	private void analyseOperandFormat(String line) throws AssemblerException {

		line = line.trim();

		String[] mnemFormatSplit = line.split("\\s+"); 
		ArrayList<String> mnemFormatList = new ArrayList<String>();

		for (String formatTerm : mnemFormatSplit) {
			formatTerm = formatTerm.replaceAll("^,+", "");
			formatTerm = formatTerm.replaceAll(",+$", "");
			
			if(!formatTerm.isEmpty())
				mnemFormatList.add(formatTerm);
		}

		// Check format token has been defined somewhere in tree
		ArrayList<String> assemblyOpTreeTokens = data.getAssemblyOpTree().getAssemblyOpTreeTokens();

		for (String formatToken : mnemFormatList) {
			if (!assemblyOpTreeTokens.contains(formatToken)) {				
				abortMnem = true;				
				throw new AssemblerException(
						"MnemonicData error: Operand format token \""
								+ formatToken
								+ "\" not found in AssemblyOpTree.");
			}
		}

		currentMnemFormat = new OperandFormat();
		currentMnemFormat.setMnemFormat(line);
		currentMnemonic.getOperandsFormats().add(line);
		
		if(currentMnemonic.getOperandFormatHash().get(line) != null)
			throw new AssemblerException(
					"MnemonicData error: Operand format \"" + line + "\" already defined for mnemonic \"" + currentMnemonic.getMnemonic() + "\".");
		
		currentMnemonic.getOperandFormatHash().put(line, currentMnemFormat);
	}

	private void analyseOperandFormatData(String line) throws AssemblerException {

		if (atOperandFieldEncodings) {			
			line = line.trim();	
			
			// If line is "--" then there are no operand field encodings
			if (!line.equals("--"))
				currentMnemFormat.setOperandFieldEncodings(line);			
			atOperandFieldEncodings = false;
			atLocalFieldEncodings = true;
		}

		else if (atLocalFieldEncodings) {
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
					abortMnem = true;					
					throw new AssemblerException(
							"MnemonicData error: Local opcodes syntax error,"
									+ "\n<fieldName>=<value><B/H/I> or \"--\" (if no local opcodes) expected.");
				}

				// Legit local field encodings so omit unnecessary spaces
				line = line.replaceAll("\\s+", "");
				String[] tokens = line.split(",");

				for (String token : tokens) {
					String[] elements = token.split("=");					
					String field = elements[0];					
					String valueAndBase = elements[1];
					String binary = getBinaryFromBase(valueAndBase);					
					currentMnemFormat.getFieldBitHash().put(field, binary);
				}
			}
			atLocalFieldEncodings = false;
			atInsFormat = true;
		}

		else if (atInsFormat) {
			line = line.trim();
			String[] tokens = line.split("\\s+");
			for (String ins : tokens)
				currentMnemFormat.getInstructionFormat().add(ins);
			endOfOperandFormatBlockErrorCheck();
			atInsFormat = false;
		}
	}
	
	private String getMnemDataErrorMessage() {

		ArrayList<String> rawLines = currentMnemonic.getRawLines();
		int noOfLines = rawLines.size();
		int maxLineLength = 0;
		String msg = "";

		for (String str : rawLines) {
			str = str.replaceAll("\\s+$", "");
			if (str.length() > maxLineLength)
				maxLineLength = str.length();
			msg += "\n" + str;
		}

		int lastLineLength = rawLines.get(noOfLines - 1).replaceAll("\\s+$", "").length();
		int noOfSpaces = 0;
		String whiteSpace = "\t\t\t";

		if (lastLineLength == 0)
			noOfSpaces = maxLineLength;
		
		else
			noOfSpaces = maxLineLength - lastLineLength;

		for (; noOfSpaces > 0; noOfSpaces -= 1)
			whiteSpace += " ";

		msg += whiteSpace + "<---";

		return msg;
	}

	private void endOfOperandFormatBlockErrorCheck() throws AssemblerException {

		ArrayList<String> instructionFormat = currentMnemFormat.getInstructionFormat();
		int totalBits = 0;

		for (String instruction : instructionFormat) {
			InstructionFormat insFormat = data.getInstructionFormatHash().get(instruction);

			if (insFormat == null) {
				abortMnem = true;
				throw new AssemblerException(
						currentMnemFormat.getRawLinesString()
								+ "\nMnemonicData error: Instruction \""
								+ instruction
								+ "\" not defined in instructionFormat section.");
			}

			ArrayList<String> instructions = insFormat.getFields();

			for (String field : instructions) {
				int bits = insFormat.getFieldBitHash().get(field);
				totalBits += bits;

				if (currentMnemonic.getGlobalFieldEncodingHash().get(field) != null) {
					String field1 = currentMnemonic.getGlobalFieldEncodingHash().get(field);					
					int noOfBits = field1.length();

					if (noOfBits > bits) {
						abortMnem = true;
						throw new AssemblerException(
								currentMnemonic.getRawLinesString()
										+ "\nMnemonicData error: Encoding for field \""
										+ field
										+ "\" in \""
										+ currentMnemonic.getMnemonic()
										+ "\" global opcodes ("
										+ currentMnemonic.getRawGlobalFieldEncodingString()
										+ ")\nexceeds expected " 
										+ bits
										+ " bits in instruction format \""
										+ instruction + "\" ("
										+ insFormat.getRawLineString() 
										+ ").");
					}
				}

				else if (currentMnemFormat.getFieldBitHash().get(field) != null) {
					String field1 = currentMnemFormat.getFieldBitHash().get(field);
					int noOfBits = field1.length();
					
					if (noOfBits > bits) {
						abortMnem = true;
						throw new AssemblerException(
								currentMnemFormat.getRawLinesString()
										+ "\nMnemonicData error: Encoding for field \""
										+ field 
										+ "\" in local opcodes for \""
										+ currentMnemonic.getMnemonic()
										+ "\" format \""
										+ currentMnemFormat.getMnemFormat()
										+ "\"\nexceeds expected " 
										+ bits
										+ " bits in instruction format \""
										+ instruction + "\" ("
										+ insFormat.getRawLineString() 
										+ ").");
					}
				}

				else if (existsInInsFieldLabels(currentMnemFormat.getOperandFieldEncodings(), field));

				else {
					abortMnem = true;
					throw new AssemblerException(currentMnemonic.getRawLinesString()
							+ "\nMnemonicData error: Field \"" 
							+ field
							+ "\" in instruction format \"" 
							+ instruction
							+ "\" (" + insFormat.getRawLineString()
							+ ")\nnot found within global \""
							+ currentMnemonic.getMnemonic()
							+ "\" opcodes ("
							+ currentMnemonic.getRawGlobalFieldEncodingString()
							+ ") or in \"" 
							+ currentMnemonic.getMnemonic()
							+ "\" format \""
							+ currentMnemFormat.getMnemFormat() 
							+ "\".");
				}
			}
		}
		
		int minAdrUnit = data.getMinAdrUnit();

		// If total instruction size is not divisible by minimum addressable unit
		if (totalBits % minAdrUnit != 0)
			throw new AssemblerException(currentMnemFormat.getRawLinesString()
					+ "\nMnemonicData error: Total instruction size ("
							+ totalBits
							+ " bits) should be divisable by the minimum addressable unit ("
							+ minAdrUnit + ")");
	}

	private boolean existsInInsFieldLabels(String operandFieldEncodings, String field) {

		String[] operandFieldEncodingTokens = operandFieldEncodings.split("[^a-zA-Z0-9]+");

		for (String field1 : operandFieldEncodingTokens) {
			if (field1.equals(field))
				return true;
		}
		return false;
	}

	private void resetBooleanValues() {

		abortMnem = false;

		doneGlobalOpcodes = false;
		emptyLine = false;

		foundFormatHeader = false;
		atOperandFieldEncodings = false;
		atLocalFieldEncodings = false;
		atInsFormat = false;

		currentMnemonic = null;
	}

	private void analyseGlobalFieldEncodings(String line) throws AssemblerException {

		line = line.trim();

		// Legit global opcode expression:
		// alphanumeric+ space* equals space* alphanumeric+
		// (space* comma space* alphanumeric+ space* equals space*
		// alphanumeric+)*
		boolean legitGlobalOpcodes = Pattern
				.matches("[a-zA-Z0-9]+\\s*=\\s*[a-zA-Z0-9]+(\\s*,\\s*[a-zA-Z0-9]+\\s*=\\s*[a-zA-Z0-9]+)*",
						line);

		if (!legitGlobalOpcodes) {
			abortMnem = true;			
			throw new AssemblerException(
					"MnemonicData error: Global opcodes syntax error, <fieldName>=<value><B/H/I> expected.");
		}

		// Legit global opcodes so omit unnecessary spaces
		line = line.replaceAll("\\s+", "");
		String[] tokens = line.split(",");

		for (String token : tokens) {
			String[] elements = token.split("=");			
			String opcode = elements[0];			
			String valueAndBase = elements[1];
			String binary = getBinaryFromBase(valueAndBase);			
			currentMnemonic.getGlobalFieldEncodingHash().put(opcode, binary);
		}

		currentMnemonic.setRawGlobalFieldEncodingString(line);
	}

	private void analyseInstructionFormat(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		foundInsFormat = true;
		line = line.trim();

		// Legit instruction format:
		// (!(space|colon))+ space* colon space* (letters|numbers)+ openBracket
		// 0-9+ closeBracket
		// (space* (letter|numbers)+ openBracket 0-9+ closeBracket)*
		boolean validInsFormat = Pattern
				.matches("[^\\s:]+\\s*:\\s*[a-zA-Z0-9]+\\([0-9]+\\)(\\s*[a-zA-Z0-9]+\\([0-9]+\\))*",
						line);

		if (!validInsFormat) {
			throw new AssemblerException(
					"InstructionFormat error: Syntax error, <instructionName> : <fieldName>(<bitLength>) expected. For example:\n"
							+ "\nopcode : op(6) d(1) s(1)");
		}

		InstructionFormat insF = new InstructionFormat();
		String[] tokens = line.split(":");
		String insName = tokens[0].trim();
		String fieldsAndValues = tokens[1].trim();
		String[] fieldTokens = fieldsAndValues.split("\\s+");

		for (String field : fieldTokens) {
			String[] fieldAndSize = field.split("\\(|\\)");
			String fieldName = fieldAndSize[0];
			int bitSize = Integer.parseInt(fieldAndSize[1]);			
			if(bitSize == 0)
				throw new AssemblerException(
						"InstructionFormat error: Can not have 0 bit field length.");
			insF.getFields().add(fieldName);
			insF.getFieldBitHash().put(fieldName, bitSize);
		} 
		
		insF.setInstructionName(insName);
		insF.setRawLineString(line.trim());
		
		if(data.getInstructionFormatHash().get(insName) != null)
			throw new AssemblerException(
					"InstructionFormat error: Instruction \""+ insName + "\" already defined");
		
		data.getInstructionFormatHash().put(insName, insF);
	}
	
	private boolean isBinary(String s) {

		String pattern = "[0-1]*$";

		if (s.matches(pattern))
			return true;

		return false;
	}

	public DataSource getData() {
		return data;
	}
}
