/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class Mnemonic {
	
	private String mnemonic;
	private HashMap<String,String> globalFieldEncodingHash;
	private HashMap<String,OperandFormat> operandFormatHash;
	private ArrayList<String> operandsFormats;
	private ArrayList<String> rawLines;
	private String rawGlobalFieldEncodingString;
	private String rawLinesString;
	
	public Mnemonic(){
		
		mnemonic = "";
		globalFieldEncodingHash = new HashMap<String, String>();
		operandFormatHash = new HashMap<String, OperandFormat>();
		operandsFormats = new ArrayList<String>();
		rawGlobalFieldEncodingString = "";
		rawLinesString = "";
		rawLines = new ArrayList<String>();
	}
	
	public void addToRawLines(String str){
		
		this.rawLines.add(str);
		this.rawLinesString += str + "\n";
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public HashMap<String, String> getGlobalFieldEncodingHash() {
		return globalFieldEncodingHash;
	}

	public void setGlobalFieldEncodingHash(
			HashMap<String, String> globalFieldEncodingHash) {
		this.globalFieldEncodingHash = globalFieldEncodingHash;
	}

	public HashMap<String, OperandFormat> getOperandFormatHash() {
		return operandFormatHash;
	}

	public void setOperandFormatHash(
			HashMap<String, OperandFormat> operandFormatHash) {
		this.operandFormatHash = operandFormatHash;
	}

	public ArrayList<String> getOperandsFormats() {
		return operandsFormats;
	}

	public void setOperandsFormats(ArrayList<String> operandsFormats) {
		this.operandsFormats = operandsFormats;
	}

	public ArrayList<String> getRawLines() {
		return rawLines;
	}

	public void setRawLines(ArrayList<String> rawLines) {
		this.rawLines = rawLines;
	}

	public String getRawGlobalFieldEncodingString() {
		return rawGlobalFieldEncodingString;
	}

	public void setRawGlobalFieldEncodingString(String rawGlobalFieldEncodingString) {
		this.rawGlobalFieldEncodingString = rawGlobalFieldEncodingString;
	}

	public String getRawLinesString() {
		return rawLinesString;
	}

	public void setRawLinesString(String rawLinesString) {
		this.rawLinesString = rawLinesString;
	}	
}
