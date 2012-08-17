package net.praqma.cli;

import java.io.File;
import java.io.IOException;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.setup.EnvironmentParser;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class Setup extends CLI {
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static Logger logger = Logger.getLogger();
	
	public static void main( String[] args ) throws ClearCaseException, IOException, Exception {
		tracer.finest("Starting execution of method - main");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Creating new Setup and calling perform with the given args...");
		
		Setup s = new Setup();
        s.perform( args );
        
        tracer.finest("Ending execution of method - main");
	}

	public void perform( String[] args ) throws ClearCaseException, IOException, Exception {
		tracer.finest("Starting execution of method - perform");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Creating options.");

		Options o = new Options( "1.0.0" );

		Option ofile = new Option( "file", "f", true, 1, "XML file describing setup" );

		o.setOption( ofile );
		
		tracer.finest("Setting default options.");

		o.setDefaultOptions();
		
		tracer.finest("Parsing args to Options.");

		o.parse( args );
		
		tracer.finest("Attepmting to load Options...");

		try {
			o.checkOptions();
		} catch( Exception e ) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			logger.error( "Incorrect option: " + e.getMessage() );
			o.display();
			
			tracer.severe("Cannot recover from exception, exiting...");
			
			System.exit( 1 );
		}
		tracer.finest("Successfully loaded Options.");
		tracer.finest("Loading file from option");

		File file = new File( ofile.getString() );
		logger.verbose( "Parsing " + file.getAbsolutePath() );
		
		tracer.finest(String.format("Parsing file: %s", file.getAbsolutePath());
		
		EnvironmentParser parser = new EnvironmentParser( file );
		parser.parse();
		
		tracer.finest("Ending execution of method - main");
	}
}
