package net.praqma.cli;

import edu.umd.cs.findbugs.annotations.*;
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
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

import java.util.logging.Logger;

@SuppressFBWarnings("")
public class GetTagValue {
	private static Logger logger = Logger.getLogger( GetTagValue.class.getName() );
	
	public static void main( String[] args ) throws ClearCaseException {
		try {
			run( args );
		} catch( ClearCaseException e ) {
			e.print( System.err );
			throw e;
		}
	}

	public static void run( String[] args ) throws UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException, UnknownEntityException, TagException, UnableToGetEntityException, UnableToInitializeEntityException {
		Options o = new Options();

		Option oentity = new Option( "entity", "e", true, 1, "The UCM entity" );
		Option okey = new Option( "key", "k", true, 1, "The tag key" );
		Option otagtype = new Option( "tagtype", "y", true, 1, "The tag type" );
		Option otagid = new Option( "tagid", "i", true, 1, "The tag id" );

		o.setOption( oentity );
		o.setOption( okey );
		o.setOption( otagtype );
		o.setOption( otagid );

		o.setDefaultOptions();

		o.setHeader( "Get the value of a tag" );
		o.setSyntax( "GetTagValue -e <entity> -k <key> -y <tag type> -i <tag id>" );
		o.setDescription( "Examples:" + Options.linesep + "GetTagValue -e baseline:bls@\\somevob -k status -y myjob -i 10101" );

		o.parse( args );

		try {
			o.checkOptions();
		} catch (Exception e) {
			logger.severe( "Incorrect option: " + e.getMessage() );
			o.display();
			System.exit( 1 );
		}

		UCMEntity e = null;

		e = UCMEntity.getEntity( oentity.getString() ).load();

		Tag tag = e.getTag( otagtype.getString(), otagid.getString() );

		String value = tag.getEntry( okey.getString() );

		if( value == null ) {
			logger.info( "Unknown key, " + okey.getString() );
		} else {
			logger.info( value );
		}
	}

}
