/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class MnemFormat {
	
	private String mnemFormat;
	private String insFieldLabels;
	private HashMap<String,String> opCodes;
	private ArrayList<String> instructionFormat;
	
	public MnemFormat(){
		
		mnemFormat = "";
		insFieldLabels = "";
		opCodes = new HashMap<String, String>();
		instructionFormat = new ArrayList<String>();		
	}

	public String getMnemFormat() {
		return mnemFormat;
	}

	public void setMnemFormat(String mnemFormat) {
		this.mnemFormat = mnemFormat;
	}

	public String getInsFieldLabels() {
		return insFieldLabels;
	}

	public void setInsFieldLabels(String insFieldLabels) {
		this.insFieldLabels = insFieldLabels;
	}

	public HashMap<String, String> getOpCodes() {
		return opCodes;
	}

	public void setOpCodes(HashMap<String, String> opCodes) {
		this.opCodes = opCodes;
	}

	public ArrayList<String> getInstructionFormat() {
		return instructionFormat;
	}

	public void setInstructionFormat(ArrayList<String> instructionFormat) {
		this.instructionFormat = instructionFormat;
	}
}
