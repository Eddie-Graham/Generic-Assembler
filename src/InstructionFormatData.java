import java.util.ArrayList;
import java.util.HashMap;


public class InstructionFormatData {
	
	String instructionName;
	HashMap<String, Integer> opFormatHash;
	ArrayList<String> operands;
	
	public InstructionFormatData(){
		
		instructionName = "";
		opFormatHash = new HashMap<String, Integer>();
		operands = new ArrayList<String>();		
	}

	public String getInstructionName() {
		return instructionName;
	}

	public void setInstructionName(String instructionName) {
		this.instructionName = instructionName;
	}

	public HashMap<String, Integer> getOpFormatHash() {
		return opFormatHash;
	}

	public void setOpFormatHash(HashMap<String, Integer> opFormatHash) {
		this.opFormatHash = opFormatHash;
	}

	public ArrayList<String> getOperands() {
		return operands;
	}

	public void setOperands(ArrayList<String> operands) {
		this.operands = operands;
	}
}
