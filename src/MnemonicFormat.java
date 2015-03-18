/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class MnemonicFormat {
	
	private String mnemFormat;
	private String insFieldLabels;
	private HashMap<String,String> opcodes;
	private ArrayList<String> instructionFormat;
	private String rawLinesString;
	
	public MnemonicFormat(){
		
		mnemFormat = "";
		insFieldLabels = "";
		opcodes = new HashMap<String, String>();
		instructionFormat = new ArrayList<String>();	
		rawLinesString = "";
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

	public HashMap<String, String> getOpcodes() {
		return opcodes;
	}

	public void setOpcodes(HashMap<String, String> opCodes) {
		this.opcodes = opCodes;
	}

	public ArrayList<String> getInstructionFormat() {
		return instructionFormat;
	}

	public void setInstructionFormat(ArrayList<String> instructionFormat) {
		this.instructionFormat = instructionFormat;
	}
	
	public String getRawLinesString() {
		return rawLinesString;
	}

	public void setRawLinesString(String rawLinesString) {
		this.rawLinesString = rawLinesString;
	}

	public void addToRawLineString(String str){
		
		rawLinesString += str + "\n";
	}
}
