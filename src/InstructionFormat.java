/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;


public class InstructionFormat {
	
	private String instructionName;
	private HashMap<String, Integer> fieldBitHash;
	private ArrayList<String> fields;
	private String rawLineString;
	
	public InstructionFormat(){
		
		instructionName = null;
		fieldBitHash = new HashMap<String, Integer>();
		fields = new ArrayList<String>();	
		rawLineString = null;
	}

	public String getInstructionName() {
		return instructionName;
	}

	public void setInstructionName(String instructionName) {
		this.instructionName = instructionName;
	}

	public HashMap<String, Integer> getFieldBitHash() {
		return fieldBitHash;
	}

	public void setFieldBitHash(HashMap<String, Integer> fieldBitHash) {
		this.fieldBitHash = fieldBitHash;
	}

	public ArrayList<String> getFields() {
		return fields;
	}

	public void setFields(ArrayList<String> fields) {
		this.fields = fields;
	}

	public String getRawLineString() {
		return rawLineString;
	}

	public void setRawLineString(String rawLineString) {
		this.rawLineString = rawLineString;
	}			
}
