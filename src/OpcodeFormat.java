/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class OpcodeFormat {
	
	String mnemonic;
	ArrayList<String> opFormat;
	HashMap<String,String> opConditions;
	String instructionName;
	
	public OpcodeFormat(){
		
		mnemonic = "";
		opFormat = new ArrayList<String>();
		opConditions = new HashMap<String, String>();
		instructionName = "";
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public ArrayList<String> getOpFormat() {
		return opFormat;
	}

	public void setOpFormat(ArrayList<String> opFormat) {
		this.opFormat = opFormat;
	}

	public HashMap<String, String> getOpConditions() {
		return opConditions;
	}

	public void setOpConditions(HashMap<String, String> opConditions) {
		this.opConditions = opConditions;
	}

	public String getInstructionName() {
		return instructionName;
	}

	public void setInstructionName(String instructionName) {
		this.instructionName = instructionName;
	}	
}
