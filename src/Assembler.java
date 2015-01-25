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

public class Assembler {

	private DataSource data;
	
	private int locationCounter;
	private int insNumber;
	private HashMap<Integer, Integer> insAdrTable;
	private HashMap<String, Integer> symbolTable;
	
	private ArrayList<ArrayList<String>> legitPaths;
	private HashMap<String, String> assemblyTypeHash;
	
	private boolean atBss, atData, atText;
	private boolean first, second;
	
	private boolean debug = false;

	public Assembler(DataSource data) {

		this.data = data;
		
		locationCounter = 0;
		insNumber = 0;
		insAdrTable = new HashMap<Integer, Integer>();
		symbolTable = new HashMap<String, Integer>();

		legitPaths = new ArrayList<ArrayList<String>>();
		assemblyTypeHash = new HashMap<String, String>();
		
		atBss = false;
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
					setBooleanValues(false, true, false);
			
				else if(assemblyLine.startsWith("section .bss"))
					setBooleanValues(true, false, false);
			
				else if(assemblyLine.startsWith("section .text")){
					setBooleanValues(false, false, true);
					first = true;
				}
			
				else if (atData){
				
				}
				
				else if (atBss){
				
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
					setBooleanValues(false, true, false);
			
				else if(assemblyLine.startsWith("section .bss"))
					setBooleanValues(true, false, false);
			
				else if(assemblyLine.startsWith("section .text")){
					setBooleanValues(false, false, true);
					first = true;
				}
			
				else if (atData){
				
				}
				
				else if (atBss){
				
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
						}
					}				
				}
			}
		}		
	}

	private void setBooleanValues(boolean atBss, boolean atData, boolean atText) {
		
		this.atBss = atBss;
		this.atData = atData;
		this.atText = atText;
	}

	private void analyseInstructionForLabels(String assemblyLine) throws AssemblerException {
		
		legitPaths = new ArrayList<ArrayList<String>>();
		assemblyTypeHash = new HashMap<String,String>();	
		
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
	
		for(String ins: instructionFormat){
			
			InstructionFormatData insFormat = data.getInstructionFormat().get(ins);
			
			ArrayList<String> instructions = insFormat.getOperands();
			
			for(String insField: instructions){
				
				int bits = insFormat.getOperandBitHash().get(insField);
				
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

	private void analyseWithADT(String assemblyLine) throws AssemblerException {
	
		ADT adt = data.getAdt();
		String adtRoot = adt.getRootTerm();
		
		ArrayList<String> rootTermList = new ArrayList<String>();
		rootTermList.add(adtRoot);
	
		String[] assemblySplit = assemblyLine.split("\\s+");		//space 
		
		ArrayList<ArrayList<String>> paths = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentPath = new ArrayList<String>();
		ArrayList<String> assemblyList = new ArrayList<String>();
		ArrayList<String> fullTermsIter = new ArrayList<String>();
		fullTermsIter.add(adtRoot);
	
		for (String str : assemblySplit) {
			
			if (!str.matches(",+")){
				
				str = str.replaceAll("^,+", "");
				str = str.replaceAll(",+$", "");
				
				assemblyList.add(str);
			}
		}
			
		analyseOperands(rootTermList, assemblyList, rootTermList, fullTermsIter, paths, currentPath, adtRoot);
		
		if(legitPaths.isEmpty())
			throw new AssemblerException("Line not consistent with ADT");
	}

	private boolean analyseOperands(ArrayList<String> terms,
			ArrayList<String> assemblyList, ArrayList<String> termsIter, ArrayList<String> fullTermsIter,
			ArrayList<ArrayList<String>> paths, ArrayList<String> currentPath,
			String parent) {
	
		boolean done = false;
		
		if(debug){		
			System.out.println("--------------------");
			System.out.println("terms: "+terms);
			System.out.println("parent: "+parent);
			System.out.println("paths: "+paths);
			System.out.println("curpath: "+currentPath);
			System.out.println("asslist: "+assemblyList);
			System.out.println("termsIter: "+termsIter);
		}
	
		for (String term : terms) {
			
			if(debug)
				System.out.println(term);
	
			String[] splitTerm = term.split("\\s+");			
	
			if (splitTerm.length > 1) { // more than one term, update iter
	
				ArrayList<String> splitTermList = new ArrayList<String>();
	
				for (String str : splitTerm) 
					splitTermList.add(str);				
				
				ArrayList<String> newTermsIter = updateTermsIter(splitTermList, termsIter, parent);
				ArrayList<String> newFullTermsIter = updateTermsIter(splitTermList, fullTermsIter, parent);
				
				done = analyseOperands(splitTermList, assemblyList, newTermsIter, newFullTermsIter, paths, currentPath, parent);
	
				if (done)
					return true;
			} 
			
			else { // one term				
	
				String tempTerm = term.replaceAll("\\?|\\*|\\+", "");
				
				if(term.charAt(term.length()-1)=='+'){
					
					ArrayList<String> splitTermList = new ArrayList<String>();	
					String oneOrMore = tempTerm + " " + tempTerm + "*";
					splitTermList.add(oneOrMore);
					
					ArrayList<String> newTermsIter = updateTermsIter(splitTermList, termsIter, term);
					ArrayList<String> newFullTermsIter = updateTermsIter(splitTermList, fullTermsIter, term);
					
					done = analyseOperands(splitTermList, assemblyList, newTermsIter, newFullTermsIter, paths, currentPath, parent);
					
					if(done)
						return true;
				}
				
				ArrayList<String> termsListFromHash = data.getAdt().getAdtHash().get(tempTerm);
				
				ArrayList<String> newCurrentPath = clone(currentPath);					
				newCurrentPath.add(term);
	
				if (termsListFromHash != null) { // not leaf					
					
				    if(!(parent.charAt(parent.length()-1)=='*')){
						
						ArrayList<String> splitTermList = new ArrayList<String>();	
						splitTermList.add(term);
					
						ArrayList<String> newTermsIter = updateTermsIter(splitTermList, termsIter, parent);
						ArrayList<String> newFullTermsIter = updateTermsIter(splitTermList, fullTermsIter, parent);
					
						done = analyseOperands(termsListFromHash, assemblyList, newTermsIter, newFullTermsIter, paths, newCurrentPath, term);
					}					
					
					else
						done = analyseOperands(termsListFromHash, assemblyList, termsIter, fullTermsIter, paths, newCurrentPath, parent);
						
					if (done)
						return true;
				} 
				
				else { // leaf
					
					String assemblyTerm = assemblyList.get(0);
	
					if (match(term, assemblyTerm, newCurrentPath)) {
						
						if(debug)						
							System.out.println("found: " + term);
						
						if(!legitIter(termsIter, newCurrentPath))
							return false;
						
						ArrayList<ArrayList<String>> newPaths = clone2(paths);
						newPaths.add(newCurrentPath);
						
						termsIter = updateTermsIter(termsIter, newCurrentPath);
						assemblyList = removeFirstElement(assemblyList);
						
						newCurrentPath = new ArrayList<String>();						
	
						if (termsIter.isEmpty() || assemblyList.isEmpty()) {
							
							if(termsIter.isEmpty() && !assemblyList.isEmpty())
								return false;
							
							else if(!termsIter.isEmpty() && assemblyList.isEmpty()){
								
								if(!legitWithFullTermsIter(fullTermsIter, newPaths))
									return false;
							}			
								
							legitPaths = newPaths;		//legit							
							
							return true;							
						}
						
						done = analyseOperands(termsIter, assemblyList, termsIter, fullTermsIter, newPaths, newCurrentPath, data.getAdt().getRootTerm());
	
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

	private ArrayList<String> updateTermsIter(ArrayList<String> termsIter, ArrayList<String> currentPath) {
	
		ArrayList<String> newList = new ArrayList<String>();
		String newStr = "";
	
		String st = termsIter.get(0);
	
		String[] split = st.split("\\s+");
		
		boolean found = false;
		int index = 0;
		
		for(String str: split){
			
			for(String str2: currentPath){
				
				if(str.equals(str2)){
					found = true;
					break;
				}			
			}			
			
			if(found && str.charAt(str.length()-1) == '*')
				return termsIter;
			
			if(found){
				index++;
				break;
			}
			
			index++;
		}
		
		for(String str: split){
			
			if(index <= 0)
				newStr += str + " ";
			
			index--;			
		}
	
		newStr = newStr.trim();
	
		if (newStr != "")
			newList.add(newStr);
	
		return newList;
	}

	private ArrayList<String> updateTermsIter(ArrayList<String> splitTermList,
			ArrayList<String> termsIter, String parent) {
		
		ArrayList<String> newTermsIter = new ArrayList<String>();
		
		String newStr = "";
		
		String st = termsIter.get(0);
		String[] split = st.split("\\s+");
		
		boolean done = false;
		
		for (String str : split) {
	
			if(str.equals(parent) && !done){
				
				for(String str2: splitTermList){
					newStr += str2 + " ";	
					done = true;
				}				
			}
			
			else
				newStr += str + " ";
		}
		
		newStr = newStr.trim();
		newTermsIter.add(newStr);
		
		return newTermsIter;
	}

	private boolean legitIter(ArrayList<String> termsIter,
			ArrayList<String> currentPath) {
		
		boolean legit = false;
		
		String st = termsIter.get(0);
		String[] splitTermsIter = st.split("\\s+");
		
		for(String str: splitTermsIter){
			
			for(String str2: currentPath){
				
				if(str.equals(str2)){
					legit = true;
					break;
				}
			}
			
			if(legit)
				return true;
			
			else if(!(str.charAt(str.length()-1) == '?') && !(str.charAt(str.length()-1) == '*'))
				return false;	
		}
		
		return false;
	}

	private ArrayList<String> removeFirstElement(ArrayList<String> list) {
	
		boolean first = true;
		ArrayList<String> newList = new ArrayList<String>();
	
		for (String st : list) {
			
			if (first) 
				first = false;
			
			else 
				newList.add(st);			
		}
		
		return newList;
	}

	private boolean legitWithFullTermsIter(ArrayList<String> fullTermsIter, ArrayList<ArrayList<String>> paths) {
		
		String str = fullTermsIter.get(0);
		String[] splitTermsIter = str.split("\\s+");
		
		boolean pathsFinished = false;
		int pathCounter = 0;
		ArrayList<String> path = null;
		
		for(String iterTerm: splitTermsIter){
			
			if(pathsFinished){
				
				if(!(iterTerm.charAt(iterTerm.length()-1)=='?' || iterTerm.charAt(iterTerm.length()-1)=='*'))
					return false;				
			}
			
			else{			
								
				path = paths.get(pathCounter);	
			
				if(iterTerm.charAt(iterTerm.length()-1)=='*'){
				
					while(legitPath(path, iterTerm)){
						
						pathCounter++;
					
						if(pathCounter > paths.size()-1){
							
							pathsFinished = true;
							break;
						}
					
						else					
							path = paths.get(pathCounter);
					}
				}			
			
				else if(!legitPath(path, iterTerm)){
				
					if(!(iterTerm.charAt(iterTerm.length()-1)=='?' || iterTerm.charAt(iterTerm.length()-1)=='*'))
						return false;
				}
			
				else{
					
					pathCounter++;
						
					if(pathCounter > paths.size()-1)
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
			splitAssemblyTerms = assemblyTerm.split("(?=[" + prefixes + "])|(?<=[" + prefixes + "])");
		
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
				ArrayList<String> termsListFromHash = data.getAdt().getAdtHash().get(term);
				
				if(termsListFromHash != null){						
					
					for(String termFromHash: termsListFromHash){
						
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
					
					assemblyTypeHash.put(splitAssemblyTerms[i], term);
				}
				
				else if(term.equals("INT")){
					
					if(!isNumeric(splitAssemblyTerms[i]))
						return false;
					
					assemblyTypeHash.put(splitAssemblyTerms[i], term);
				}
				
				else if(term.equals("LABEL")){
					
					if(!isAlpha(splitAssemblyTerms[i]))
						return false;
					
					assemblyTypeHash.put(splitAssemblyTerms[i], term);					
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
		
		String[] assemblySplit = assemblyLine.split("\\s+");		//space 
		ArrayList<String> assemblyList = new ArrayList<String>();
	
		for (String str : assemblySplit) {		
				
			str = str.replaceAll("^,+", "");
			str = str.replaceAll(",+$", "");
				
			assemblyList.add(str);			
		}
		
		MnemonicData mnemData = null;
		
		for(String term: assemblyList){
			
			if(data.getMnemonicTable().get(term) != null){
				
				mnemData = data.getMnemonicTable().get(term);
				break;
			}
		}
		
		if(mnemData == null)
			throw new AssemblerException("Unable to find mnemonic data.");		
		
		return mnemData;
	}

	private boolean formatMatch(String format) {
		
		String[] tokens = format.split("[,\\s]+");
		
		int i = 0;
		boolean legit = false;
		
		//TODO what if optional one in format defined?
		
		for(ArrayList<String> path: legitPaths){
			
			for(String term: path){
				
				if(term.equals(tokens[i])){
					
					legit = true;
					i++;
					break;
				}	
				
				else if((term.charAt(term.length()-1) == '?')){
					
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

	private boolean correctSyntax(String mnemFormat, String assemblyLine) {
		
		
		String[] tokens = mnemFormat.split("(?=[,\\s]+)|(?<=[,\\s]+)");
		
		String regex = ".*";
		
		boolean inSpaces = false;
		
		for(String token: tokens){
	
			if(token.matches("\\s+")){
				
				if(!inSpaces){
					regex += "\\s+";
					inSpaces = true;
				}
			}
			
			else{
				
				inSpaces = false;
				
				if(token.equals(","))
						regex += ",";
				
				else
					regex += "[^\\s,]+";				
			}				
		}
		
		regex += ".*";
		
		boolean legitSyntax = Pattern.matches(regex,assemblyLine);	
		
		return legitSyntax;	
	}

	private String getLabelString() {
		
		String label = "";
		boolean foundLabel = false;
		
		for(ArrayList<String> path: legitPaths){
			
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

	private void populateInstruction(String assemblyLine) throws AssemblerException {		
		
		assemblyLine = assemblyLine.trim();
		
		System.out.println("*****************************");
		System.out.println(assemblyLine);
		
		legitPaths = new ArrayList<ArrayList<String>>();
		assemblyTypeHash = new HashMap<String,String>();
	
		analyseWithADT(assemblyLine);
		
		System.out.println(legitPaths);
		
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
		
		String insLabels = format.getInsLabels();		
		String operandsForWork = getOperandsForWork(mnemFormat);
		HashMap<String, String> insHash = createInsHash(operandsForWork, insLabels);	
		
		ArrayList<String> instructionFormat = format.getInstructionFormat();	// gets instructions
		
		String binary = "";
		insNumber ++;
		
		System.out.println("insHash: " + insHash);
		System.out.println("assTypeHash: " + assemblyTypeHash);
		
		for(String ins: instructionFormat){
			
			InstructionFormatData insFormat = data.getInstructionFormat().get(ins);
			
			ArrayList<String> instructions = insFormat.getOperands();
			
			for(String insField: instructions){
				
				String binaryTemp = "";
				
				int bits = insFormat.getOperandBitHash().get(insField);
				
				if(mnemData.getGlobalOpCodes().get(insField)!= null)	//global
					binaryTemp = mnemData.getGlobalOpCodes().get(insField);				
				
				else if(format.getOpCodes().get(insField) != null)	//local
					binaryTemp = format.getOpCodes().get(insField);
				
				else{
					
					String insHashTerm = insHash.get(insField);
					
					if(data.getRegisterHash().get(insHashTerm) != null)	// reg
						binaryTemp = data.getRegisterHash().get(insHashTerm);
					
					else{	// imm etc
						
						String numType = assemblyTypeHash.get(insHashTerm);
						
						if(numType.equals("INT"))
							binaryTemp = intToBinary(insHashTerm);
						
						else if(numType.equals("HEX"))
							binaryTemp = hexToBinary(insHashTerm);
						
						else if(numType.equals("LABEL")){
							binaryTemp = relativeJumpInBinary(insHashTerm, bits);
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

	private String relativeJumpInBinary(String insHashTerm, int bits) {
		
		int locationCounter = insAdrTable.get(insNumber+1);
		int destination = symbolTable.get(insHashTerm);		

		int jump = destination-locationCounter;
		
		String binary = Integer.toBinaryString(jump);
		
		if(binary.length() > bits)
			binary = binary.substring(binary.length()-bits);
		
		return binary;
	}

	private String getOperandsForWork(String format) {
		
		String operands = "";
		String[] tokens = format.split("[,\\s]+");
		
		int i = 0;
		
		for(ArrayList<String> path: legitPaths){
			
			for(String term: path){
				
				if(term.equals(tokens[i])){
					
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

	private HashMap<String, String> createInsHash(String assemblyLine,
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
		
		ArrayList<String> byteArray = new ArrayList<String>();
		
		int minAdrUnit = data.getMinAdrUnit();
		
		int index = 0;
		
		while (index < binary.length()) {
			
		    byteArray.add(binary.substring(index, Math.min(index + minAdrUnit,binary.length())));
		    
		    index += minAdrUnit;
		}
		
		return byteArray;
	}

	private String getHexObjCode(ArrayList<String> byteArray) {
		
		String hexObjCode = "";
		
		if(data.getEndian().equals("big")){
			
			for(String str: byteArray){ 
				
				String hex = binaryToHex(str);
				
				if(hex.length() == 1)
					hex = "0" + hex;
				
				hexObjCode += hex + " ";
			}			
		}
		
		else if(data.getEndian().equals("little")){
			
			int counter = byteArray.size()-1;
			
			for(; counter >= 0; counter--){
				
				String hex = binaryToHex(byteArray.get(counter));
				
				if(hex.length() == 1)
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
