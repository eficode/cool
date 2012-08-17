package net.praqma.cli;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.TagException;
import net.praqma.clearcase.exceptions.TagException.Type;
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

public class SetTag {
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
		
		Logger.addAppender( app );
		Options o = new Options();

		Option oentity = new Option( "entity", "e", true, 1, "The UCM entity" );
		Option otag = new Option( "tag", "t", true, 1, "The tag. Given as: \"key1=val1&key2=val2\"" );
		Option otagtype = new Option( "tagtype", "y", true, 1, "The tag type" );
		Option otagid = new Option( "tagid", "i", true, 1, "The tag id" );

		o.setOption( oentity );
		o.setOption( otag );
		o.setOption( otagtype );
		o.setOption( otagid );
		
		tracer.finest("Setting default options.");

		o.setDefaultOptions();

		o.setHeader( "Set a tag for an UCM entity" );
		o.setSyntax( "SetTag -e <entity> -t <tag> -y <tag type> -i <tag id>" );
		o.setDescription( "Examples:" + Options.linesep + "SetTag -e baseline:bls@\\somevob -T \"key1=val1&key2=val2\" -y myjob -i 10101" + Options.linesep + "SetTag -e baseline:bls@\\somevob -T \"key1=&key2=val2\" -y myjob -i 10101" + Options.linesep + "The last example will remove key1 from the tag" );

		tracer.finest("Parsing args to Options.");
		
		o.parse( args );
		
		tracer.finest("Attepmting to load Options...");

		try {
			o.checkOptions();
		} catch( Exception e ) {
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
		tracer.finest("Getting tags from tag option.");

		/* Split key value structure */
		String[] tags = otag.getString().split( "&" );
		
		tracer.finest("tags set to:");
		for (String output : tags) {
			tracer.finest(String.format("    %s", output));
		}
		tracer.finest("Parsing tags.");
		
		for( String t : tags ) {
			
			tracer.finest(String.format("Splitting tag: %s", t));
			
			String[] entry = t.split( "=" );

			tracer.finest("Attempting to set entry to tag...");
			
			try {
				logger.verbose( "+(" + entry[0] + ", " + entry[1] + ") " );

				tag.setEntry( entry[0].trim(), entry[1].trim() );
			} catch( ArrayIndexOutOfBoundsException ea ) {
				tracer.severe(String.format("Exception thrown type: %s; message: %s", ea.getClass(), ea.getMessage()));
				
				logger.info( "-(" + entry[0] + ") " );
				
				tracer.severe(String.format("Removing entry %s %s due to exception being thrown.", entry[0].trim(), entry[1].trim()));
				
				tag.removeEntry( entry[0] );
			}
		}
		tracer.finest(String.format("tag updated to: %s", tag));
		tracer.finest("Attempting to persist tag...");
		
		try {
			tag.persist();
		} catch( TagException ex ) {
			if( ex.getType().equals( Type.CREATION_FAILED ) ) {
				tracer.severe(String.format("Exception thrown type: %s; message: %s", ex.getClass(), ex.getMessage()));
				
				logger.fatal( "Could not persist the tag." );
				
				tracer.severe("Cannot recover from exception, exiting...");
				
				System.exit( 1 );
			}
		}

		tracer.finest("Checking if tag has been created.");
		if( tag.isCreated() ) {
			tracer.finest("tag has been created.");
			
			logger.info( "Tag created." );
		} else {
			tracer.finest("tag has not been created.");
			
			logger.info( "Tag updated." );
		}
		tracer.finest("Ending execution of method - run");
	}

}
