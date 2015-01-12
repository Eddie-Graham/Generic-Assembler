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
			adt, endian;
	private boolean atGlobalOpcodes, first, working, emptyLine, atMnemType,
			atMnemHeader, atMnemName, abort;
	private boolean atLocalInsLabels, atLocalOpCodes, atLocalInsFormat;
	private boolean firstEntry;
	private MnemonicData currentMnemonicData;
	private MnemType currentMnemType;

	/**
	 * <pre>
	 * Constructor for class, initialises variables and calls methods which scan both files.
	 * </pre>
	 * 
	 * @param specFile
	 * @param assemblyFile
	 */
	public FileParser(String specFile, String assemblyFile) {

		data = new DataSource();

		architecture = false;
		registers = false;
		mnemonicData = false;
		instructionFormat = false;
		adt = false;
		endian = false;

		atGlobalOpcodes = true;
		first = false;
		working = false;
		emptyLine = false;
		atMnemType = false;
		atLocalInsLabels = false;
		atLocalOpCodes = false;
		abort = false;
		atLocalInsFormat = false;
		atMnemHeader = false;
		atMnemName = false;

		firstEntry = true;

		currentMnemonicData = null;
		currentMnemType = null;

		scanAssemblyFile(assemblyFile);
		scanSpecFile(specFile);
	}

	/**
	 * <pre>
	 * Scans assembly file and stores it in data source.
	 * </pre>
	 * 
	 * @param fileName
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
	 * Scans specification file and stores data in data source.
	 * </pre>
	 * 
	 * @param fileName
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

			String[] commentSplit = assemblyLine.split(";");
			assemblyLine = commentSplit[0];

			assemblyLine = assemblyLine.replaceAll("\\s+$", ""); // remove end
																	// whitespace

			try {
				scanLine(assemblyLine);
			} catch (AssemblerException e) {
				System.out.println("Exception at line " + lineCounter);
				System.out.println("Line: \"" + assemblyLine + "\"");
				System.out.println(e.getMessage());
				System.out.println();
			}
		}

		inputFile.close();
	}

	/**
	 * <pre>
	 * Scans line from specification file and diverts it to relevant method for further analysis.
	 * </pre>
	 * 
	 * @param line
	 * @throws AssemblerException
	 */
	private void scanLine(String line) throws AssemblerException {

		String lowerCaseLine = line.toLowerCase();

		if (lowerCaseLine.startsWith("architecture:"))
			setBooleanValues(true, false, false, false, false, false);

		else if (lowerCaseLine.startsWith("registers:"))
			setBooleanValues(false, true, false, false, false, false);

		else if (lowerCaseLine.startsWith("mnemonicformat:"))
			setBooleanValues(false, false, true, false, false, false);

		else if (lowerCaseLine.startsWith("instructionformat:"))
			setBooleanValues(false, false, false, true, false, false);

		else if (lowerCaseLine.startsWith("adt:"))
			setBooleanValues(false, false, false, false, true, false);

		else if (lowerCaseLine.startsWith("endian:"))
			setBooleanValues(false, false, false, false, false, true);

		else if (architecture)
			analyseArchitecture(line);

		else if (registers)
			analyseRegisters(line);

		else if (mnemonicData)
			analyseMnemonicData(line);

		else if (instructionFormat)
			analyseInstructionFormat(line);

		else if (adt)
			analyseADT(line);

		else if (endian)
			analyseEndian(line);
	}

	/**
	 * <pre>
	 * Sets endian, "big" or "little" (not case sensitive).
	 * </pre>
	 * 
	 * @param line
	 * @throws AssemblerException
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
	}

	/**
	 * <pre>
	 * Analyses ADT line, expected format: 
	 * label : label label*
	 * </pre>
	 * 
	 * @param line
	 * @throws AssemblerException
	 */
	private void analyseADT(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;
		
		line = line.trim();

		boolean legit = Pattern.matches(
				"^\\s*[^\\s:]*\\s*:[\\s*[^\\s:]*]+[\\s+[^\\s:]*]*\\s*$", line);	//TODO fix

		if (!legit)
			throw new AssemblerException(
					"ADT syntax error, label : label label* expected.");

		ADT adt = data.getAdt();

		String[] colonSplit = line.split("(?=[:])|(?<=[:])");

		String label = colonSplit[0].trim();
		String terms = colonSplit[2].trim();

		if (firstEntry) {

			String rootTerm = label;
			adt.setRootTerm(rootTerm);
			firstEntry = false;
		}

		ArrayList<String> termsList = new ArrayList<String>();
		termsList.add(terms);

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
			boolean endian) {

		this.architecture = architecture;
		this.registers = registers;
		this.mnemonicData = mnemonicData;
		this.instructionFormat = instructionFormat;
		this.adt = adt;
		this.endian = endian;
	}

	/**
	 * <pre>
	 * Sets architecture name.
	 * </pre>
	 * 
	 * @param line
	 */
	private void analyseArchitecture(String line) {

		if (line.trim().length() == 0)
			return;

		data.setArchitecture(line.trim());
	}

	/**
	 * <pre>
	 * Sets register name and value, expected format: 
	 * regName regValue (regValue must end in "B", "H" or "D" to indicate data type).
	 * </pre>
	 * 
	 * @param line
	 * @throws AssemblerException
	 */
	private void analyseRegisters(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		line = line.trim();

		boolean legit = Pattern.matches("[^\\s]+\\s+[^\\s]+", line);

		if (!legit)
			throw new AssemblerException(
					"Registers syntax error, regName regValue expected.");

		String[] tokens = line.split("\\s+");

		String regLabel = tokens[0];
		String regValue = tokens[1];

		char dataType = regValue.charAt(regValue.length() - 1);
		regValue = regValue.substring(0, regValue.length() - 1);

		if (dataType == 'B') {

			if (!isBinary(regValue))
				throw new AssemblerException("Registers syntax error, \""
						+ regValue + "\" is not binary.");

			data.getRegisterHash().put(regLabel, regValue);
		}

		else if (dataType == 'H') {

			try {
				regValue = Assembler.hexToBinary(regValue);
			} catch (NumberFormatException e) {
				throw new AssemblerException("Registers syntax error, \""
						+ regValue + "\" is not a valid hex number.");
			}

			data.getRegisterHash().put(regLabel, regValue);
		}

		else if (dataType == 'D') {

			try {
				regValue = Assembler.decimalToBinary(regValue);
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
	 * 		globalOpcodes
	 * 
	 * 		mnemType
	 * 			insLabels
	 * 			localOpcodes
	 * 			insFormat
	 * 
	 * 		mnemType
	 * 			...
	 * 
	 * mnemName
	 * 		...
	 * </pre>
	 * 
	 * 
	 * @param line
	 * @throws AssemblerException
	 */
	private void analyseMnemonicData(String line) throws AssemblerException {

		if (line.trim().length() == 0) {

			if (abort)
				return;

			if (working)
				emptyLine = true;

			if (atMnemHeader || atLocalInsLabels || atLocalOpCodes || atLocalInsFormat) {

				abort = true;
				throw new AssemblerException(
						"Mnemonic data syntax error, line format error.");
			}

			return;
		}

		if (!line.startsWith(" ") && !line.startsWith("\t"))// new mnemonic
			atMnemName = true;

		else if (abort)
			return;

		else if (Pattern.matches("\t[^\t\\s].*", line) && !emptyLine) {

			if (first) {

				atGlobalOpcodes = true;
				first = false;
			}
		}

		else if (Pattern.matches("\t[^\t\\s].*", line) && emptyLine) {

			if (!(atLocalInsLabels || atLocalOpCodes || atLocalInsFormat))
				atMnemHeader = true;
		}

		else if (Pattern.matches("\t\t[^\t\\s].*", line) && emptyLine)
			atMnemType = true;

		else if (working) {

			abort = true;
			throw new AssemblerException(
					"Mnemonic data syntax error, indentation error.");
		}

		else {

			abort = true;
			throw new AssemblerException(
					"Mnemonic data syntax error, mnemonicName expected (no spaces at start allowed).");
		}

		if (atMnemName) {

			analyseMnemName(line);
			atMnemName = false;
		}

		else if (atGlobalOpcodes) {

			analyseGlobalOpcodes(line);
			atGlobalOpcodes = false;
		}

		else if (atMnemHeader) {
			
			analyseMnemHeader(line);
			atMnemHeader = false;
			atLocalInsLabels = true;
		}

		else if (atMnemType)
			analyseMnemType(line);

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
	 * @param line
	 * @throws AssemblerException
	 */
	private void analyseMnemName(String line) throws AssemblerException {

		resetBooleanValues();

		String mnem = line.trim();

		boolean legit = Pattern.matches("[^\\s]+", line);

		if (!legit) {

			abort = true;
			throw new AssemblerException(
					"Mnemonic data syntax error, mnemonic should only be single string (no spaces).");
		}

		currentMnemonicData = new MnemonicData();
		currentMnemonicData.setMnemonic(mnem);

		data.getMnemonicTable().put(mnem, currentMnemonicData);

		working = true;
	}
	
	/**
	 * <pre>
	 * Sets mnemonic header.
	 * </pre>
	 * 
	 * @param line
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
	 * @param line
	 * @throws AssemblerException
	 */
	private void analyseMnemType(String line) throws AssemblerException {

		atMnemType = false;
		
		if (atLocalInsLabels) {

			line = line.trim();

			currentMnemType.setInsLabels(line);

			atLocalInsLabels = false;
			atLocalOpCodes = true;
		}

		else if (atLocalOpCodes) {

			line = line.trim();

			if (!line.equals("--")) {

				boolean legitOpcodes = Pattern.matches(
								"[^\\s=,]+\\s*=\\s*[^\\s=,]+(\\s*,\\s*[^\\s=,]+\\s*=\\s*[^\\s=,]+)*", line);

				if (!legitOpcodes) {

					abort = true;
					throw new AssemblerException(
							"Mnemonic data syntax error, codeLabel=codeValue(,codeLabel=codeValue)* expected.");
				}

				line = line.replaceAll("\\s+", "");

				String[] tokens = line.split(",");

				for (String token : tokens) {

					String[] elements = token.split("=");
					currentMnemType.getOpCodes().put(elements[0], elements[1]);
				}
			}

			atLocalOpCodes = false;
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

		else {

			abort = true;
			throw new AssemblerException(
					"Mnemonic data syntax error, indentation error.");
		}
	}

	/**
	 * <pre>
	 * Resets boolean values when about to work on new mnemonic. 
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
		atLocalOpCodes = false;
		atLocalInsFormat = false;

		currentMnemonicData = null;
	}

	/**
	 * <pre>
	 * Sets global opcodes, expected format:
	 * codeLabel=codeValue(,codeLabel=codeValue)*	  
	 * </pre>
	 * 
	 * @param opcodes
	 * @throws AssemblerException
	 */
	private void analyseGlobalOpcodes(String opcodes) throws AssemblerException {

		opcodes = opcodes.trim();

		boolean legitOpcodes = Pattern.matches(
						"[^\\s=,]+\\s*=\\s*[^\\s=,]+(\\s*,\\s*[^\\s=,]+\\s*=\\s*[^\\s=,]+)*", opcodes);

		if (!legitOpcodes) {

			abort = true;
			throw new AssemblerException(
					"Mnemonic data syntax error, codeLabel=codeValue(,codeLabel=codeValue)* expected.");
		}

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
	 * @param line
	 * @throws AssemblerException
	 */
	private void analyseInstructionFormat(String line) throws AssemblerException {

		if (line.trim().length() == 0)
			return;

		line = line.trim();

		boolean legit = Pattern.matches(
						"[^\\s:]+\\s*:\\s*[^\\s:]+\\([0-9]+\\)(\\s*[^\\s:]+\\([0-9]+\\))*", line);

		if (!legit) {

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
	 * Returns true if string represents a binary number, else false.
	 * </pre>
	 * 
	 * @param s
	 * @return
	 */
	public boolean isBinary(String s) {

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
	 * @return
	 */
	public DataSource getData() {
		return data;
	}
}
