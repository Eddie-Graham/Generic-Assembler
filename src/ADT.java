/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class ADT {
	
	String rootTerm;
	private HashMap<String, ArrayList<String>> adtHash;
	
	public ADT(){
		
		rootTerm= "";
		adtHash = new HashMap<String, ArrayList<String>>();
	}

	public String getRootTerm() {
		return rootTerm;
	}

	public void setRootTerm(String rootTerm) {
		this.rootTerm = rootTerm;
	}

	public HashMap<String, ArrayList<String>> getAdtHash() {
		return adtHash;
	}

	public void setAdtHash(HashMap<String, ArrayList<String>> adtHash) {
		this.adtHash = adtHash;
	}	
}
