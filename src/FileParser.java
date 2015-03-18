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
	private boolean foundFormatHeader, atLocalInsLabels, atLocalOpcodes,
			atLocalInsFormat;
	private boolean firstAssemblyOpTreeEntry;
	private String rootOpTreeEntry;
	private MnemonicData currentMnemonicData;
	private MnemonicFormat currentMnemFormat;

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
		atLocalInsLabels = false;
		atLocalOpcodes = false;
		atLocalInsFormat = false;

		firstAssemblyOpTreeEntry = true;
		rootOpTreeEntry = "";

		currentMnemonicData = null;
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
		
		else{
			errorReport.add("No errors found within specification file.");
		}
		
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
		
		// run one last time with empty line to catch any error at end of mnemonic data section
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

//	private void analyseAssemblyOpTree(String line) throws AssemblerException {
//
//		if (line.trim().length() == 0)
//			return;
//
//		foundAssemblyOpTree = true;
//
//		line = line.trim();
//
//		// Legit assemblyOpTree expression:
//		// (letters|numbers)+ space* colon space* (!(space|colon))+ (space*
//		// (!(space|colon))+)*
//		boolean legitAssemblyOpTreeExp = Pattern.matches(
//				"[a-zA-Z0-9]+\\s*:\\s*[^\\s:]+(\\s*[^\\s:]+)*", line);
//
//		if (!legitAssemblyOpTreeExp)
//			throw new AssemblerException("AssemblyOpTree error: Syntax error."); // TODO
//
//		AssemblyOpTree assemblyOpTree = data.getAssemblyOpTree();
//
//		String[] assemblyOpTreeTokens = line.split("[^A-Za-z0-9]+");
//
//		for (String assemblyOpTreeToken : assemblyOpTreeTokens)
//			assemblyOpTree.getAssemblyOpTreeTokens().add(assemblyOpTreeToken);
//
//		String[] colonSplit = line.split("(?=[:])|(?<=[:])");
//
//		String label = colonSplit[0].trim();
//		String terms = colonSplit[2].trim();
//
//		// First entry must be root term
//		if (firstAssemblyOpTreeEntry) {
//
//			String rootToken = label;
//			assemblyOpTree.setRootToken(rootToken);
//			firstAssemblyOpTreeEntry = false;
//		}
//
//		ArrayList<String> termsList = new ArrayList<String>();
//		termsList.add(terms);
//
//		// If label already exists in hash, then add to existing list, else put
//		// label in hash
//		ArrayList<String> list = assemblyOpTree.getAssemblyOpTreeHash().get(
//				label);
//
//		if (list != null)
//			list.add(terms);
//
//		else
//			assemblyOpTree.getAssemblyOpTreeHash().put(label, termsList);
//	}
	
	private void analyseAssemblyOpTree(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		foundAssemblyOpTree = true;

		line = line.trim();		
		
		boolean legitAssemblyOpTreeExp = Pattern.matches("[^:]*:[^:]*", line);

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
			throw new AssemblerException("label error");
		
		String terms = colonSplit[1].trim();

		// First entry must be root term
		if (firstAssemblyOpTreeEntry || node.equals(rootOpTreeEntry)) {
			
			// Legit assemblyOpTree expression:
			// (letters|numbers)+ space* colon space* (!(space|colon))+ (space*
			// (!(space|colon))+)*
			boolean legitRootExp = Pattern.matches(
					"[^\\s:]+(\\s*[^\\s:]+)*", terms);

			if (!legitRootExp)
				throw new AssemblerException("AssemblyOpTree error: Root expression syntax error.");

			if(firstAssemblyOpTreeEntry){
				rootOpTreeEntry = node;
				assemblyOpTree.setRootToken(node);
				firstAssemblyOpTreeEntry = false;
			}
		}
		
		else{
			boolean legitNonRootExp = Pattern.matches(
					"[^\\s:]+", terms);

			if (!legitNonRootExp)
				throw new AssemblerException("AssemblyOpTree error: Non root expressions should only consist of a single token.");
			
			else if (terms.charAt(terms.length() - 1) == '*'
					|| terms.charAt(terms.length() - 1) == '+'
					|| terms.charAt(terms.length() - 1) == '?')
				throw new AssemblerException("AssemblyOpTree error: Wildcards (\"*\", \"+\" or \"?\") can only be applied to tokens in root node (\"" + data.getAssemblyOpTree().getRootToken() + "\").");
		}

		assemblyOpTree.getAssemblyOpTreeTokens().add(node);
		assemblyOpTree.getAssemblyOpTreeTokens().add(terms);

		// If label already exists in hash, then add to existing list, else put
		// label in hash
		ArrayList<String> list = assemblyOpTree.getAssemblyOpTreeHash().get(node);

		if (list != null)
			list.add(terms);

		else{
			
			ArrayList<String> termsList = new ArrayList<String>();
			termsList.add(terms);
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

		// Legit register expression:
		// (!space)+ space+ (!space)+
		boolean legitRegExp = Pattern.matches("[^\\s]+\\s+[^\\s]+", line);

		if (!legitRegExp)
			throw new AssemblerException(
					"Registers error: Syntax error, <registerName> <value><B/H/I> expected. For example:\n"
							+ "\n" 
							+ "eax    000B");

		String[] tokens = line.split("\\s+");

		String regLabel = tokens[0];
		String valueAndBase = tokens[1];
		
		String regValue = getBinaryFromBase(valueAndBase);
		
		if(data.getRegisterHash().get(regLabel) != null)
			throw new AssemblerException("Registers error: Register \"" + regLabel + "\" already defined.");
		
		data.getRegisterHash().put(regLabel, regValue);
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
			
			currentMnemonicData.addToRawLines(line);
			
		} catch (NullPointerException e) {}

		if (line.trim().length() == 0) {

			emptyLine = true;

			if (abortMnem)
				return;

			if (atLocalInsLabels || atLocalOpcodes || atLocalInsFormat)
				checkWhatLineExpected();

			return;
		}

		foundMnemData = true;

		// New mnemonic (no whitespace at beginning)
		if (Pattern.matches("[^\t\\s].*", line) && emptyLine
				&& foundFormatHeader && !atLocalInsLabels && !atLocalOpcodes
				&& !atLocalInsFormat) {

			emptyLine = false;

			analyseMnemName(line);

			currentMnemonicData.addToRawLines(line);
		}

		else if (abortMnem)
			return;

		else if (currentMnemonicData == null) {

			abortMnem = true;

			throw new AssemblerException(
					"MnemonicData error: Mnemonic name not declared.");
		}

		// Global opcodes (starts with tab and not passed an empty line)
		else if (Pattern.matches("\t[^\t\\s].*", line) && !emptyLine
				&& !doneGlobalOpcodes) {

			analyseGlobalOpcodes(line);

			doneGlobalOpcodes = true;
		}

		// Mnemonic format header (starts with tab and empty line passed)
		else if (Pattern.matches("\t[^\t\\s].*", line)) {

			if(!emptyLine)
				checkWhatLineExpected();
				
			emptyLine = false;

			analyseOperandFormat(line);

			atLocalInsLabels = true;
			foundFormatHeader = true;

			try {
				
				currentMnemFormat.addToRawLineString(line);
				
			} catch (NullPointerException e) {}
		}

		// Mnemonic format data (starts with double tab and empty line passed)
		else if (Pattern.matches("\t\t[^\t\\s].*", line)
				&& (atLocalInsLabels || atLocalOpcodes || atLocalInsFormat)) {

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

		if (atLocalInsLabels) {

			abortMnem = true;

			throw new AssemblerException(
					"MnemonicData error: Line format or indentation error, instruction field labels line expected.\n"
							+ getMnemDataErrorMessage());
		}

		else if (atLocalOpcodes) {

			throw new AssemblerException(
					"MnemonicData error: Line format or indentation error, local opcodes line expected.\n"
							+ getMnemDataErrorMessage());
		}

		else if (atLocalInsFormat) {

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
							+ currentMnemonicData.getMnemonic()
							+ "\".\n"
							+ getMnemDataErrorMessage());
		}
		
		else{
			throw new AssemblerException("HEEEEEEREEEEEE");	//TODO
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

		currentMnemonicData = new MnemonicData();
		currentMnemonicData.setMnemonic(mnem);
		
		if(data.getMnemonicTable().get(mnem) != null)
			throw new AssemblerException(
					"MnemonicData error: Mnemonic name \"" + mnem + "\" already defined.");

		// Put mnemonic data in mnemonic hash table
		data.getMnemonicTable().put(mnem, currentMnemonicData);
	}

	private void analyseOperandFormat(String line) throws AssemblerException {

		line = line.trim();

		String[] mnemFormatSplit = line.split("\\s+"); // space
		ArrayList<String> mnemFormatList = new ArrayList<String>();

		for (String formatTerm : mnemFormatSplit) {

			formatTerm = formatTerm.replaceAll("^,+", "");
			formatTerm = formatTerm.replaceAll(",+$", "");
			
			if(!formatTerm.isEmpty())
				mnemFormatList.add(formatTerm);
		}

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

		currentMnemFormat = new MnemonicFormat();
		currentMnemFormat.setMnemFormat(line);
		currentMnemonicData.getMnemFormats().add(line);
		
		if(currentMnemonicData.getMnemFormatHash().get(line) != null)
			throw new AssemblerException(
					"MnemonicData error: Operand format \"" + line + "\" already defined for mnemonic \"" + currentMnemonicData.getMnemonic() + "\".");
		
		currentMnemonicData.getMnemFormatHash().put(line, currentMnemFormat);
	}

	private void analyseOperandFormatData(String line) throws AssemblerException {

		if (atLocalInsLabels) {
			
			line = line.trim();
			
			if (!line.equals("--"))
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

					abortMnem = true;
					
					throw new AssemblerException(
							"MnemonicData error: Local opcodes syntax error,"
									+ "\n<fieldName>=<value><B/H/I> or \"--\" (if no local opcodes) expected.");
				}

				// Legit local opcodes so omit unnecessary spaces
				line = line.replaceAll("\\s+", "");

				String[] tokens = line.split(",");

				for (String token : tokens) {

					String[] elements = token.split("=");
					
					String opcode = elements[0];
					
					String valueAndBase = elements[1];
					String binary = getBinaryFromBase(valueAndBase);
					
					currentMnemFormat.getOpcodes().put(opcode, binary);
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

			endOfFormatBlockErrorCheck();

			atLocalInsFormat = false;
		}
	}
	
	private String getMnemDataErrorMessage() {

		ArrayList<String> rawLines = currentMnemonicData.getRawLines();

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

	private void endOfFormatBlockErrorCheck() throws AssemblerException {

		ArrayList<String> instructionFormat = currentMnemFormat.getInstructionFormat();
		int totalBits = 0;

		for (String instruction : instructionFormat) {

			InstructionFormatData insFormat = data.getInstructionFormat().get(instruction);

			if (insFormat == null) {

				abortMnem = true;

				throw new AssemblerException(
						currentMnemFormat.getRawLinesString()
								+ "\nMnemonicData error: Instruction \""
								+ instruction
								+ "\" not defined in instructionFormat section.");
			}

			ArrayList<String> instructions = insFormat.getOperands();

			for (String field : instructions) {

				int bits = insFormat.getOperandBitHash().get(field);
				totalBits += bits;

				if (currentMnemonicData.getGlobalOpCodes().get(field) != null) {

					String opcode = currentMnemonicData.getGlobalOpCodes().get(field);					
					int noOfBits = opcode.length();

					if (noOfBits > bits) {

						abortMnem = true;

						throw new AssemblerException(
								currentMnemonicData.getRawLinesString()
										+ "\nMnemonicData error: Encoding for field \""
										+ field
										+ "\" in \""
										+ currentMnemonicData.getMnemonic()
										+ "\" global opcodes ("
										+ currentMnemonicData.getRawGlobalOpcodesString()
										+ ")\nexceeds expected " 
										+ bits
										+ " bits in instruction format \""
										+ instruction + "\" ("
										+ insFormat.getRawLineString() 
										+ ").");
					}
				}

				else if (currentMnemFormat.getOpcodes().get(field) != null) {

					String opcode = currentMnemFormat.getOpcodes().get(field);
					int noOfBits = opcode.length();

					if (noOfBits > bits) {

						abortMnem = true;

						throw new AssemblerException(
								currentMnemFormat.getRawLinesString()
										+ "\nMnemonicData error: Encoding for field \""
										+ field 
										+ "\" in local opcodes for \""
										+ currentMnemonicData.getMnemonic()
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

				else if (existsInInsFieldLabels(currentMnemFormat.getInsFieldLabels(), field));

				else {

					abortMnem = true;

					throw new AssemblerException(currentMnemonicData.getRawLinesString()
							+ "\nMnemonicData error: Field \"" 
							+ field
							+ "\" in instruction format \"" 
							+ instruction
							+ "\" (" + insFormat.getRawLineString()
							+ ")\nnot found within global \""
							+ currentMnemonicData.getMnemonic()
							+ "\" opcodes ("
							+ currentMnemonicData.getRawGlobalOpcodesString()
							+ ") or in \"" 
							+ currentMnemonicData.getMnemonic()
							+ "\" format \""
							+ currentMnemFormat.getMnemFormat() 
							+ "\".");
				}
			}
		}
		
		int minAdrUnit = data.getMinAdrUnit();

		if (totalBits % minAdrUnit != 0)
			throw new AssemblerException(currentMnemFormat.getRawLinesString()
					+ "\nMnemonicData error: Total instruction size ("
							+ totalBits
							+ " bits) should be divisable by the minimum addressable unit ("
							+ minAdrUnit + ")");
	}

	private boolean existsInInsFieldLabels(String insFieldLabels, String field) {

		String[] insFieldLabelTokens = insFieldLabels.split("[^a-zA-Z0-9]+");

		for (String insFieldLabel : insFieldLabelTokens) {

			if (insFieldLabel.equals(field))
				return true;
		}

		return false;
	}

	private void resetBooleanValues() {

		abortMnem = false;

		doneGlobalOpcodes = false;
		emptyLine = false;

		foundFormatHeader = false;
		atLocalInsLabels = false;
		atLocalOpcodes = false;
		atLocalInsFormat = false;

		currentMnemonicData = null;
	}

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
			
			currentMnemonicData.getGlobalOpCodes().put(opcode, binary);
		}

		currentMnemonicData.setRawGlobalOpcodesString(line);
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
		boolean legitInsFormat = Pattern
				.matches("[^\\s:]+\\s*:\\s*[a-zA-Z0-9]+\\([0-9]+\\)(\\s*[a-zA-Z0-9]+\\([0-9]+\\))*",
						line);

		if (!legitInsFormat) {

			abortMnem = true;
			throw new AssemblerException(
					"InstructionFormat error: Syntax error, <instructionName> : <fieldName>(<bitLength>) expected. For example:\n"
							+ "\nopcode : op(6) d(1) s(1)");
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
			
			if(bitSize == 0)
				throw new AssemblerException(
						"InstructionFormat error: Can not have 0 bit field length.");

			insF.getOperands().add(op);
			insF.getOperandBitHash().put(op, bitSize);
		} 

		insF.setInstructionName(insName);
		insF.setRawLineString(line.trim());
		
		if(data.getInstructionFormat().get(insName) != null)
			throw new AssemblerException(
					"InstructionFormat error: Instruction \""+ insName + "\" already defined");
		
		data.getInstructionFormat().put(insName, insF);
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
