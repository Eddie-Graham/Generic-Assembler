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
	private HashMap<String,String> globalOpCodes;
	private HashMap<String,OperandFormat> mnemFormatHash;
	private ArrayList<String> mnemFormats;
	private ArrayList<String> rawLines;
	private String rawGlobalOpcodesString;
	private String rawLinesString;
	
	public Mnemonic(){
		
		mnemonic = "";
		globalOpCodes = new HashMap<String, String>();
		mnemFormatHash = new HashMap<String, OperandFormat>();
		mnemFormats = new ArrayList<String>();
		rawGlobalOpcodesString = "";
		rawLinesString = "";
		rawLines = new ArrayList<String>();
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public HashMap<String, String> getGlobalOpCodes() {
		return globalOpCodes;
	}

	public void setGlobalOpCodes(HashMap<String, String> globalOpCodes) {
		this.globalOpCodes = globalOpCodes;
	}

	public HashMap<String, OperandFormat> getMnemFormatHash() {
		return mnemFormatHash;
	}

	public void setMnemFormatHash(HashMap<String, OperandFormat> mnemFormatHash) {
		this.mnemFormatHash = mnemFormatHash;
	}

	public ArrayList<String> getMnemFormats() {
		return mnemFormats;
	}

	public void setMnemFormats(ArrayList<String> mnemFormats) {
		this.mnemFormats = mnemFormats;
	}

	public String getRawGlobalOpcodesString() {
		return rawGlobalOpcodesString;
	}

	public void setRawGlobalOpcodesString(String rawGlobalOpcodesString) {
		this.rawGlobalOpcodesString = rawGlobalOpcodesString;
	}

	public ArrayList<String> getRawLines() {
		return rawLines;
	}

	public void setRawLines(ArrayList<String> rawLines) {
		this.rawLines = rawLines;
	}
	
	public void addToRawLines(String str){
		
		this.rawLines.add(str);
		this.rawLinesString += str + "\n";
	}

	public String getRawLinesString() {
		return rawLinesString;
	}

	public void setRawLinesString(String rawLinesString) {
		this.rawLinesString = rawLinesString;
	}
}
