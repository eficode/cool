package net.praqma.clearcase;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UnableToCreateAttributeException;
import net.praqma.clearcase.exceptions.UnableToListAttributesException;
import net.praqma.clearcase.exceptions.UnableToSetAttributeException;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

public abstract class ClearCase extends Cool {
	
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	
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
		tracer.entering(ClearCase.class.getSimpleName(), "getAttributes", new Object[]{entity, context});
		
		String cmd = "describe -aattr -all " + entity;

		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		CmdResult res = null;
		try {
			res = Cleartool.run( cmd, context );
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Could not find attributes on " + fqname + ". Recieved: " + e.getMessage(), e.getMessage() );
			UnableToListAttributesException exception = new UnableToListAttributesException( entity, context, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.finest("Parsing Cleartool output for attributes.");
		
		Map<String, String> atts = new HashMap<String, String>();
		for( String s : res.stdoutList ) {
			/* A valid attribute */
			tracer.finest("Checking if line %s contains an attribute.");
			if( s.matches( rx_attr_find ) ) {
				tracer.finest("Line contains an attribute, adding to Map.");
				String[] data = s.split( "=" );
				atts.put( data[0].trim(), data[1].trim() );
			}
		}
		
		tracer.exiting(ClearCase.class.getSimpleName(), "getAttributes", atts);

		return atts;
	}

	public Map<String, String> getAttributes() throws UnableToListAttributesException {
		tracer.entering(ClearCase.class.getSimpleName(), "getAttributes", context);

		Map<String, String> result = getAttributes( this, null );
		tracer.exiting(ClearCase.class.getSimpleName(), "getAttributes", result);
		return result;
	}

	public Map<String, String> getAttributes( File context ) throws UnableToListAttributesException {
		tracer.entering(ClearCase.class.getSimpleName(), "getAttributes", context);

		Map<String, String> result = getAttributes( this, context );
		tracer.exiting(ClearCase.class.getSimpleName(), "getAttributes", result);
		return result;
	}

	public String getAttribute( String key ) throws UnableToListAttributesException {
		tracer.entering(ClearCase.class.getSimpleName(), "getAttribute", key);
		tracer.finest("Getting all attributes.");
		Map<String, String> atts = getAttributes( this, null );
		tracer.finest(String.format("Checking if attributes contain %s", key));
		String result;
		if( atts.containsKey( key ) ) {
			tracer.finest(String.format("Attribute %s found.", key));
			result = atts.get( key );
		} else {
			tracer.finest(String.format("Attribute %s not present.", key));
			result = null;
		}
		tracer.exiting(ClearCase.class.getSimpleName(), "getAttribute", result);
		return result;
	}

	public void setAttribute( String attribute, String value, boolean replace ) throws UnableToSetAttributeException {
		tracer.entering(ClearCase.class.getSimpleName(), "setAttribute", new Object[]{attribute, value, replace});
		setAttribute( attribute, value, replace, null );
		tracer.exiting(ClearCase.class.getSimpleName(), "setAttribute");
	}

	public void setAttribute( String attribute, String value, boolean replace, File context ) throws UnableToSetAttributeException {
		tracer.entering(ClearCase.class.getSimpleName(), "setAttribute", new Object[]{attribute, value, replace, context});
		tracer.finest("Checking that we are on a Windows machine.");
		
		if( Cool.getOS().equals( OperatingSystem.WINDOWS )) {
			tracer.finest("We are on a Windows machine, setting value.");
			value = "\\\"" + value + "\\\"";
		} else {
			tracer.finest("We are not on a Windows machine, setting value.");
			value = "'\"" + value + "\"'";
		}

		logger.debug( "Setting attribute " + attribute + "=" + value + " for " + this.getFullyQualifiedName() );

		String cmd = "mkattr " + ( replace ? "-replace " : "" ) + attribute + " " + value + " " + this.getFullyQualifiedName();
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		try {
			Cleartool.run( cmd, context );
		} catch( AbnormalProcessTerminationException e ) {
			tracer.finest("Could not run Cleartool command.");
			tracer.finest(String.format("Checking that we are trying to replace: %s", replace));

			if( !replace ) {
				tracer.finest("We are not trying to replace.");
				cmd = "mkattr " + attribute + " " + value + " " + this.getFullyQualifiedName();
				
				tracer.finest(String.format("Attempting to run new Cleartool command: %s", cmd));
				try {
					Cleartool.run( cmd, context );
				} catch( AbnormalProcessTerminationException e2 ) {
					UnableToSetAttributeException exception = new UnableToSetAttributeException( this, attribute, value, context, e2 );
					tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
					throw exception;
				}
				tracer.finest("Successfully ran Cleartool command.");
			}
			// TODO Do we want to hit this exception even if we have run the second Cleartool command?
			UnableToSetAttributeException exception = new UnableToSetAttributeException( this, attribute, value, context, e );
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.exiting(ClearCase.class.getSimpleName(), "setAttribute");
	}
	
	/**
	 * Create an attribute with no possibility of specifying type or range.
	 * @param name Name of the type
	 * @param pvob The PVob
	 * @param replace
	 * @throws CleartoolException
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
