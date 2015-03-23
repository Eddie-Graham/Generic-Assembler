/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class OperandFormat {
	
	private String mnemFormat;
	private String operandFieldEncodings;
	private HashMap<String,String> fieldBitHash;
	private ArrayList<String> instructionFormat;
	private String rawLinesString;
	
	public OperandFormat(){
		
		mnemFormat = "";
		operandFieldEncodings = "";
		fieldBitHash = new HashMap<String, String>();
		instructionFormat = new ArrayList<String>();	
		rawLinesString = "";
	}

	public void addToRawLineString(String str){		
		rawLinesString += str + "\n";
	}

	public String getMnemFormat() {
		return mnemFormat;
	}

	public void setMnemFormat(String mnemFormat) {
		this.mnemFormat = mnemFormat;
	}

	public String getOperandFieldEncodings() {
		return operandFieldEncodings;
	}

	public void setOperandFieldEncodings(String operandFieldEncodings) {
		this.operandFieldEncodings = operandFieldEncodings;
	}

	public HashMap<String, String> getFieldBitHash() {
		return fieldBitHash;
	}

	public void setFieldBitHash(HashMap<String, String> fieldBitHash) {
		this.fieldBitHash = fieldBitHash;
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
}
