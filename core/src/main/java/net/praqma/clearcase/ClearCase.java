package net.praqma.clearcase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UnableToCreateAttributeException;
import net.praqma.clearcase.exceptions.UnableToListAttributesException;
import net.praqma.clearcase.exceptions.UnableToSetAttributeException;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

public abstract class ClearCase extends Cool {
	
	private static Logger logger = Logger.getLogger( ClearCase.class.getName() );

	public static final String rx_attr_find = "^\\s*\\S+\\s*=\\s*\\S*\\s*$";
	
	/**
	 * Retrieve the attributes for an entity, executed from the current working
	 * directory
	 * 
	 * @return A Map of key, value pairs of the attributes
	 * @throws UnableToListAttributesException
	 */
	public static Map<String, String> getAttributes( ClearCase entity, File context ) throws UnableToListAttributesException {
		String cmd = "describe -aattr -all " + entity;

		CmdResult res = null;
		try {
			res = Cleartool.run( cmd, context );
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Could not find attributes on " + fqname + ". Recieved: " + e.getMessage(), e.getMessage() );
			throw new UnableToListAttributesException( entity, context, e );
		}

		Map<String, String> atts = new HashMap<String, String>();

		for( String s : res.stdoutList ) {
			/* A valid attribute */
			if( s.matches( rx_attr_find ) ) {
				String[] data = s.split( "=" );
				atts.put( data[0].trim(), data[1].trim() );
			}
		}

		return atts;
	}

	public Map<String, String> getAttributes() throws UnableToListAttributesException {
		return getAttributes( this, null );
	}

	public Map<String, String> getAttributes( File context ) throws UnableToListAttributesException {
		return getAttributes( this, context );
	}

	public String getAttribute( String key ) throws UnableToListAttributesException {
		Map<String, String> atts = getAttributes( this, null );
		if( atts.containsKey( key ) ) {
			return atts.get( key );
		} else {
			return null;
		}
	}

	public void setAttribute( String attribute, String value, boolean replace ) throws UnableToSetAttributeException {
		setAttribute( attribute, value, replace, null );
	}

	public void setAttribute( String attribute, String value, boolean replace, File context ) throws UnableToSetAttributeException {

		if( Cool.getOS().equals( OperatingSystem.WINDOWS )) {
			value = "\\\"" + value + "\\\"";
		} else {
			value = "'\"" + value + "\"'";
		}

		logger.fine( "Setting attribute " + attribute + "=" + value + " for " + this.getFullyQualifiedName() );

		String cmd = "mkattr " + ( replace ? "-replace " : "" ) + attribute + " " + value + " " + this.getFullyQualifiedName();
		try {
			Cleartool.run( cmd, context );
		} catch( AbnormalProcessTerminationException e ) {

			if( !replace ) {
				cmd = "mkattr " + attribute + " " + value + " " + this.getFullyQualifiedName();
				try {
					Cleartool.run( cmd, context );
				} catch( AbnormalProcessTerminationException e2 ) {
					throw new UnableToSetAttributeException( this, attribute, value, context, e2 );
				}
			}
			throw new UnableToSetAttributeException( this, attribute, value, context, e );
		}
	}
	
	/**
	 * Create an attribute with no possibility of specifying type or range.
	 * @param name Name of the type
	 * @param pvob The PVob
	 * @param replace
	 */
	public static void createSimpleAttributeType( String name, PVob pvob, boolean replace ) throws UnableToCreateAttributeException {
		String cmd = "mkattype -vtype string -nc " + name + "@" + pvob;
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			if( replace ) {
				cmd = "mkattype -replace -vtype string -nc " + name + "@" + pvob;
				try {
					Cleartool.run( cmd );
				} catch( Exception e2 ) {
					throw new UnableToCreateAttributeException( "Unable to create attribute type", e );
				}
				
			} else {
				throw new UnableToCreateAttributeException( "Unable to create attribute type", e );
			}
		}
	}
	
	public abstract String getFullyQualifiedName();

}
