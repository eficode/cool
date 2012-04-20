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
	
	private static Logger logger = Logger.getLogger();
	private static Appender app = new ConsoleAppender();
	
	public static void main( String[] args ) throws ClearCaseException {
		try {
			run( args );
		} catch( ClearCaseException e ) {
			e.print( System.err );
			throw e;
		}
	}

	public static void run( String[] args ) throws UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException, UnknownEntityException, TagException, UnableToGetEntityException, UnableToInitializeEntityException {
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

		o.setDefaultOptions();

		o.setHeader( "Set a tag for an UCM entity" );
		o.setSyntax( "SetTag -e <entity> -t <tag> -y <tag type> -i <tag id>" );
		o.setDescription( "Examples:" + Options.linesep + "SetTag -e baseline:bls@\\somevob -T \"key1=val1&key2=val2\" -y myjob -i 10101" + Options.linesep + "SetTag -e baseline:bls@\\somevob -T \"key1=&key2=val2\" -y myjob -i 10101" + Options.linesep + "The last example will remove key1 from the tag" );

		o.parse( args );

		try {
			o.checkOptions();
		} catch( Exception e ) {
			logger.fatal( "Incorrect option: " + e.getMessage() );
			o.display();
			System.exit( 1 );
		}

		UCMEntity e = null;

		e = UCMEntity.getEntity( oentity.getString() ).load();


		Tag tag = e.getTag( otagtype.getString(), otagid.getString() );

		/* Split key value structure */
		String[] tags = otag.getString().split( "&" );

		for( String t : tags ) {

			String[] entry = t.split( "=" );

			try {
				logger.verbose( "+(" + entry[0] + ", " + entry[1] + ") " );

				tag.setEntry( entry[0].trim(), entry[1].trim() );
			} catch( ArrayIndexOutOfBoundsException ea ) {
				logger.info( "-(" + entry[0] + ") " );
				tag.removeEntry( entry[0] );
			}
		}

		try {
			tag.persist();
		} catch( TagException ex ) {
			if( ex.getType().equals( Type.CREATION_FAILED ) ) {
				logger.fatal( "Could not persist the tag." );
				System.exit( 1 );
			}
		}

		if( tag.isCreated() ) {
			logger.info( "Tag created." );
		} else {
			logger.info( "Tag updated." );
		}
	}

}
