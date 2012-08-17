package net.praqma.clearcase;

import java.io.File;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.EntityAlreadyExistsException;
import net.praqma.clearcase.exceptions.NotMountedException;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

/**
 * Vob class represented by a fully qualified vob name, including \ or /<br>
 * To get the name of vob use getName()
 * 
 * @author wolfgang
 * 
 */
public class Vob extends ClearCase implements Serializable {
	
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	public static final Pattern rx_vob_get_path = Pattern.compile( "^\\s*VOB storage global pathname\\s*\"(.*?)\"\\s*$" );

	private static Logger logger = Logger.getLogger();

	protected String name;

	protected boolean projectVob = false;

	protected String storageLocation = null;

	public static final String rx_tag_format = "\\S+";

	public Vob( String name ) {
		tracer.finest("Constructor called for class Vob.");
		tracer.finest(String.format("Input parameter name type: %s; value: %s", name.getClass(), name));
		
		this.name = name;
		
		tracer.finest("Ending execution of constructor - Vob");
	}
	
	public static boolean isValidTag( String tag ) {
		tracer.finest("Starting execution of method - isValidTag");
		tracer.finest(String.format("Input parameter tag type: %s; value: %s", tag.getClass(), tag));
		boolean result = tag.matches( rx_tag_format );
		tracer.finest(String.format("Returning value: %s", result));
		return result;
	}

