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
	private HashMap<String,MnemType> mnemTypeHash;
	private ArrayList<String> mnemTypes;
	
	public MnemonicData(){
		
		mnemonic = "";
		globalOpCodes = new HashMap<String, String>();
		mnemTypeHash = new HashMap<String, MnemType>();
		mnemTypes = new ArrayList<String>();
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

	public HashMap<String, MnemType> getMnemTypeHash() {
		return mnemTypeHash;
	}

	public void setMnemTypeHash(HashMap<String, MnemType> mnemTypeHash) {
		this.mnemTypeHash = mnemTypeHash;
	}

	public ArrayList<String> getMnemTypes() {
		return mnemTypes;
	}

	public void setMnemTypes(ArrayList<String> mnemTypes) {
		this.mnemTypes = mnemTypes;
	}
}
