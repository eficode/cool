package net.praqma.cli;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.logging.Config;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

/*
 * Usage
 * java -classpath COOL-0.1.5.jar net.praqma.cli.BuildNumber -f stamptest.txt -m 12 -p 1234 -s 22221 --minor 22b
 * 
 * 
 */

public class BuildNumberStamper {
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	public static void main( String[] args ) throws ClearCaseException {
		tracer.finest("Starting execution of method - main");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Calling run method with args");
		
		run( args );
		
		tracer.finest("Ending execution of method - main");
	}

	public static void run( String[] args ) {
		tracer.finest("Starting execution of method - run");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Creating options.");
		
		Options o = new Options( net.praqma.cool.Version.version );

		Option omajor = new Option( "major", "m", false, 1, "The major version of the change set to stamp" );
		Option ominor = new Option( "minor", "i", false, 1, "The minor version of the change set to stamp" );
		Option opatch = new Option( "patch", "p", false, 1, "The patch version of the change set to stamp" );
		Option osequence = new Option( "sequence", "s", false, 1, "The sequence version of the change set to stamp" );
		Option ofile = new Option( "file", "f", true, 1, "The file to stamp" );

		o.setOption( omajor );
		o.setOption( ominor );
		o.setOption( opatch );
		o.setOption( osequence );
		o.setOption( ofile );
		
		tracer.finest("Setting default options.");

		o.setDefaultOptions();

		o.setSyntax( "BuildNumber <options> -f file" );
		o.setHeader( "Automatically stamp a build number into a source/header file." );
		o.setDescription( "Examples:" + Options.linesep + "private static final String major = \"0\"; // buildnumber.major" );

		tracer.finest("Parsing args to Options.");
		
		o.parse( args );
		
		tracer.finest("Attepmting to load Options...");

		try {
			o.checkOptions();
		} catch( Exception e ) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			System.err.println( "Incorrect option: " + e.getMessage() );
			o.display();
			
			tracer.severe("Cannot recover from exception, exiting...");
			
			System.exit( 1 );
		}
		tracer.finest("Successfully loaded Options.");
		tracer.finest("Creating file from file option.");

		File file = new File( ofile.getString() );
		
		tracer.finest(String.format("file set to: %s", file.getAbsolutePath()));

		net.praqma.util.io.BuildNumberStamper stamp = null;
		
		tracer.finest("Attempting to create BuildNumberStamper...");
		try {
			stamp = new net.praqma.util.io.BuildNumberStamper( file );
		} catch( IOException e ) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			System.err.println( "Could not create temporary file" );
			
			tracer.severe("Cannot recover from exception, exiting...");
			
			System.exit( 1 );
		}
		tracer.finest("Successfully create BuildNumberStamper.");
		tracer.finest("Checking if options are verbose.");
		
		if( o.isVerbose() ) {
			tracer.finest("Options are verbose.");
			o.print();
		}

		tracer.finest("Attempting to stamp build number into code.");
		
		try {
			System.out.println( "Stamping file " + file );
			int number = stamp.stampIntoCode( omajor.getString(), ominor.getString(), opatch.getString(), osequence.getString() );
			
			tracer.finest("Checking if number of stamps is greater than 0.");
			
			if( number > 0 ) {
				tracer.finest(String.format("Number of occurences is greater than 0, value: %s", number));
				
				System.out.println( number + " occurrence" + ( number > 1 ? "s" : "" ) + " found" );
			} else {
				System.err.println( "No occurrences found" );
				
				tracer.finest("No occurrences, exiting with error code.");
				
				System.exit( 1 );
			}
		} catch( IOException e ) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			System.err.println( "Could not edit file: " + e.getMessage() );
			
			tracer.severe("Cannot recover from exception, exiting...");
			
			System.exit( 1 );
		}
		tracer.finest("Successfully stamped build number into code.");
		tracer.finest("Ending execution of method - run");
	}
}
