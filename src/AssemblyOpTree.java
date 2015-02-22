/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

import java.util.ArrayList;
import java.util.HashMap;

public class AssemblyOpTree {
	
	String rootToken;
	private HashMap<String, ArrayList<String>> assemblyOpTreeHash;
	private ArrayList<String> assemblyOpTreeTokens;
	
	public AssemblyOpTree(){
		
		rootToken= null;
		assemblyOpTreeHash = new HashMap<String, ArrayList<String>>();
		assemblyOpTreeTokens = new ArrayList<String>();
	}

	public String getRootToken() {
		return rootToken;
	}

	public void setRootToken(String rootToken) {
		this.rootToken = rootToken;
	}

	public HashMap<String, ArrayList<String>> getAssemblyOpTreeHash() {
		return assemblyOpTreeHash;
	}

	public void setAssemblyOpTreeHash(
			HashMap<String, ArrayList<String>> assemblyOpTreeHash) {
		this.assemblyOpTreeHash = assemblyOpTreeHash;
	}

	public ArrayList<String> getAssemblyOpTreeTokens() {
		return assemblyOpTreeTokens;
	}

	public void setAssemblyOpTreeTokens(ArrayList<String> assemblyOpTreeTokens) {
		this.assemblyOpTreeTokens = assemblyOpTreeTokens;
	}			
}
