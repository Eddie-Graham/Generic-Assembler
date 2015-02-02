/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * <pre>
 * This class assembles each instruction using the data source.
 * </pre>
 * 
 * @author eddieindahouse
 *
 */
public class Assembler {

	private DataSource data;
	
	private int locationCounter;
	private int insNumber;
	private HashMap<Integer, Integer> insAdrTable;
	private HashMap<String, Integer> symbolTable;
	
	private ArrayList<ArrayList<String>> legitAdtPaths;
	private HashMap<String, String> assemblyTermTypeHash;
	
	private boolean atData, atText;
	private boolean first, second;
	
	private boolean debug = false;

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

		legitAdtPaths = new ArrayList<ArrayList<String>>();
		assemblyTermTypeHash = new HashMap<String, String>();
		
		atData = false;
		atText = false;
		
		first = false;
		second = false;

		assemble();
	}

	private void assemble() {
		
		firstPass();
		
		insNumber ++;
		insAdrTable.put(insNumber, locationCounter);	// to account for last line

		insNumber = 0;
		
		secondPass();		
	}

	private void firstPass() {
		
		int lineCounter = 0;
	
		for (String assemblyLine : data.getAssemblyCode()) {
			
			lineCounter ++;
			
			String[] commentSplit = assemblyLine.split(";");
			assemblyLine = commentSplit[0];
			
			if (assemblyLine.trim().length() > 0){	
				
				assemblyLine.replaceAll("\\s+$", "");	// remove end whitespace			
			
				if (assemblyLine.startsWith("section .data")) 
					setBooleanValues(true, false);
			
				else if(assemblyLine.startsWith("section .text")){
					setBooleanValues(false, true);
					first = true;
				}
			
				else if (atData){
				
				}
				
				else if (atText){
				
					if(first){
					
						if(assemblyLine.startsWith("\tglobal main")){
						
							first = false;
							second = true;
						}						
					}
				
					else if(second){
					
						if(assemblyLine.startsWith("main:"))						
							second = false;					
					}
				
					else{
					
						try {
							analyseInstructionForLabels(assemblyLine);
						} catch (AssemblerException e) {
							System.out.println("Exception at line " + lineCounter);
							System.out.println("Line: " + assemblyLine.trim());
							System.out.println(e.getMessage());		
							System.exit(0);
						}
					}				
				}
			}
		}		
	}

	private void secondPass() {
		
		int lineCounter = 0;
	
		for (String assemblyLine : data.getAssemblyCode()) {
			
			lineCounter ++;
			
			String[] commentSplit = assemblyLine.split(";");
			assemblyLine = commentSplit[0];
			
			if (assemblyLine.trim().length() > 0){	
				
				assemblyLine.replaceAll("\\s+$", "");	// remove end whitespace			
			
				if (assemblyLine.startsWith("section .data")) 
					setBooleanValues(true, false);
			
				else if(assemblyLine.startsWith("section .text")){
					setBooleanValues(false, true);
					first = true;
				}
			
				else if (atData){
				
				}
				
				else if (atText){
				
					if(first){
					
						if(assemblyLine.startsWith("\tglobal main")){
						
							first = false;
							second = true;
						}						
					}
				
					else if(second){
					
						if(assemblyLine.startsWith("main:"))						
							second = false;					
					}
				
					else{
					
						try {
							populateInstruction(assemblyLine);
						} catch (AssemblerException e) {
							System.out.println("Exception at line " + lineCounter);
							System.out.println("Line: " + assemblyLine.trim());
							System.out.println(e.getMessage());	
							System.exit(0);
						}
					}				
				}
			}
		}		
	}

	private void setBooleanValues(boolean atData, boolean atText) {
		
		this.atData = atData;
		this.atText = atText;
	}

	private void analyseInstructionForLabels(String assemblyLine) throws AssemblerException {
		
		legitAdtPaths = new ArrayList<ArrayList<String>>();
		assemblyTermTypeHash = new HashMap<String,String>();	
		
		assemblyLine = assemblyLine.trim();
	
		analyseWithADT(assemblyLine);
		
		MnemonicData mnemData = getMnemData(assemblyLine);		
		
		ArrayList<String> mnemFormats = mnemData.getMnemFormats();		// get mnem types
		String mnemFormat = "";
		
		for(String format: mnemFormats){
			
			if(formatMatch(format)){
				
				mnemFormat = format;	// find type
				break;
			}
		}
		
		if(mnemFormat == "")
			throw new AssemblerException("Mnem format mismatch.");
		
		if(!correctSyntax(mnemFormat, assemblyLine))
			throw new AssemblerException("Assembly line syntax error.");
		
		MnemFormat format = mnemData.getMnemFormatHash().get(mnemFormat);		// get type data		
		
		ArrayList<String> instructionFormat = format.getInstructionFormat();	// gets instructions
		
		int insSize = 0;
	
		for(String instruction: instructionFormat){
			
			InstructionFormatData insFormat = data.getInstructionFormat().get(instruction);
			
			ArrayList<String> instructions = insFormat.getOperands();
			
			for(String field: instructions){
				
				int bits = insFormat.getOperandBitHash().get(field);
				
				insSize += bits;				
			}
		}
		
		int minAdrUnit = data.getMinAdrUnit();
		
		int noOfAdrUnits = insSize/minAdrUnit;
		
//		System.out.println(Integer.toHexString(locationCounter) + ":   " + assemblyLine + "    ("+noOfAdrUnits+")");
		
		insNumber ++;
		insAdrTable.put(insNumber, locationCounter);
			
		String label = getLabelString();
		
		if(label != "")
			symbolTable.put(label, locationCounter);		
	
		locationCounter += noOfAdrUnits;		
	}

	private void populateInstruction(String assemblyLine) throws AssemblerException {		
			
			assemblyLine = assemblyLine.trim();
			
			System.out.println("*****************************");
			System.out.println(assemblyLine);
			
			legitAdtPaths = new ArrayList<ArrayList<String>>();
			assemblyTermTypeHash = new HashMap<String,String>();
		
			analyseWithADT(assemblyLine);
			
			System.out.println(legitAdtPaths);
			
			MnemonicData mnemData = getMnemData(assemblyLine);		
			
			ArrayList<String> mnemFormats = mnemData.getMnemFormats();		// get mnem types
			String mnemFormat = "";
			
			for(String format: mnemFormats){
				
				if(formatMatch(format)){
					
					mnemFormat = format;	// find type				
					break;
				}
			}
			
			if(mnemFormat == "")
				throw new AssemblerException("Mnem format mismatch.");		
			
	//		if(!correctSyntax(mnemFormat, assemblyLine))
	//			throw new AssemblerException("Assembly line syntax error.");		
			
			MnemFormat format = mnemData.getMnemFormatHash().get(mnemFormat);		// get type data
			
			String insFieldLabels = format.getInsFieldLabels();		
			String relevantOperands = getRelevantOperands(mnemFormat);
			HashMap<String, String> insFieldHash = mapInsFieldLabels(relevantOperands, insFieldLabels);	
			
			ArrayList<String> instructionFormat = format.getInstructionFormat();	// gets instructions
			
			String binary = "";
			insNumber ++;
			
			System.out.println("insFieldHash: " + insFieldHash);
			System.out.println("assTypeHash: " + assemblyTermTypeHash);
			
			for(String instruction: instructionFormat){
				
				InstructionFormatData insFormat = data.getInstructionFormat().get(instruction);
				
				ArrayList<String> instructions = insFormat.getOperands();
				
				for(String field: instructions){
					
					String binaryTemp = "";
					
					int bits = insFormat.getOperandBitHash().get(field);
					
					if(mnemData.getGlobalOpCodes().get(field)!= null)	//global
						binaryTemp = mnemData.getGlobalOpCodes().get(field);				
					
					else if(format.getOpCodes().get(field) != null)	//local
						binaryTemp = format.getOpCodes().get(field);
					
					else{
						
						String assemblyTerm = insFieldHash.get(field);
						
						if(data.getRegisterHash().get(assemblyTerm) != null)	// reg
							binaryTemp = data.getRegisterHash().get(assemblyTerm);
						
						else{	// imm etc
							
							String type = assemblyTermTypeHash.get(assemblyTerm);
							
							if(type.equals("INT"))
								binaryTemp = intToBinary(assemblyTerm);
							
							else if(type.equals("HEX"))
								binaryTemp = hexToBinary(assemblyTerm);
							
							else if(type.equals("LABEL")){
								binaryTemp = relativeJumpInBinary(assemblyTerm, bits);
							}
						}
					}
					
					binary += binaryFromBinaryFormatted(binaryTemp, bits);
				}
			}		
			
			ArrayList<String> binaryArray = splitToMinAdrUnits(binary);
			
			String hexObjCode = getHexObjCode(binaryArray);		
			
			System.out.println(hexObjCode);		
		}

	private void analyseWithADT(String assemblyLine) throws AssemblerException {
	
		ADT adt = data.getAdt();
		String adtRoot = adt.getRootTerm();
		
		ArrayList<String> rootTerm = new ArrayList<String>();
		rootTerm.add(adtRoot);
			
		ArrayList<ArrayList<String>> paths = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentPath = new ArrayList<String>();
		
		ArrayList<String> fullTermsIter = new ArrayList<String>();
		fullTermsIter.add(adtRoot);
		
		ArrayList<String> assemblyList = new ArrayList<String>();
		
		String[] assemblySplit = assemblyLine.split("\\s+");		//space 
	
		for (String str : assemblySplit) {
			
			if (!str.matches(",+")){
				
				str = str.replaceAll("^,+", "");
				str = str.replaceAll(",+$", "");
				
				assemblyList.add(str);
			}
		}
			
		analyseOperands(rootTerm, assemblyList, rootTerm, fullTermsIter, paths, currentPath, adtRoot);
		
		if(legitAdtPaths.isEmpty())
			throw new AssemblerException("Line not consistent with ADT");
	}

	private boolean analyseOperands(ArrayList<String> parseTerms,
			ArrayList<String> assemblyListIter, ArrayList<String> parseTermsIter, ArrayList<String> fullParseTermsIter,
			ArrayList<ArrayList<String>> paths, ArrayList<String> currentPath,
			String parent) {
	
		boolean done = false;
		
		if(debug){		
			System.out.println("--------------------");
			System.out.println("terms: "+parseTerms);
			System.out.println("parent: "+parent);
			System.out.println("paths: "+paths);
			System.out.println("curpath: "+currentPath);
			System.out.println("asslist: "+assemblyListIter);
			System.out.println("termsIter: "+parseTermsIter);
		}
	
		for (String parseTerm : parseTerms) {
			
			if(debug)
				System.out.println(parseTerm);
	
			String[] splitParseTerm = parseTerm.split("\\s+");			
	
			if (splitParseTerm.length > 1) { // more than one term, update iter
	
				ArrayList<String> splitParseTermList = new ArrayList<String>();
	
				for (String str : splitParseTerm) 
					splitParseTermList.add(str);				
				
				ArrayList<String> newParseTermsIter = updateTermsIter(splitParseTermList, parseTermsIter, parent);
				ArrayList<String> newFullParseTermsIter = updateTermsIter(splitParseTermList, fullParseTermsIter, parent);
				
				done = analyseOperands(splitParseTermList, assemblyListIter, newParseTermsIter, newFullParseTermsIter, paths, currentPath, parent);
	
				if (done)
					return true;
			} 
			
			else { // one term				
	
				String tempParseTerm = parseTerm.replaceAll("\\?|\\*|\\+", "");
				
				if(parseTerm.charAt(parseTerm.length()-1)=='+'){
					
					ArrayList<String> oneOrMoreParseTerm = new ArrayList<String>();	
					String oneOrMore = tempParseTerm + " " + tempParseTerm + "*";
					oneOrMoreParseTerm.add(oneOrMore);
					
					ArrayList<String> newParseTermsIter = updateTermsIter(oneOrMoreParseTerm, parseTermsIter, parseTerm);
					ArrayList<String> newFullParseTermsIter = updateTermsIter(oneOrMoreParseTerm, fullParseTermsIter, parseTerm);
					
					done = analyseOperands(oneOrMoreParseTerm, assemblyListIter, newParseTermsIter, newFullParseTermsIter, paths, currentPath, parent);
					
					if(done)
						return true;
				}
				
				ArrayList<String> adtTerms = data.getAdt().getAdtHash().get(tempParseTerm);
				
				ArrayList<String> newCurrentPath = clone(currentPath);					
				newCurrentPath.add(parseTerm);
	
				if (adtTerms != null) { // not leaf					
					
				    if(!(parent.charAt(parent.length()-1)=='*')){
						
						ArrayList<String> parseTerm1 = new ArrayList<String>();	
						parseTerm1.add(parseTerm);
					
						ArrayList<String> newParseTermsIter = updateTermsIter(parseTerm1, parseTermsIter, parent);
						ArrayList<String> newFullParseTermsIter = updateTermsIter(parseTerm1, fullParseTermsIter, parent);
					
						done = analyseOperands(adtTerms, assemblyListIter, newParseTermsIter, newFullParseTermsIter, paths, newCurrentPath, parseTerm);
					}					
					
					else
						done = analyseOperands(adtTerms, assemblyListIter, parseTermsIter, fullParseTermsIter, paths, newCurrentPath, parent);
						
					if (done)
						return true;
				} 
				
				else { // leaf
					
					String assemblyTerm = assemblyListIter.get(0);
	
					if (match(parseTerm, assemblyTerm, newCurrentPath)) {
						
						if(debug)						
							System.out.println("found: " + parseTerm);
						
						if(!legitIter(parseTermsIter, newCurrentPath))
							return false;
						
						ArrayList<ArrayList<String>> newPaths = clone2(paths);
						newPaths.add(newCurrentPath);
						
						parseTermsIter = updateTermsIter(parseTermsIter, newCurrentPath);
						assemblyListIter = removeFirstElement(assemblyListIter);
						
						newCurrentPath = new ArrayList<String>();						
	
						if (parseTermsIter.isEmpty() || assemblyListIter.isEmpty()) {
							
							if(parseTermsIter.isEmpty() && !assemblyListIter.isEmpty())
								return false;
							
							else if(!parseTermsIter.isEmpty() && assemblyListIter.isEmpty()){
								
								if(!legitWithFullTermsIter(fullParseTermsIter, newPaths))
									return false;
							}			
								
							legitAdtPaths = newPaths;		//legit							
							
							return true;							
						}
						
						done = analyseOperands(parseTermsIter, assemblyListIter, parseTermsIter, fullParseTermsIter, newPaths, newCurrentPath, data.getAdt().getRootTerm());
	
						if (done)
							return true;
					}
	
					else { // not found						
						
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
		
		for(String iterTerm: splitTermsIter){
			
			for(String pathTerm: currentPath){
				
				if(iterTerm.equals(pathTerm)){
					found = true;
					break;
				}			
			}			
			
			if(found && iterTerm.charAt(iterTerm.length()-1) == '*')
				return parseTermsIter;
			
			if(found){
				
				index++;
				break;
			}
			
			index++;
		}
		
		for(String iterTerm: splitTermsIter){
			
			if(index <= 0)
				newIterStr += iterTerm + " ";
			
			index--;			
		}
	
		newIterStr = newIterStr.trim();
	
		if (newIterStr != "")
			newTermsIter.add(newIterStr);
	
		return newTermsIter;
	}

	private ArrayList<String> updateTermsIter(ArrayList<String> splitParseTermList,
			ArrayList<String> parseTermsIter, String parent) {
		
		ArrayList<String> newTermsIter = new ArrayList<String>();
		
		String newIterStr = "";
		
		String termsIter = parseTermsIter.get(0);
		String[] splitTermsIter = termsIter.split("\\s+");
		
		boolean done = false;
		
		for (String iterTerm : splitTermsIter) {
	
			if(iterTerm.equals(parent) && !done){
				
				for(String str: splitParseTermList){
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
		
		for(String iterTerm: splitTermsIter){
			
			for(String pathTerm: newCurrentPath){
				
				if(iterTerm.equals(pathTerm)){
					legit = true;
					break;
				}
			}
			
			if(legit)
				return true;
			
			else if(!(iterTerm.charAt(iterTerm.length()-1) == '?') && !(iterTerm.charAt(iterTerm.length()-1) == '*'))
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

	private boolean legitWithFullTermsIter(ArrayList<String> fullParseTermsIter, ArrayList<ArrayList<String>> newPaths) {
		
		String str = fullParseTermsIter.get(0);
		String[] splitFullTermsIter = str.split("\\s+");
		
		boolean pathsFinished = false;
		int pathCounter = 0;
		ArrayList<String> path = null;
		
		for(String iterTerm: splitFullTermsIter){
			
			if(pathsFinished){
				
				if(!(iterTerm.charAt(iterTerm.length()-1)=='?' || iterTerm.charAt(iterTerm.length()-1)=='*'))
					return false;				
			}
			
			else{			
								
				path = newPaths.get(pathCounter);	
			
				if(iterTerm.charAt(iterTerm.length()-1)=='*'){
				
					while(legitPath(path, iterTerm)){
						
						pathCounter++;
					
						if(pathCounter > newPaths.size()-1){
							
							pathsFinished = true;
							break;
						}
					
						else					
							path = newPaths.get(pathCounter);
					}
				}			
			
				else if(!legitPath(path, iterTerm)){
				
					if(!(iterTerm.charAt(iterTerm.length()-1)=='?' || iterTerm.charAt(iterTerm.length()-1)=='*'))
						return false;
				}
			
				else{
					
					pathCounter++;
						
					if(pathCounter > newPaths.size()-1)
						pathsFinished = true;	
				}			
			}
		}
		
		return true;
	}

	private boolean legitPath(ArrayList<String> path, String iterTerm) {
	
		for(String pathTerm: path){
			
			if(iterTerm.equals(pathTerm))
				return true;
		}
		
		return false;
	}

	private boolean match(String adtTerm, String assemblyTerm, ArrayList<String> currentPath) {	
		
		if(adtTerm.startsWith("\"") && adtTerm.endsWith("\"")){
			
			adtTerm = adtTerm.replaceAll("\"", "");
			
			return adtTerm.equals(assemblyTerm);
		}		
			
		else
			return nestedMatch(adtTerm, assemblyTerm, currentPath);		
	}

	private boolean nestedMatch(String adtTerm, String assemblyTerm, ArrayList<String> currentPath) {
		
		String[] splitAdtTerms = adtTerm.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");
		
		String prefixes = "";
		
		for(String str: splitAdtTerms){
			
			if(!isAlphaNumeric(str))
				prefixes += "\\" + str;
		}		
	
		String[] splitAssemblyTerms;
		
		if(prefixes.isEmpty())
			splitAssemblyTerms = assemblyTerm.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");
		
		else
			splitAssemblyTerms = assemblyTerm.split("(?=["+prefixes+"])|(?<=["+prefixes+"])");
		
		if(splitAdtTerms.length != splitAssemblyTerms.length)
			return false;
		
		int i = 0;
		
		for(String term: splitAdtTerms){
			
			if(term.isEmpty() || splitAssemblyTerms[i].isEmpty()){
				
				if(!(term.isEmpty() && splitAssemblyTerms[i].isEmpty()))
					return false;
			}
			
			else if(!isAlphaNumeric(term)) {
				
				if(!term.equals(splitAssemblyTerms[i]))
					return false;
			}
			
			else if(term.equals(splitAssemblyTerms[i])){
				
			}
			
			else{
				
				boolean legit = false;
				ArrayList<String> adtTerms = data.getAdt().getAdtHash().get(term);
				
				if(adtTerms != null){						
					
					for(String termFromHash: adtTerms){
						
						if(match(termFromHash, splitAssemblyTerms[i], currentPath)){
							
							legit = true;
							break;
						}								
					}
					
					if(!legit)
						return false;
				}
				
				else if (data.getRegisterHash().get(splitAssemblyTerms[i]) != null
						|| data.getMnemonicTable().get(splitAssemblyTerms[i]) != null)
					return false;
					
				
				else if(term.equals("HEX")){
					
					if(!isAlphaNumeric(splitAssemblyTerms[i]))
						return false;
					
					assemblyTermTypeHash.put(splitAssemblyTerms[i], term);
				}
				
				else if(term.equals("INT")){
					
					if(!isNumeric(splitAssemblyTerms[i]))
						return false;
					
					assemblyTermTypeHash.put(splitAssemblyTerms[i], term);
				}
				
				else if(term.equals("LABEL")){
					
					if(!isAlpha(splitAssemblyTerms[i]))
						return false;
					
					assemblyTermTypeHash.put(splitAssemblyTerms[i], term);					
				}
				
				else{
					return false;
				}
			}				
		
			i++;			
		}
			
		currentPath.add(assemblyTerm);
		
		return true;					
	}

	private MnemonicData getMnemData(String assemblyLine) throws AssemblerException {
		
		String[] assemblyLineSplit = assemblyLine.split("\\s+");		//space 
		ArrayList<String> assemblyTermList = new ArrayList<String>();
	
		for (String assemblyTerm : assemblyLineSplit) {		
				
			assemblyTerm = assemblyTerm.replaceAll("^,+", "");
			assemblyTerm = assemblyTerm.replaceAll(",+$", "");
				
			assemblyTermList.add(assemblyTerm);			
		}
		
		MnemonicData mnemData = null;
		
		for(String assemblyTerm: assemblyTermList){
			
			if(data.getMnemonicTable().get(assemblyTerm) != null){
				
				mnemData = data.getMnemonicTable().get(assemblyTerm);
				break;
			}
		}
		
		if(mnemData == null)
			throw new AssemblerException("Unable to find mnemonic in mnemonicData.");		
		
		return mnemData;
	}

	private boolean formatMatch(String mnemFormat) {
		
		String[] splitFormat = mnemFormat.split("[,\\s]+");
		
		int i = 0;
		boolean legit = false;
		
		//TODO what if optional one in format defined?
		
		for(ArrayList<String> path: legitAdtPaths){
			
			for(String pathTerm: path){
				
				if(i >= splitFormat.length)
					return false;
				
				if(pathTerm.equals(splitFormat[i])){
					
					legit = true;
					i++;
					break;
				}	
				
				else if((pathTerm.charAt(pathTerm.length()-1) == '?')){
					
					legit = true;
					break;					
				}
			}
			
			if(!legit)
				return false;
			
			legit = false;		
		}
		
		return true;
	}

	private boolean correctSyntax(String format, String assemblyLine) {
		
		String[] formatSplit = format.split("(?=[,\\s]+)|(?<=[,\\s]+)");
		
		String regex = ".*";
		
		boolean inSpaces = false;
		
		for(String str: formatSplit){
	
			if(str.matches("\\s+")){
				
				if(!inSpaces){
					regex += "\\s+";
					inSpaces = true;
				}
			}
			
			else{
				
				inSpaces = false;
				
				if(str.equals(","))
						regex += ",";
				
				else
					regex += "[^\\s,]+";				
			}				
		}
		
		regex += ".*";
		
		boolean legitSyntax = Pattern.matches(regex,assemblyLine);	
		
		return legitSyntax;	
	}

	private String getLabelString() {	//assumes label in first path
		
		String label = "";
		boolean foundLabel = false;
		
		for(ArrayList<String> path: legitAdtPaths){
			
			for(String term: path){
				
				if(term.equals("LABEL"))
					foundLabel = true;
				
				if(foundLabel)
					label = term;					
			}
			
			break;
		}
		
		return label;
	}

	private String relativeJumpInBinary(String insHashTerm, int bits) {
		
		int locationCounter = insAdrTable.get(insNumber+1);
		int destination = symbolTable.get(insHashTerm);		

		int jump = destination-locationCounter;
		
		String binary = Integer.toBinaryString(jump);
		
		if(binary.length() > bits)
			binary = binary.substring(binary.length()-bits);
		
		return binary;
	}

	private String getRelevantOperands(String format) {
		
		String operands = "";
		String[] formatSplit = format.split("[,\\s]+");
		
		int i = 0;
		
		for(ArrayList<String> path: legitAdtPaths){
			
			for(String pathTerm: path){
				
				if(pathTerm.equals(formatSplit[i])){
					
					operands += getAssemblyOperand(path) + " ";
					i++;
					break;
				}	
			}	
		}
		
		return operands.trim();
	}

	private String getAssemblyOperand(ArrayList<String> path) {
		
		String operand = path.get(path.size()-1);
		
		operand = operand.replaceAll("\"", "");		
		
		return operand;
	}

	private HashMap<String, String> mapInsFieldLabels(String assemblyLine,
			String insLabels) {
		
		HashMap<String,String> insHash = new HashMap<String,String>();
		
		String[] splitAssemblyTerms = assemblyLine.split("[^a-zA-Z0-9]+");
		String[] splitInsTerms = insLabels.split("[^a-zA-Z0-9]+");
			
		int i = 0;
		
		for(String insTerm: splitInsTerms){		
				
			String assemblyTerm = splitAssemblyTerms[i];
			insHash.put(insTerm, assemblyTerm);			
			
			i++;
		}	
		
		return insHash;
	}

	private ArrayList<String> splitToMinAdrUnits(String binary) {
		
		ArrayList<String> binaryArray = new ArrayList<String>();
		
		int minAdrUnit = data.getMinAdrUnit();
		
		int index = 0;
		
		while (index < binary.length()) {
			
		    binaryArray.add(binary.substring(index, Math.min(index + minAdrUnit,binary.length())));
		    
		    index += minAdrUnit;
		}
		
		return binaryArray;
	}

	private String getHexObjCode(ArrayList<String> binaryArray) {
		
		String hexObjCode = "";
		
		int minAdrUnit = data.getMinAdrUnit();
		
		int noOfHexCharacters = (minAdrUnit/8)*2;
		
		if(data.getEndian().equals("big")){
			
			for(String str: binaryArray){ 
				
				String hex = binaryToHex(str);
				
				while(hex.length() < noOfHexCharacters)
					hex = "0" + hex;
				
				hexObjCode += hex + " ";
			}			
		}
		
		else if(data.getEndian().equals("little")){
			
			int counter = binaryArray.size()-1;
			
			for(; counter >= 0; counter--){
				
				String hex = binaryToHex(binaryArray.get(counter));
				
				while(hex.length() < noOfHexCharacters)
					hex = "0" + hex;
				
				hexObjCode += hex + " ";
			}
		}
		
		return hexObjCode;
	}

	private ArrayList<ArrayList<String>> clone2(
			ArrayList<ArrayList<String>> sourceList) {
		
		ArrayList<ArrayList<String>> newList = new ArrayList<ArrayList<String>>();
		
		for (ArrayList<String> list : sourceList){
			
			ArrayList<String> temp = new ArrayList<String>();
			
			for(String str: list)
				temp.add(str);
			
			newList.add(temp);
		}
		
		return newList;
	}

	private ArrayList<String> clone(ArrayList<String> sourceList) {
		
		ArrayList<String> newList = new ArrayList<String>();
		
		for(String str: sourceList)
			newList.add(str);
		
		return newList;
	}

	public static String binaryFromIntFormatted(String intStr, int bits) {

		String binary = intToBinary(intStr);

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

	public static String intToBinary(String intStr) {

		int decimalInt = Integer.parseInt(intStr);
		String binary = Integer.toBinaryString(decimalInt);

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
}
