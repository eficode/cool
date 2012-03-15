package net.praqma.clearcase;

import java.io.File;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.CmdResult;

public class Vob extends Cool implements Serializable {
	
	public static final Pattern rx_vob_get_path = Pattern.compile( "^\\s*VOB storage global pathname\\s*\"(.*?)\"\\s*$" );
	
	private static Logger logger = Logger.getLogger();

	protected String name;
	
	protected boolean projectVob = false;
	
	protected String storageLocation = null;

	public Vob(String name) {
		this.name = name;
	}
	
	public void load() throws CleartoolException {
		//context.loadVob(this);
		logger.debug( "Loading vob " + this );

		String cmd = "describe vob:" + this;

		try {
			/*
			 * We have to ignore any abnormal terminations, because describe can
			 * return != 0 even when the result is valid
			 */
			CmdResult r = Cleartool.run( cmd, null, true, true );

			if( r.stdoutBuffer.toString().contains( "Unable to determine VOB for pathname" ) ) {
				throw new CleartoolException( "The Vob " + getName() + " does not exist" );
			}

			if( r.stdoutBuffer.toString().contains( "Trouble opening VOB database" ) ) {
				throw new CleartoolException( "The Vob " + getName() + " could not be opened" );
			}

			for( String s : r.stdoutList ) {
				if( s.contains( "VOB storage global pathname" ) ) {
					Matcher m = rx_vob_get_path.matcher( s );
					if( m.find() ) {
						setStorageLocation( m.group( 1 ) );
					}
				} else if( s.contains( "project VOB" ) ) {
					setIsProjectVob( true );
				}
			}

		} catch( Exception e ) {
			throw new CleartoolException( "Could not load Vob: " + this, e );
		}
	}
	
	public void mount() throws CleartoolException {
		logger.debug( "Mounting vob " + this );

		String cmd = "mount " + this;
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			if( e.getMessage().contains( "is already mounted" ) ) {
				/* No op */
				return;
			}

			throw new CleartoolException( "Could not mount Vob " + this + ": " + e.getMessage() );
		}
	}
	
	public void unmount() throws CleartoolException {
		logger.debug( "UnMounting vob " + this );

		String cmd = "umount " + this;
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new CleartoolException( "Could not unmount Vob " + this + ": " + e.getMessage() );
		}
	}
	
	public void setStorageLocation( String storageLocation ) {
		this.storageLocation = storageLocation;
	}
	
	public String getStorageLocation() throws CleartoolException {
		if( storageLocation == null ) {
			load();
		}
		
		return this.storageLocation;
	}
	
	public void setIsProjectVob( boolean pvob ) {
		this.projectVob = pvob;
	}
	
	public boolean isProjectVob() throws CleartoolException {
		if( storageLocation == null ) {
			load();
		}
		
		return this.projectVob;
	}
	
	public String toString() {
		return name;
	}
	
	public String getName() {
		if( name.startsWith( "\\" )) {
			return name.substring( 1 );
		} else {
			return name;
		}
	}
	
	public static Vob create( String name, String path, String comment ) throws CleartoolException {
		return create( name, false, path, comment );
	}
	
	public static Vob create( String name, boolean UCMProject, String path, String comment ) throws CleartoolException {
		//context.createVob(name, false, path, comment);
		logger.debug( "Creating vob " + name );

		String cmd = "mkvob -tag " + name + ( UCMProject ? " -ucmproject" : "" ) + ( comment != null ? " -c \"" + comment + "\"" : "" ) + " -stgloc " + ( path != null ? path : "-auto" );

		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to create vob " + name, e );
		}
		
		Vob vob = new Vob(name);
		vob.storageLocation = path;
		
		return vob;
	}
	
	public void remove() throws CleartoolException {
		String cmd = "rmvob -force " + getStorageLocation();

		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new CleartoolException( "Could not remove Vob " + this, e );
		}
	}
	
	public static Vob get( String vobname ) {
		try {
			Vob vob = new Vob(vobname);
			vob.load();
			return vob;
		} catch( Exception e ) {
			return null;
		}
	}
	
	public static boolean isVob( File context) {
		logger.debug( "Testing " + context );

		String cmd = "lsvob \\" + context.getName();
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			logger.debug( "E=" + e.getMessage() );
			return false;
		}

		return true;
	}

}
