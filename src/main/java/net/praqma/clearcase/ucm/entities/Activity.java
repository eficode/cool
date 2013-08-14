package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.api.Describe;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.*;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class Activity extends UCMEntity {
	
	//private final static Pattern pattern_activity = Pattern.compile( "^>>\\s*(\\S+)\\s*.*$" );
	private final static Pattern pattern_activity = Pattern.compile( "^[<>-]{2}\\s*(\\S+)\\s*.*$" );
	
	private static Logger logger = Logger.getLogger( Activity.class.getName() );

    /**
     * The change set of the activity
     */
	public Changeset changeset = new Changeset();

    /**
     * The specialCase field is used when parsing an activity string from cleartool,<br />
     * when there's no_activity
     */
	private boolean specialCase = false;

    /**
     * The headline of the {@link Activity}
     */
	private String headline = "";

	Activity() {
		super( "activity" );
	}

	public void setSpecialCase( boolean b ) {
		this.specialCase = b;
	}

	public boolean isSpecialCase() {
		return this.specialCase;
	}

	/**
	 * Load the Activity into memory from ClearCase.<br>
	 * This function is automatically called when needed by other functions.
	 * @return 
	 * @throws UnableToLoadEntityException
	 */
	public Activity load() throws UnableToLoadEntityException {
		String[] result = new String[2];

		/* The special case branch */
		if( isSpecialCase() ) {
			result[0] = "System";
			result[1] = "";
		} else {
			String cmd = "describe -fmt %u{!}%[headline]p " + this;
			try {
				String line = Cleartool.run( cmd ).stdoutBuffer.toString();
                result = line.split( "\\{!\\}" );
			} catch( AbnormalProcessTerminationException e ) {
				throw new UnableToLoadEntityException( this, e );
			}
		}
		
		setUser( result[0].trim() );
		headline = result[1].trim();
		
		return this;
	}
	
	/**
	 * Create an activity. If name is null an anonymous activity is created and the return value is null.
	 * @param name
	 * @param in
	 * @param pvob
	 * @param force
	 * @param comment
	 * @param headline
	 * @param view
	 * @return
	 * @throws UnableToCreateEntityException
	 * @throws UCMEntityNotFoundException
	 * @throws UnableToGetEntityException
	 * @throws UnableToInitializeEntityException
	 */
	public static Activity create( String name, Stream in, PVob pvob, boolean force, String comment, String headline, File view ) throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException, UnableToInitializeEntityException {
		String cmd = "mkactivity" + ( comment != null ? " -c \"" + comment + "\"" : " -nc" ) + 
									( headline != null ? " -headline \"" + headline + "\"" : "" ) +
									( in != null ? " -in " + in.getNormalizedName() : "" ) + 
									( force ? " -force" : "" ) + 
									( name != null ? " " + name + "@" + pvob : "" );

		try {
			Cleartool.run( cmd, view );
		} catch( Exception e ) {
			throw new UnableToCreateEntityException( Activity.class, e );
		}
		
		Activity activity = null;
		
		if( name != null ) {
			activity = get( name, pvob );
		}
		
		return activity;
	}
	
	
	
	public static List<Activity> parseActivityStrings( List<String> result, int length ) throws UnableToLoadEntityException, UCMEntityNotFoundException, UnableToInitializeEntityException {
		ArrayList<Activity> activities = new ArrayList<Activity>();
		Activity current = null;
		//System.out.println("PARSING:");
		for( String s : result ) {
			/* Get activity */
			Matcher match = pattern_activity.matcher( s );

			/* This line is a new activity */
			if( match.find() ) {
				current = get( match.group( 1 ) );

				/* A special case? */
				if( current.getShortname().equals( "no_activity" ) ) {
					logger.fine( "Recorded a special activity case" );
					current.setSpecialCase( true );
				}
				activities.add( current );
				continue;
			}

			if( current == null ) {
				logger.fine( "Not an activity: " + s );
				continue;
			}

			/* If not an activity, it must be a version */
			String f = s.trim();

			Version v = (Version) UCMEntity.getEntity( Version.class, f ).load();
			v.setSFile( v.getFile().getAbsolutePath().substring( length ) );

			current.changeset.versions.add( v );
		
		}

		return activities;
	}
	
	public String getHeadline() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		return headline;
	}

    public List<Version> getVersions( File path ) throws UnableToInitializeEntityException, CleartoolException {
        return getVersions( this, path );
    }

    public static List<Version> getVersions( Activity activity, File path ) throws UnableToInitializeEntityException {
        logger.fine( "Getting versions for " + activity );

        String output = null;
        try {
            output = new Describe( activity ).addModifier( Describe.versions ).setPath( path ).executeGetFirstLine();
        } catch( CleartoolException e ) {
            logger.fine( e.getMessage() );
            return Collections.emptyList();
        }

        String[] versionNames = output.split( "," );

        List<Version> versions = new ArrayList<Version>( versionNames.length );

        for( String versionName : versionNames ) {
            versions.add( Version.get( versionName.trim() ) );
        }

        return versions;
    }
	
	public static Activity get( String name ) throws UnableToInitializeEntityException {
		if( !name.startsWith( "activity:" ) ) {
			name = "activity:" + name;
		}
		Activity entity = (Activity) UCMEntity.getEntity( Activity.class, name );
		return entity;
	}

	public static Activity get( String name, PVob pvob ) throws UnableToInitializeEntityException {
		if( !name.startsWith( "activity:" ) ) {
			name = "activity:" + name;
		}
		Activity entity = (Activity) UCMEntity.getEntity( Activity.class, name + "@" + pvob );
		return entity;
	}
	
}
