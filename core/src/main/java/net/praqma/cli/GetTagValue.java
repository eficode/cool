package net.praqma.cli;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.TagException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.UnknownEntityException;
import net.praqma.clearcase.ucm.entities.Tag;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.util.debug.Logger;
import net.praqma.util.debug.appenders.Appender;
import net.praqma.util.debug.appenders.ConsoleAppender;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class GetTagValue {
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static Logger logger = Logger.getLogger();
	private static Appender app = new ConsoleAppender();
	
	public static void main( String[] args ) throws ClearCaseException {
		tracer.finest("Starting execution of method - main");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Attempting to execute run method with the given args...");
		
		try {
			run( args );
		} catch( ClearCaseException e ) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			e.print( System.err );
			throw e;
		}
		tracer.finest("Successfully completed run method");
		tracer.finest("Ending execution of method - main");
	}

	public static void run( String[] args ) throws UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException, UnknownEntityException, TagException, UnableToGetEntityException, UnableToInitializeEntityException {
		tracer.finest("Starting execution of method - run");
		tracer.finest(String.format("Input parameter args type: %s; value:", args.getClass()));
		for (String s : args) {
			tracer.finest(String.format("    %s", s));
		}
		tracer.finest("Creating options.");
		
		Options o = new Options();

		Option oentity = new Option( "entity", "e", true, 1, "The UCM entity" );
		Option okey = new Option( "key", "k", true, 1, "The tag key" );
		Option otagtype = new Option( "tagtype", "y", true, 1, "The tag type" );
		Option otagid = new Option( "tagid", "i", true, 1, "The tag id" );

		o.setOption( oentity );
		o.setOption( okey );
		o.setOption( otagtype );
		o.setOption( otagid );
		
        app.setTemplate( "[%level]%space %message%newline" );
        Logger.addAppender( app );
        
        tracer.finest("Setting default options.");
        
		o.setDefaultOptions();

		o.setHeader( "Get the value of a tag" );
		o.setSyntax( "GetTagValue -e <entity> -k <key> -y <tag type> -i <tag id>" );
		o.setDescription( "Examples:" + Options.linesep + "GetTagValue -e baseline:bls@\\somevob -k status -y myjob -i 10101" );

		tracer.finest("Parsing args to Options.");
		
		o.parse( args );

		tracer.finest("Attepmting to load Options...");
		
		try {
			o.checkOptions();
		} catch (Exception e) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			logger.fatal( "Incorrect option: " + e.getMessage() );
			o.display();
			
			tracer.severe("Cannot recover from exception, exiting...");
			
			System.exit( 1 );
		}
		tracer.finest("Successfully loaded Options.");
		tracer.finest("Getting UCMEntity from entity option.");

		UCMEntity e = null;

		e = UCMEntity.getEntity( oentity.getString() ).load();
		
		tracer.finest(String.format("UCMEntity e set to: %s", e));
		tracer.finest("Getting Tag from tag type and tag id options.");

		Tag tag = e.getTag( otagtype.getString(), otagid.getString() );
		
		tracer.finest(String.format("tag set to: %s", tag));
		tracer.finest("Getting tag entry from key option.");

		String value = tag.getEntry( okey.getString() );
		
		tracer.finest(String.format("value set to: %s", value));
		tracer.finest("Checking if value is null.");
		
		if( value == null ) {
			tracer.finest("value is null.");
			
			logger.info( "Unknown key, " + okey.getString() );
		} else {
			tracer.finest("value is not null");
			
			logger.info( value );
		}
		tracer.finest("Ending execution of method - run");
	}

}
