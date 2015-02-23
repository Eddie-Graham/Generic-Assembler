/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

public class Main {

	/**
	 * Usage: args[0] is specification file name
	 *        args[1] is assembly file name
	 */
	public static void main(String[] args) {
	
		if(args.length == 0){
			
			System.out.println("Specification and assembly filenames not given.");
			System.exit(0);
		}
		
		else if(args.length == 1){
			
			System.out.println("Assembly file not given.");
			System.out.println("Specification file: " + args[0]);
			System.exit(0);
		}
				
		FileParser file = new FileParser(args[0], args[1]);
		
		DataSource data = file.getData();	
		
//		System.out.println(data.getPrefixHash());		
//		System.out.println(data.getPrefixes());
		
//		String x = "0x012A4020";
//		String x = "8";
//		String s = Assembler.binaryFormatted(x, 7);
//		System.out.println(s);
		
//		String x = "12";
//		String s = Assembler.decimalToBinary(x);
//		System.out.println(s);
		
//		String x = "00000001000011010110000000100000";
//		String s = Assembler.binaryToHex(x);
//		System.out.println(s);
		
//		String x = "0x2";
//		String s = Assembler.hexToBinary(x);
//		System.out.println(s);
				
//		System.out.println("assembly: " + data.getAssemblyCode());	
//		System.out.println("arc " + data.getArchitecture());		
//		System.out.println("insF " + data.getInstructionFormat());	
//		System.out.println("regHash " + data.getRegisterHash());
		
//		String s = "ADD";
//		System.out.println("opMn " + data.getMnemonicTable().get(s).getMnemonic());
//		System.out.println("globalops " + data.getMnemonicTable().get(s).getGlobalOpCodes());
//		System.out.println("globalops " + data.getMnemonicTable().get(s).getMnemTypeHash());
//		System.out.println("mnemTypes " + data.getMnemonicTable().get(s).getMnemTypes());
		
		
//		System.out.println("AssemblyOpTree " + data.getAdt().getAdtHash());
		
//		System.out.println("minAdrUnit " + data.getMinAdrUnit());
		
//		System.out.println(data.getInstructionSizes());
		
		
		Assembler assembler = new Assembler(data);
		
	}
}
