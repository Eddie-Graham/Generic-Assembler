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
	private String mnemonicFormat;
	private HashMap<String,String> opCodes;
	private ArrayList<ArrayList<DataSource.OperandType>> opFormats;
	private String instructionName;
	
	public MnemonicFormatData(){
		
		mnemonic = "";
		mnemonicFormat = "";
		opCodes = new HashMap<String, String>();
		opFormats = new ArrayList<ArrayList<DataSource.OperandType>>();
		instructionName = "";
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public String getMnemonicFormat() {
		return mnemonicFormat;
	}

	public void setMnemonicFormat(String mnemonicFormat) {
		this.mnemonicFormat = mnemonicFormat;
	}

	public HashMap<String, String> getOpCodes() {
		return opCodes;
	}

	public void setOpCodes(HashMap<String, String> opCodes) {
		this.opCodes = opCodes;
	}

	public ArrayList<ArrayList<DataSource.OperandType>> getOpFormats() {
		return opFormats;
	}

	public void setOpFormats(ArrayList<ArrayList<DataSource.OperandType>> opFormats) {
		this.opFormats = opFormats;
	}

	public String getInstructionName() {
		return instructionName;
	}

	public void setInstructionName(String instructionName) {
		this.instructionName = instructionName;
	}	
}
