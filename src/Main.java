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
		
//		String x = "0x012A4020";
//		String s = Assembler.hexToBinary(x);
//		System.out.println(s);
		
//		String x = "00000001001010100100000001000000";
//		String s = Assembler.binaryToHex(x);
//		System.out.println(s);
				
//		System.out.println("assembly: " + data.getAssemblyCode());	
//		System.out.println("arc " + data.getArchitecture());		
//		System.out.println("insF " + data.getInstructionFormat());	
//		System.out.println("regHash " + data.getRegisterHash());
		
//		String s = "add";
//		System.out.println("opMn " + data.getOpcodeFormats().get(s).getMnemonic());
//		System.out.println("opCond " + data.getOpcodeFormats().get(s).getOpConditions());
//		System.out.println("opLabel " + data.getOpcodeFormats().get(s).getLabel());
//		System.out.println("opFor " + data.getOpcodeFormats().get(s).getOpFormat());
		
		Assembler assembler = new Assembler(data);
		
	}
}
