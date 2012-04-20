package net.praqma.clearcase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UnableToListAttributesException;
import net.praqma.clearcase.exceptions.UnableToSetAttributeException;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

public abstract class ClearCase extends Cool {
	
	private static Logger logger = Logger.getLogger();

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

	public void setAttribute( String attribute, String value ) throws UnableToSetAttributeException {
		setAttribute( attribute, value, null );
	}

	public void setAttribute( String attribute, String value, File context ) throws UnableToSetAttributeException {
		//context.setAttribute( this, attribute, value );
		logger.debug( "Setting attribute " + attribute + "=" + value + " for " + this );

		String cmd = "mkattr -replace " + attribute + " " + value + " " + this;
		try {
			Cleartool.run( cmd, context );
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Could not create the attribute " + attribute, e.getMessage() );
			throw new UnableToSetAttributeException( this, attribute, value, context, e );
		}
	}

}
