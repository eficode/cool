package net.praqma.cli;

import java.util.List;

import net.praqma.logging.Config;
import net.praqma.util.PackageUtils;

public class Main {
	public static void main( String[] args ) throws Exception {
		final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
		tracer.finest("Starting execution of method - main");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		
		tracer.finest("Checking if any args have been given.");
		
		if( args.length > 0 ) {
			tracer.finest("args have been given.");
			tracer.finest("Parsing args.");
			
			/* Fist argument is sub program */
			String app = args[0];
			
			tracer.finest(String.format("Creating new CLI class of type: %s", app));
			
			Class<CLI> clazz = (Class<CLI>) Class.forName( "net.praqma.cli." + app );
			CLI cli = clazz.newInstance();
			
			tracer.finest(String.format("Calling perform method of class: %s", cli));
			
			cli.perform( args );
		} else {
			tracer.finest("No args were provided.");
			tracer.finest("Listing sub programs:");
			
			List<Class<?>> list = PackageUtils.getClasses( "net.praqma.cli", null );
			
			System.out.println( "Possible sub programs:" );
			for( Class<?> c : list ) {
				tracer.finest(String.format("    Checking if %s is a sub class of CLI.", c.getSimpleName()));
				
				if( CLI.class.isAssignableFrom( c ) && !c.equals( CLI.class ) ) {
					tracer.finest(String.format("    %s is a sub class of CLI.", c.getSimpleName()));
					
					Class<? extends CLI> c1 = (Class<? extends CLI>) c;
					System.out.println( " * " + c.getSimpleName() );
				}
			}
		}
		tracer.finest("Ending execution of method - main");
	}
}
