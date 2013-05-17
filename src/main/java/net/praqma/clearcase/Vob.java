package net.praqma.clearcase;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.command.ListVobs;
import net.praqma.clearcase.command.RemoveVob;
import net.praqma.clearcase.command.UnmountVob;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.EntityAlreadyExistsException;
import net.praqma.clearcase.exceptions.EntityNotLoadedException;
import net.praqma.clearcase.exceptions.NotMountedException;
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

	public static final Pattern rx_vob_get_path = Pattern.compile( "^\\s*VOB storage global pathname\\s*\"(.*?)\"\\s*$" );

	transient private static Logger logger = Logger.getLogger( Vob.class.getName() );

	protected String name;

	protected boolean projectVob = false;

	protected String storageLocation = null;

	public static final String rx_tag_format = "\\S+";

	public Vob( String name ) {
		this.name = name;
	}
	

	public static boolean isValidTag( String tag ) {
		return tag.matches( rx_tag_format );
	}

	public void load() throws CleartoolException {
		logger.fine( "Loading vob " + this );

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

	public void mount() throws NotMountedException {
		logger.fine( "Mounting vob " + this );

		String cmd = "mount " + this;
		
		/* Linux specifics 
		 * TODO we should check if the vob IS private, otherwise we should create it ourselves */
		if( Cool.getOS().equals( OperatingSystem.UNIX ) ) {
			logger.fine( "Creating mount-over directory" );
			File path = new File( this.getName() );
			if( !path.exists() && !path.mkdirs() ) {
				throw new NotMountedException( "Could not create mount-over directory" );
			}
		}
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			if( e.getMessage().contains( "is already mounted" ) ) {
				/* No op */
				return;
			}

			throw new NotMountedException( "Could not mount vob " + this, e );
		}
	}

	public void unmount() throws CleartoolException {
		try {
			new UnmountVob( this ).execute();
		} catch( AbnormalProcessTerminationException e ) {
			if( e.getMessage().equals( this + " is not currently mounted." ) ) {
				return;
			} else {
				throw new CleartoolException( "Could not unmount Vob " + this + ": " + e.getMessage() );
			}
		}
	}

	public void setStorageLocation( String storageLocation ) {
		this.storageLocation = storageLocation;
	}

	public String getStorageLocation() {
        if( this.storageLocation == null ) {
            try {
                load();
            } catch( CleartoolException e ) {
                throw new EntityNotLoadedException( name, name + " could not be auto loaded", e );
            }
        }
		return this.storageLocation;
	}

	public void setIsProjectVob( boolean pvob ) {
		this.projectVob = pvob;
	}

	public boolean isProjectVob() {
		return this.projectVob;
	}

	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public static Vob create( String name, String path, String comment ) throws CleartoolException, EntityAlreadyExistsException {
		return create( name, false, path, comment );
	}

	public static Vob create( String name, boolean UCMProject, String path, String comment ) throws CleartoolException, EntityAlreadyExistsException {
		logger.fine( "Creating vob " + name );

		String cmd = "mkvob -tag " + name + ( UCMProject ? " -ucmproject" : "" ) + ( comment != null ? " -c \"" + comment + "\"" : " -nc" ) + " -stgloc " + ( path != null ? path : "-auto" );

		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			if( e.getMessage().matches( "^(?s).*?A VOB tag already exists for.*?$" ) ) {
				throw new EntityAlreadyExistsException( name, e );
			} else {
				throw new CleartoolException( "Unable to create vob " + name, e );
			}
		}

		Vob vob = new Vob( name );
		vob.storageLocation = path;

		return vob;
	}

	public void remove() throws CleartoolException {
		new RemoveVob( this ).execute();
	}

    public void remove( boolean unmount ) throws CleartoolException {
        if( unmount ) {
            unmount();
        }
        new RemoveVob( this ).execute();
    }

	public static Vob get( String vobname ) {
		try {
			Vob vob = new Vob( vobname );
			vob.load();
			return vob;
		} catch( Exception e ) {
			return null;
		}
	}

	public static boolean isVob( File context ) {
		logger.fine( "Testing " + context );

		String cmd = "lsvob " + Cool.filesep + context.getName();
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			logger.fine( "E=" + e.getMessage() );
			return false;
		}

		return true;
	}
	
	@Override
	public boolean equals( Object other ) {
		if( other instanceof Vob ) {
			return ((Vob)other).name.equals( name );
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + ( null == name ? 0 : name.hashCode() );
		return hash;
	}

	@Override
	public String getFullyQualifiedName() {
		return "vob:" + this;
	}


    public static List<Vob> list( boolean ucmvobs ) {
        return new ListVobs().setVobType( ListVobs.VobType.get( ucmvobs ) ).execute().get();
    }
}
