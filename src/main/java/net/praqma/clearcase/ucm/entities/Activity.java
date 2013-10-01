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
import net.praqma.clearcase.api.DiffBl;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class Activity extends UCMEntity {
	
	//private final static Pattern pattern_activity = Pattern.compile( "^>>\\s*(\\S+)\\s*.*$" );
	private final static Pattern pattern_activity = Pattern.compile( "^[<>-]{2}\\s*(\\S+)\\s*.*$" );
    private final static Pattern pattern_activity2 = Pattern.compile( "^([<>-]{2})\\s*(\\S+)\\s*.*$" );
	
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
        this.loaded = true;
		
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

    public static class Parser {
        public enum Direction {
            /** The item is present only in baseline-selector1 or stream-selector1. */
            LEFT( "<<" ),
            /** The activity is present in both of the items being compared and is newer in baseline-selector1 or stream-selector1. */
            LEFTI( "<-" ),
            /** The item is present only in baseline-selector2 or stream-selector2. */
            RIGHT( ">>" ),
            /** The activity is present in both of the items being compared and is newer in baseline-selector2 or stream-selector2. */
            RIGHTI( "->" );

            private String symbol;

            private Direction( String symbol ) {
                this.symbol = symbol;
            }

            public boolean matches( String symbol ) {
                return this.symbol.equals( symbol );
            }
        }

        private List<Direction> directions = new ArrayList<Direction>( 4 );

        private DiffBl diffBl;

        private boolean activityUserAsVersionUser = false;

        private int length = 0;

        private List<Activity> activities = new ArrayList<Activity>(  );

        public Parser( DiffBl diffBl ) {
            this.diffBl = diffBl;
            if( diffBl.getViewRoot() != null ) {
                length = diffBl.getViewRoot().getAbsoluteFile().toString().length();
            }
        }

        public List<Activity> getActivities() {
            return activities;
        }

        public Parser addDirection( Direction direction ) {
            this.directions.add( direction );

            return this;
        }

        public Parser setActivityUserAsVersionUser( boolean b ) {
            activityUserAsVersionUser = b;

            return this;
        }

        private boolean hasDirection( String symbol ) {
            for( Direction direction : directions ) {
                if( direction.matches( symbol ) ) {
                    return true;
                }
            }

            return false;
        }

        public Parser parse() throws ClearCaseException {
            Activity current = null;
            boolean include = false;

            List<String> lines = diffBl.execute();

            for( String line : lines ) {
                logger.finest( "Line: " + line );

			    /* Get activity */
                Matcher match = pattern_activity2.matcher( line );

                /* This line is a new activity */
                if( match.find() ) {
                    /* Test direction */
                    String symbol = match.group( 1 );
                    if( hasDirection( symbol ) ) {
                        current = get( match.group( 2 ) );

                        /* A special case? */
                        if( current.getShortname().equals( "no_activity" ) ) {
                            logger.fine( "Recorded a special activity case" );
                            current.setSpecialCase( true );
                        }
                        activities.add( current );
                        include = true;
                    } else {
                        include = false;
                    }

                    continue;
                }

                if( include ) {
                    if( current == null ) {
                        logger.fine( "Current is not an activity: " + line );
                        continue;
                    }

                    /* If not an activity, it must be a version */
                    String f = line.trim();

                    Version v = (Version) UCMEntity.getEntity( Version.class, f );
                    v.setSFile( v.getFile().getAbsolutePath().substring( length ) );
                    if( activityUserAsVersionUser ) {
                        v.setUser( current.getUser() );
                    } else {
                        v.load();
                    }

                    current.changeset.versions.add( v );
                }
            }

            return this;
        }
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
