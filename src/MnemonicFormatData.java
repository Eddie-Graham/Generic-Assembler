/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class MnemonicFormatData {
	
	private String mnemonic;
	private HashMap<String,String> opCodes;
	private ArrayList<String> opFormats;
	private String instructionName;
	
	public MnemonicFormatData(){
		
		mnemonic = "";
		opCodes = new HashMap<String, String>();
		opFormats = new ArrayList<String>();
		instructionName = "";
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public HashMap<String, String> getOpCodes() {
		return opCodes;
	}

	public void setOpCodes(HashMap<String, String> opCodes) {
		this.opCodes = opCodes;
	}

	public ArrayList<String> getOpFormats() {
		return opFormats;
	}

	public void setOpFormats(ArrayList<String> opFormats) {
		this.opFormats = opFormats;
	}

	public String getInstructionName() {
		return instructionName;
	}

	public void setInstructionName(String instructionName) {
		this.instructionName = instructionName;
	}
}
