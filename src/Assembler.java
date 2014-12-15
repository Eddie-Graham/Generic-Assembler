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
		
		reset();

		System.out.println("*****************************");
		System.out.println(assemblyLine.trim());
		System.out.println("*****************************");

//		String[] tokens = assemblyLine.split("\\s+");
//		String mnemonic = tokens[0];
//		MnemonicFormatData op = data.getMnemonicTable().get(mnemonic);

		analyseWithADT(assemblyLine);
		
		System.out.println(legitPaths);
	}

	private void reset() {
		
		legitPaths = new ArrayList<ArrayList<String>>();		
	}

	private void analyseWithADT(String assemblyLine) throws AssemblerException {

		ADT adt = data.getAdt();
		String adtRoot = adt.getRootTerm();
		
		ArrayList<String> rootTermList = new ArrayList<String>();
		rootTermList.add(adtRoot);

		String[] assemblySplit = assemblyLine.split("\\s+|,");
		
		ArrayList<ArrayList<String>> paths = new ArrayList<ArrayList<String>>();
		ArrayList<String> currentPath = new ArrayList<String>();
		ArrayList<String> assemblyList = new ArrayList<String>();

		for (String str : assemblySplit) {
			
			if (!str.isEmpty())
				assemblyList.add(str);
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
							paths = new ArrayList<ArrayList<String>>();							
							
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
		
		term = term.replaceAll("\"", "");
		
		if(term.equals(assemblyTerm))
			return true;
		
		else if(checkTerm(term, assemblyTerm, "HEX"))
			return true;
		
		else if(checkTerm(term, assemblyTerm, "DECIMAL"))
			return true;
			
		else
			return checkNestedTerm(term, assemblyTerm);		
	}

	private boolean checkNestedTerm(String term, String assemblyTerm) {
		
		String[] splitTerms = term.split("(?=[^a-zA-Z0-9])|(?<=[^a-zA-Z0-9])");

		String prefix = "";		
		boolean first = true;
		
		for(String str: splitTerms){	
			
			if(!isAlphaNumeric(str)) {	
				
				if(!first)
					prefix += "|";
				
				prefix += "\\"+ str;
				
				first = false;
			}				
		}
	
		String[] splitAssemblyTerms = assemblyTerm.split("(?="+prefix+")|(?<="+prefix+")");
		
		if(splitTerms.length == splitAssemblyTerms.length){

			int i = 0;			
			
			for(String str: splitTerms){
				
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
					
					else
						return false;					
				}	
				
				i++;
			}
			
			return true;			
		}
		else
			return false;		
	}

	private boolean checkTerm(String term, String assemblyTerm, String type) {
		
		String prefix = "";
		
		String[] splitHexTerm = term.split("(?=" + type + ")|(?<=" + type + ")");
		
		if(splitHexTerm.length > 1){
		
			for(String str: splitHexTerm){
				
				if(!str.equals(type))
					prefix += str;				
			}
			
			String[] splitAssemblyTerm = assemblyTerm.split("(?="+prefix+")|(?<="+prefix+")");
			
			if(splitAssemblyTerm.length > 1){
			
				ArrayList<String> splitAssemblyList = removeEmptyElements(splitAssemblyTerm);
				
				if(splitHexTerm.length == splitAssemblyList.size()){
					
					int i = 0;
					
					for(String str: splitHexTerm){
						
						if(str.equals(type)){
							
							if(!isAlphaNumeric(splitAssemblyList.get(i)))
								return false;														
						}
						
						else{
							if(!str.equals(splitAssemblyList.get(i)))
								return false;
						}
						
						i++;
					}
				
					return true;
				}				
			}
		}
		
		return false;
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
