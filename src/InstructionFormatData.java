/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;


public class InstructionFormatData {
	
	private String instructionName;
	private HashMap<String, Integer> operandBitHash;
	private ArrayList<String> operands;
	private String rawLineString;
	
	public InstructionFormatData(){
		
		instructionName = "";
		operandBitHash = new HashMap<String, Integer>();
		operands = new ArrayList<String>();	
		rawLineString = "";
	}

	public String getInstructionName() {
		return instructionName;
	}

	public void setInstructionName(String instructionName) {
		this.instructionName = instructionName;
	}

	public HashMap<String, Integer> getOperandBitHash() {
		return operandBitHash;
	}

	public void setOperandBitHash(HashMap<String, Integer> operandBitHash) {
		this.operandBitHash = operandBitHash;
	}

	public ArrayList<String> getOperands() {
		return operands;
	}

	public void setOperands(ArrayList<String> operands) {
		this.operands = operands;
	}

	public String getRawLineString() {
		return rawLineString;
	}

	public void setRawLineString(String rawLineString) {
		this.rawLineString = rawLineString;
	}		
}
