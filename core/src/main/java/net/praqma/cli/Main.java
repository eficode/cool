package net.praqma.cli;

public class Main {
	public static void main( String[] args ) throws Exception {
		/* Fist argument is sub program */
		String app = args[0];
		
		Class<CLI> clazz = (Class<CLI>) Class.forName( "net.praqma.cli." + app );
		CLI cli = clazz.newInstance();
		cli.perform( args );
	}
}
