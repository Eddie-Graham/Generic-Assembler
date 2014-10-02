
public class Main {

	/**
	 * Usage: args[0] is assembly file name
	 *        args[1] is architecture specification file name
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		FileParser file = new FileParser(args[0], args[1]);
		
		DataSource data = file.getData();
		
		System.out.println(data.getAssemblyCode().toString());
		System.out.println(data.getArchitecture().toString());
		

	}

}