	public void load() throws CleartoolException {
		tracer.finest("Starting execution of method - load");
		
		//context.loadVob(this);
		logger.debug( "Loading vob " + this );

		String cmd = "describe vob:" + this;

		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		try {
			/*
			 * We have to ignore any abnormal terminations, because describe can
			 * return != 0 even when the result is valid
			 */
			CmdResult r = Cleartool.run( cmd, null, true, true );

			tracer.finest("Checking if Cleartool was unable to find VOB.");
			
			if( r.stdoutBuffer.toString().contains( "Unable to determine VOB for pathname" ) ) {
				tracer.finest("Cleartool was unable to find VOB, throwing exception.");
				
				CleartoolException exception = new CleartoolException( "The Vob " + getName() + " does not exist" );
				
				tracer.severe(String.format("Throwing exception type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
			
			tracer.finest("Checking if Cleartool was unable to open VOB.");
			if( r.stdoutBuffer.toString().contains( "Trouble opening VOB database" ) ) {
				tracer.finest("Cleartool was unable to open VOB.");
				
				CleartoolException exception = new CleartoolException( "The Vob " + getName() + " could not be opened" );
				
				tracer.severe(String.format("Throwing exception type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}

			tracer.finest("Parsing Cleartool stdout.");
			
			for( String s : r.stdoutList ) {
				tracer.finest("Checking if VOB path or project was found.");
				if( s.contains( "VOB storage global pathname" ) ) {
					tracer.finest("VOB path was found.");
					tracer.finest(String.format("Checking if path matches predefined pattern: %s", rx_vob_get_path));
					
					Matcher m = rx_vob_get_path.matcher( s );
					if( m.find() ) {
						tracer.finest("Path matches predefined pattern, setting storage location.");
						setStorageLocation( m.group( 1 ) );
					}
				} else if( s.contains( "project VOB" ) ) {
					tracer.finest("Project was found, setting projectVob to true.");
					setIsProjectVob( true );
				}
			}

		} catch( Exception e ) {
			CleartoolException exception = new CleartoolException( "Could not load Vob: " + this, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.finest("Ending execution of method - load");
	}

	public void mount() throws NotMountedException {
		tracer.finest("Starting execution of method - mount");
		
		logger.debug( "Mounting vob " + this );

		String cmd = "mount " + this;
		
		tracer.finest("Checking if we are on a Unix platform.");
		
		/* Linux specifics 
		 * TODO we should check if the vob IS private, otherwise we should create it ourselves */
		if( Cool.getOS().equals( OperatingSystem.UNIX ) ) {
			logger.debug( "Creating mount-over directory" );
			
			tracer.finest("Creating directory from Vob name.");
			
			File path = new File( this.getName() );
			
			tracer.finest(String.format("path set to: %s", path.getAbsolutePath()));
			
			tracer.finest("Checking if path exists, if not create the directory.");
			if( !path.exists() && !path.mkdirs() ) {
				tracer.finest("Could not create directory, throwing exception.");
				
				NotMountedException exception = new NotMountedException( "Could not create mount-over directory" );
				
				tracer.severe(String.format("Throwing exception type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
		}
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			tracer.finest(String.format("Exception caught type: %s; message: %s", e.getClass(), e.getMessage()));
			tracer.finest("Checking if Vob is already mounted.");
			if( e.getMessage().contains( "is already mounted" ) ) {
				tracer.finest("Vob is already mounted, returning.");
				/* No op */
				return;
			} else {
				tracer.finest("Vob is not already mounted, throwing exception:");
				NotMountedException exception = new NotMountedException( "Could not mount vob " + this, e );

				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));

				throw exception;
			}
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.finest("Ending execution of method - mount");
	}

	public void unmount() throws CleartoolException {
		tracer.finest("Starting execution of method - unmount");
		
		logger.debug( "UnMounting vob " + this );

		String cmd = "umount " + this;
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			tracer.finest(String.format("Exception caught type: %s; message: %s", e.getClass(), e.getMessage()));
			tracer.finest("Checking if Vob is not currently mounted.");
			if( e.getMessage().equals( this + " is not currently mounted." ) ) {
				tracer.finest("Vob is not currently mounted, returning");
				return;
			} else {
				tracer.finest("Could not unmount Vob, throwing exception:");
				
			CleartoolException exception = new CleartoolException( "Could not unmount Vob " + this + ": " + e.getMessage() );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.finest("Ending execution of method - unmount");
	}

	public void setStorageLocation( String storageLocation ) {
		tracer.finest("Starting execution of method - setStorageLocation");
		tracer.finest(String.format("Input parameter storageLocation type: %s; value: %s", storageLocation.getClass(), storageLocation));
		
		this.storageLocation = storageLocation;
		
		tracer.finest("Ending execution of method - setStorageLocation");
	}

	public String getStorageLocation() {
		tracer.finest("Starting execution of method - getStorageLocation");
		tracer.finest(String.format("Returning value: %s", this.storageLocation));
		return this.storageLocation;
	}

	public void setIsProjectVob( boolean pvob ) {
		tracer.finest("Starting execution of method - setIsProjectVob");
		tracer.finest(String.format("Input parameter pvob type: %s; value: %s", "boolean", pvob));
		
		this.projectVob = pvob;
		tracer.finest("Ending execution of method - setIsProjectVob");
	}

	public boolean isProjectVob() {
		tracer.finest("Starting execution of method - setIsProjectVob");
		tracer.finest(String.format("Returning value: %s", this.projectVob));
		
		return this.projectVob;
	}

	public String toString() {
		return name;
	}

	public String getName() {
		tracer.finest("Starting execution of method - getName");
		tracer.finest(String.format("Returning value: %s", this.name));
		
		return name;
	}

	public static Vob create( String name, String path, String comment ) throws CleartoolException, EntityAlreadyExistsException {
		tracer.finest("Starting execution of method - create(String name, String path, String comment)");
		tracer.finest(String.format("Input parameter name type: %s; value: %s", name.getClass(), name));
		tracer.finest(String.format("Input parameter path path: %s; value: %s", path.getClass(), path));
		tracer.finest(String.format("Input parameter comment type: %s; value: %s", comment.getClass(), comment));
		
		Vob output = create( name, false, path, comment ); 
		
		tracer.finest(String.format("Returning value: %s", output));
		
		return output;
	}

	public static Vob create( String name, boolean UCMProject, String path, String comment ) throws CleartoolException, EntityAlreadyExistsException {
		tracer.finest("Starting execution of method - create(String name, boolean UCMProject, String path, String comment)");
		tracer.finest(String.format("Input parameter name type: %s; value: %s", name.getClass(), name));
		tracer.finest(String.format("Input parameter UCMProject type: %s; value: %s", "boolean", UCMProject));
		tracer.finest(String.format("Input parameter path path: %s; value: %s", path.getClass(), path));
		tracer.finest(String.format("Input parameter comment type: %s; value: %s", comment.getClass(), comment));
		
		//context.createVob(name, false, path, comment);
		logger.debug( "Creating vob " + name );

		String cmd = "mkvob -tag " + name + ( UCMProject ? " -ucmproject" : "" ) + ( comment != null ? " -c \"" + comment + "\"" : " -nc" ) + " -stgloc " + ( path != null ? path : "-auto" );

		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			tracer.finest(String.format("Exception caught type: %s; message: %s", e.getClass(), e.getMessage()));
			tracer.finest("Checking if exception was thrown because Vob already exists.");
			if( e.getMessage().matches( "^(?s).*?A VOB tag already exists for.*?$" ) ) {
				tracer.finest("Exception was thrown because Vob already exists.");
				
				EntityAlreadyExistsException exception = new EntityAlreadyExistsException( name, e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			} else {
				tracer.finest("Exception was thrown, but Vob does not already exist.");
				
				CleartoolException exception = new CleartoolException( "Unable to create vob " + name, e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
				
				throw exception;
			}
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.finest("Creating Vob object.");
		Vob vob = new Vob( name );
		vob.storageLocation = path;
		
		tracer.finest(String.format("Retruning value: %s", vob));
		
		return vob;
	}

	public void remove() throws CleartoolException {
		tracer.entering(Vob.class.getSimpleName(), "remove");
		
		String cmd = "rmvob -force " + getStorageLocation();

		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			CleartoolException exception = new CleartoolException( "Could not remove Vob " + this, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.exiting(Vob.class..getSimpleName(), "remove");
	}

	public static Vob get( String vobname ) {
		tracer.entering(Vob.class.getSimpleName(), "get", vobname);
		
		tracer.finest("Attempting to load Vob...");
		Vob vob = null;
		
		try {
			vob = new Vob( vobname );
			vob.load();
		} catch( Exception e ) {
			return null;
		}
		tracer.finest("Successfully loaded Vob.");
		tracer.exiting(Vob.class.getSimpleName(), "get", vob);
		
		return vob;
	}

	public static boolean isVob( File context ) {
		tracer.entering(Vob.class.getSimpleName(), "isVob", context);
		
		logger.debug( "Testing " + context );

		String cmd = "lsvob \\" + context.getName();
		
		tracer.finest(String.format("Attempting to run Cleartool command: %s", cmd));
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			logger.debug( "E=" + e.getMessage() );
			
			tracer.exiting("Vob", "isVob", false);
			
			return false;
		}
		tracer.finest("Successfully ran Cleartool command.");
		tracer.exiting(Vob.class.getSimpleName(), "isVob", true);
		return true;
	}
	
	@Override
	public boolean equals( Object other ) {
		tracer.entering(Vob.class.getSimpleName(), "equals", other);
		tracer.finest("Checking if other is a Vob object.");
		
		if( other instanceof Vob ) {
			tracer.finest("other is a Vob object.");
			
			boolean result = ((Vob)other).name.equals( name );
			
			tracer.exiting(this.getClass().getSimpleName(), "equals", result);
			
			return result;
		} else {
			tracer.finest("other is not a Vob object.");
			tracer.exiting(Vob.class.getSimpleName(), "equals", false);
			
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		tracer.entering(Vob.class.getSimpleName(), "hashCode");
		
		int hash = 7;
		hash = 31 * hash + ( null == name ? 0 : name.hashCode() );
		
		tracer.exiting(Vob.class.getSimpleName(), "hashCode", hash);
		return hash;
	}

	@Override
	public String getFullyQualifiedName() {
		tracer.entering(Vob.class.getSimpleName(), "getFullyQualifiedName");
		
		String result = "vob:" + this;
		
		tracer.exiting(Vob.class.getSimpleName(), "getFullyQualifiedName", result);
		return result;
	}

}
