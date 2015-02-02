/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class MnemonicData {
	
	private String mnemonic;
	private HashMap<String,String> globalOpCodes;
	private HashMap<String,MnemFormat> mnemFormatHash;
	private ArrayList<String> mnemFormats;
	private String rawGlobalOpcodesString;
	
	public MnemonicData(){
		
		mnemonic = "";
		globalOpCodes = new HashMap<String, String>();
		mnemFormatHash = new HashMap<String, MnemFormat>();
		mnemFormats = new ArrayList<String>();
		rawGlobalOpcodesString = "";
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

	public HashMap<String, MnemFormat> getMnemFormatHash() {
		return mnemFormatHash;
	}

	public void setMnemFormatHash(HashMap<String, MnemFormat> mnemFormatHash) {
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
}
