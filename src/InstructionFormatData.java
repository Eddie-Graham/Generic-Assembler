/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;


public class InstructionFormatData {
	
	String instructionName;
	HashMap<String, Integer> opFormatBitHash;
	ArrayList<String> operands;
	
	public InstructionFormatData(){
		
		instructionName = "";
		opFormatBitHash = new HashMap<String, Integer>();
		operands = new ArrayList<String>();		
	}

	public String getInstructionName() {
		return instructionName;
	}

	public void setInstructionName(String instructionName) {
		this.instructionName = instructionName;
	}

	public HashMap<String, Integer> getOpFormatBitHash() {
		return opFormatBitHash;
	}

	public void setOpFormatBitHash(HashMap<String, Integer> opFormatBitHash) {
		this.opFormatBitHash = opFormatBitHash;
	}

	public ArrayList<String> getOperands() {
		return operands;
	}

	public void setOperands(ArrayList<String> operands) {
		this.operands = operands;
	}
}
