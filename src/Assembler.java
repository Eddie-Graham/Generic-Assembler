/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Assembler {

	private DataSource data;
	private ArrayList<String> objectCode;
	private ArrayList<ArrayList<String>> legitPaths;
	private boolean atBss, atData, atText;
	private boolean first, second;
	private boolean debug = false;

	public Assembler(DataSource data) {

		this.data = data;
		objectCode = new ArrayList<String>();
		legitPaths = new ArrayList<ArrayList<String>>();
		
		atBss = false;
		atData = false;
		atText = false;
		
		first = false;
		second = false;

		assemble();
	}

	private void assemble() {
		
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
							System.out.println(e.getMessage());
							System.out.println("Line: " + assemblyLine.trim());
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

	private void populateInstruction(String assemblyLine) throws AssemblerException {
		
		legitPaths = new ArrayList<ArrayList<String>>();

		System.out.println("*****************************");
		System.out.println(assemblyLine.trim());
		System.out.println("*****************************");

		analyseWithADT(assemblyLine);
		
		MnemonicData mnemData = getMnemData(assemblyLine);		
		
		ArrayList<String> mnemTypes = mnemData.getMnemTypes();		// get mnem types
		String mnemType = "";
		
		for(String type: mnemTypes){
			
			if(checkType(type))
				mnemType = type;	// find type			
		}
		
		if(mnemType == "")
			throw new AssemblerException("Mnem type mismatch.");
		
		MnemType type = mnemData.getMnemTypeHash().get(mnemType);		// get type data		
		String insLabels = type.getInsLabels();
		
		HashMap<String, String> insHash = createInsHash(assemblyLine, insLabels);	
		
		ArrayList<String> instructionFormat = type.getInstructionFormat();	// gets instructions
		
		String binary = "";
		
		for(String ins: instructionFormat){
			
			InstructionFormatData insFormat = data.getInstructionFormat().get(ins);
			
			ArrayList<String> instructions = insFormat.getOperands();
			
			for(String insTerm: instructions){
				
				if(mnemData.getGlobalOpCodes().get(insTerm)!= null)
					binary += mnemData.getGlobalOpCodes().get(insTerm) + " ";				
				
				else if(type.getOpCodes().get(insTerm) != null)
					binary += type.getOpCodes().get(insTerm) + " ";
				
				else{
					String reg = insHash.get(insTerm);
					binary += data.getRegisterHash().get(reg) + " ";
				}
			}
		}		
		
		System.out.println(binary);
	}

	private MnemonicData getMnemData(String assemblyLine) throws AssemblerException {
		
		String[] assemblySplit = assemblyLine.split("\\s+");		//space 
		ArrayList<String> assemblyList = new ArrayList<String>();

		for (String str : assemblySplit) {
			
			if (!str.isEmpty()){
				
				if(str.startsWith(","))
					str = str.substring(1);
			
				if(str.endsWith(","))
					str = str.substring(0, str.length()-1);
				
				assemblyList.add(str);
			}
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

	private HashMap<String, String> createInsHash(String assemblyLine,
			String insLabels) {
		
		HashMap<String,String> insHash = new HashMap<String,String>();
		
		String assemblyLineTrim = assemblyLine.trim();
		String[] splitAssemblyTerms = assemblyLineTrim.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");
		String[] splitInsTerms = insLabels.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");
			
		int i = 0;
		
		for(String term: splitAssemblyTerms){
			
			if(isAlphaNumeric(term)){
				
				String insTerm = splitInsTerms[i];
				insHash.put(insTerm, term);
			}
			
			i++;
		}	
		
		return insHash;
	}

	private boolean checkType(String type) {
		
		String[] tokens = type.replaceAll("^[,\\s]+", "").split("[,\\s]+");
		
		int i = 0;
		boolean found = false;
		
		for(ArrayList<String> path: legitPaths){
			
			for(String term: path){
				
				if(term.equals(tokens[i])){
					
					found = true;
					i++;
					break;
				}				
			}
			
			if(!found)
				return false;
			
			found = false;		
		}
		
		return true;
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

		for (String str : assemblySplit) {
			
			if (!str.isEmpty()){
				
				if(str.startsWith(","))
					str = str.substring(1);
			
				if(str.endsWith(","))
					str = str.substring(0, str.length()-1);
				
				assemblyList.add(str);
			}
		}
			
		analyseOperands(rootTermList, assemblyList, rootTermList, paths, currentPath, adtRoot);
		
		if(legitPaths.isEmpty())
			throw new AssemblerException("Line not consistent with ADT");
	}

	private boolean analyseOperands(ArrayList<String> terms,
			ArrayList<String> assemblyList, ArrayList<String> termsIter,
			ArrayList<ArrayList<String>> paths, ArrayList<String> currentPath,
			String parent) {

		boolean done = false;
		
		if(debug){		
			System.out.println("--------------------");
			System.out.println("terms: "+terms);
			System.out.println("asslist: "+assemblyList);
			System.out.println("termsIter: "+termsIter);
			System.out.println("paths: "+paths);
			System.out.println("curpath: "+currentPath);
			System.out.println("parent: "+parent);
			System.out.println();
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

				done = analyseOperands(splitTermList, assemblyList, newTermsIter, paths, currentPath, parent);

				if (done)
					return true;
			} 
			
			else { // one term

				String tempTerm = term.replaceAll("\\?|\\*", "");
				
				ArrayList<String> termsListFromHash = data.getAdt().getAdtHash().get(tempTerm);

				if (termsListFromHash != null) { // not leaf
					
					ArrayList<String> newCurrentPath = clone(currentPath);					
					newCurrentPath.add(term);
					
					done = analyseOperands(termsListFromHash, assemblyList, termsIter, paths, newCurrentPath, term);

					if (done)
						return true;
				} 
				
				else { // leaf
					
					String assemblyTerm = assemblyList.get(0);

					if (match(term, assemblyTerm)) {
						
						if(debug)						
							System.out.println("found: " + term);

						currentPath.add(term);
						
						if(!legitIter(termsIter, currentPath))
							return false;
						
						ArrayList<ArrayList<String>> newPaths = clone2(paths);
						newPaths.add(currentPath);
						
						termsIter = updateTermsIter(termsIter, currentPath);
						assemblyList = removeFirstElement(assemblyList);
						
						currentPath = new ArrayList<String>();						

						if (termsIter.isEmpty() || assemblyList.isEmpty()) {
							
							if(termsIter.isEmpty() && !assemblyList.isEmpty())
								return false;
							
							else if(!termsIter.isEmpty() && assemblyList.isEmpty()){
								
								if(!zeroOrMore(termsIter))
									return false;
							}			
								
							legitPaths = newPaths;		//legit
//							paths = new ArrayList<ArrayList<String>>();							
							
							return true;							
						}
						
						done = analyseOperands(termsIter, assemblyList, termsIter, newPaths, currentPath, data.getAdt().getRootTerm());

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

	private boolean notInTermsIter(String term, ArrayList<String> termsIter) {
		
		String st = termsIter.get(0);
		String[] split = st.split("\\s+");
		
		for(String str: split){			
			
			if(term.equals(str))
				return false;
				
			else
				return true;
			
		}
		
		return false;		
	}

	private boolean zeroOrMore(ArrayList<String> termsIter) {
		
		String st = termsIter.get(0);
		String[] splitTermsIter = st.split("\\s+");
		
		for(String str: splitTermsIter){
			
			if(!(str.charAt(str.length()-1) == '?') && !(str.charAt(str.length()-1) == '*'))
				return false;
		}		
		
		return true;
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
	
	private boolean match(String term, String assemblyTerm) {	
		
		if(term.startsWith("\"") && term.endsWith("\"")){
			
			term = term.replaceAll("\"", "");
			
			return term.equals(assemblyTerm);
		}		
			
		else
			return legitMatch(term, assemblyTerm);		
	}

	private boolean legitMatch(String term, String assemblyTerm) {
		
		String[] splitTerms = term.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");
		
		String[] splitAssemblyTerms = assemblyTerm.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");		
		
		if(splitTerms.length != splitAssemblyTerms.length)
			return false;

		int i = 0;			
			
		for(String str: splitTerms){
				
			if(str.isEmpty() || splitAssemblyTerms[i].isEmpty()){
					
				if(!(str.isEmpty() && splitAssemblyTerms[i].isEmpty()))
					return false;
			}
			
			else{					
				
				boolean legit = false;
				
				if(!isAlphaNumeric(str)) {
					
					if(!str.equals(splitAssemblyTerms[i]))
						return false;
				}
				
				else{
					ArrayList<String> termsListFromHash = data.getAdt().getAdtHash().get(str);
					
					if(termsListFromHash != null){						
						
						for(String termFromHash: termsListFromHash){
							
							if(match(termFromHash, splitAssemblyTerms[i])){
								legit = true;
								break;
							}								
						}
						
						if(!legit)
							return false;
					}
					
					else if(!(str.equals("HEX") | str.equals("DECIMAL")))
						return false;										
				}				
			}
				
			i++;
		}
			
		return true;					
	}

	private ArrayList<String> removeEmptyElements(String[] splitAssemblyTerm) {
		
		ArrayList<String> newArray = new ArrayList<String>();
		
		for(String str: splitAssemblyTerm){
			
			if(!str.isEmpty())
				newArray.add(str);
		}
		
		return newArray;
	}

	private ArrayList<String> updateTermsIter(ArrayList<String> splitTermList,
			ArrayList<String> termsIter, String parent) {
		
		ArrayList<String> newTermsIter = new ArrayList<String>();
		
		String newStr = "";
		
		String st = termsIter.get(0);
		String[] split = st.split("\\s+");
		
		for (String str : split) {

			if(str.equals(parent)){
				
				for(String str2: splitTermList)
					newStr += str2 + " ";				
			}
			
			else
				newStr += str + " ";
		}
		
		newStr = newStr.trim();
		newTermsIter.add(newStr);
		
		return newTermsIter;
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

	private String getBinaryFromNumSys(String value, DataSource.OperandType opType, int bits) {

		String binary = "";

		if (opType.getSys() == DataSource.TypeNumSystem.DECIMAL) 
			binary = binaryFromDecimalFormatted(value, bits);
		
		else if (opType.getSys() == DataSource.TypeNumSystem.HEX) 
			binary = binaryFromHexFormatted(value, bits);
		
		return binary;
	}

	public static String binaryFromDecimalFormatted(String decimal, int bits) {

		String binary = decimalToBinary(decimal);

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

	public static String decimalToBinary(String decimal) {

		int decimalInt = Integer.parseInt(decimal);
		String binary = Integer.toBinaryString(decimalInt);

		return binary;
	}

	private boolean isAlphaNumeric(String s) {
		
		String pattern = "^[a-zA-Z0-9]*$";
		
		if (s.matches(pattern)) 
			return true;
		
		return false;
	}
}
