/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class MnemType {
	
	private String mnemType;
	private String insLabels;
	private HashMap<String,String> opCodes;
	private ArrayList<String> instructionFormat;
	
	public MnemType(){
		
		mnemType = "";
		insLabels = "";
		opCodes = new HashMap<String, String>();
		instructionFormat = new ArrayList<String>();		
	}

	public String getMnemType() {
		return mnemType;
	}

	public void setMnemType(String mnemType) {
		this.mnemType = mnemType;
	}

	public String getInsLabels() {
		return insLabels;
	}

	public void setInsLabels(String insLabels) {
		this.insLabels = insLabels;
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
