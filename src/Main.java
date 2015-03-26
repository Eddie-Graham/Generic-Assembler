/** 
 * Eddie Graham
 * 1101301g
 * Individual Project 4
 * Supervisor: John T O'Donnell
 */

/*
 * The Generic-Assembler reads in two inputs: (1) a specification of the
 * computer architecture and assembly language, and (2) a source program written
 * in that assembly language. The software then outputs the corresponding
 * machine language result.
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
		
		else if(args.length > 2){
			
			System.out.println("Too many arguments provided.");
			System.exit(0);
		}
				
		FileParser file = new FileParser(args[0], args[1]);
		
		DataSource data = file.getData();		
		
		new Assembler(data);
		}
}
