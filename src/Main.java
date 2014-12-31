/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

public class Main {

	/**
	 * Usage: args[0] is assembly file name
	 *        args[1] is architecture specification file name
	 */
	public static void main(String[] args) {
	
		if(args.length == 0){
			
			System.out.println("Assembly file and specification file not given.");
			System.exit(0);
		}
		
		else if(args.length == 1){
			
			System.out.println("Specification file not given.");
			System.out.println("Assembly file: " + args[0]);
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
		
//		String x = "0x8d480060";
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
		
		
//		System.out.println("ADT " + data.getAdt().getAdtHash());
		
		Assembler assembler = new Assembler(data);
		
	}
}
