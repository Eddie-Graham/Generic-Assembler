
public class Main {

	/**
	 * Usage: args[0] is assembly file name
	 *        args[1] is architecture specification file name
	 */
	public static void main(String[] args) {
	
		if(args.length == 0){
			System.out.println("Assembly file and specification file not specified.");
			return;
		}
		else if(args.length == 1){
			System.out.println("No specification file specified.");
			System.out.println("Assembly file: " + args[0]);
			return;
		}
		
		FileParser file = new FileParser(args[0], args[1]);
		
		DataSource data = file.getData();
		
		System.out.println(data.getAssemblyCode().toString());
		System.out.println(data.getArchitecture().toString());
		

	}

}
