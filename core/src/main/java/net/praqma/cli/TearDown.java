package net.praqma.cli;

import java.io.IOException;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.util.SetupUtils;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class TearDown extends CLI {
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static Logger logger = Logger.getLogger();
	
	public static void main( String[] args ) throws ClearCaseException, IOException {
		tracer.finest("Starting execution of method - main");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Creating new TearDown and calling perform with the given args...");
		
		TearDown s = new TearDown();
		s.perform( args );
		
		tracer.finest("Ending execution of method - main");
	}

	public void perform( String[] args ) throws ClearCaseException, IOException {
		tracer.finest("Starting execution of method - perform");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Creating options.");

		Options o = new Options( "1.0.0" );

		Option otag = new Option( "tag", "t", true, 1, "UCM Project VOB tag" );

		o.setOption( otag );

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
		tracer.finest("Getting PVob from tag option");
		
		PVob pvob = PVob.get( otag.getString() );
		
		tracer.finest("Tearing down selected PVob");
		
		SetupUtils.tearDown( pvob );
		
		tracer.finest("Ending execution of method - perform");
	}
}
