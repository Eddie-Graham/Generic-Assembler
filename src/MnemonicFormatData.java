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
	private HashMap<String,String> opcodes;
	private String instructionName;
	
	public MnemonicFormatData(){
		
		mnemonic = "";
		mnemonicFormat = "";
		opcodes = new HashMap<String, String>();
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

	public HashMap<String, String> getOpcodes() {
		return opcodes;
	}

	public void setOpcodes(HashMap<String, String> opcodes) {
		this.opcodes = opcodes;
	}

	public String getInstructionName() {
		return instructionName;
	}

	public void setInstructionName(String instructionName) {
		this.instructionName = instructionName;
	}	
}
