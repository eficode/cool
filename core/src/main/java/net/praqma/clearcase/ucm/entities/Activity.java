package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class Activity extends UCMEntity {
	
	//private final static Pattern pattern_activity = Pattern.compile( "^>>\\s*(\\S+)\\s*.*$" );
	private final static Pattern pattern_activity = Pattern.compile( "^[<>-]{2}\\s*(\\S+)\\s*.*$" );
	
	private static Logger logger = Logger.getLogger();
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	
	/* Activity specific fields */
	public Changeset changeset = new Changeset();
	private boolean specialCase = false;

	Activity() {
		super( "activity" );
	}

	public void setSpecialCase( boolean b ) {
		tracer.entering(Activity.class.getSimpleName(), "setSpecialCase", b);
		
		this.specialCase = b;
		
		tracer.exiting(Activity.class.getSimpleName(), "setSpecialCase", b);
	}

	public boolean isSpecialCase() {
		tracer.entering(Activity.class.getSimpleName(), "isSpecialCase");
		tracer.exiting(Activity.class.getSimpleName(), "setSpecialCase", specialCase);
		
		return this.specialCase;
	}

	/**
	 * Load the Activity into memory from ClearCase.<br>
	 * This function is automatically called when needed by other functions.
	 * @return 
	 * @throws UnableToLoadEntityException 
	 * 
	 * @throws UCMException
	 */
	public Activity load() throws UnableToLoadEntityException {
		tracer.entering(Activity.class.getSimpleName(), "load");
		
		String result = "";

		
		/* The special case branch */
		if( isSpecialCase() ) {
			result = "System";
		} else {
			String cmd = "describe -fmt %u " + this;
			
			try {
				result = Cleartool.run( cmd ).stdoutBuffer.toString();
			} catch( AbnormalProcessTerminationException e ) {
				//throw new UCMException( e.getMessage(), e.getMessage() );
				UnableToLoadEntityException exception = new UnableToLoadEntityException( this, e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
				
				throw exception;
			}
		}
		
		setUser( result );
		
		this.loaded = true;
		
		tracer.exiting(Activity.class.getSimpleName(), "load", this);
		
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
		tracer.entering(Activity.class.getSimpleName(), "create", new Object[]{name,in,pvob,force,comment,headline,view});
		
		String cmd = "mkactivity" + ( comment != null ? " -c \"" + comment + "\"" : " -nc" ) + 
									( headline != null ? " -headline \"" + headline + "\"" : "" ) +
									( in != null ? " -in " + in.getNormalizedName() : "" ) + 
									( force ? " -force" : "" ) + 
									( name != null ? " " + name + "@" + pvob : "" );

		try {
			Cleartool.run( cmd, view );
		} catch( Exception e ) {
			UnableToCreateEntityException exception = new UnableToCreateEntityException( Activity.class, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		
		Activity activity = null;
		
		if( name != null ) {
			activity = get( name, pvob );
		}
		
		tracer.exiting(Activity.class.getSimpleName(), "create", activity);
		
		return activity;
	}
	
	
	
	public static List<Activity> parseActivityStrings( List<String> result, int length ) throws UnableToLoadEntityException, UCMEntityNotFoundException, UnableToInitializeEntityException {
		tracer.entering(Activity.class.getSimpleName(), "parseActivityStrings", new Object[]{result, length});
		
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
					logger.debug( "Recorded a special activity case" );
					current.setSpecialCase( true );
				}
				activities.add( current );
				continue;
			}

			if( current == null ) {
				logger.debug( "Not an activity: " + s );
				continue;
			}

			/* If not an activity, it must be a version */
			String f = s.trim();

			/*
			 * If the version cannot be instantiated and is a special case, skip
			 * it
			 */
			
			Version v = (Version) UCMEntity.getEntity( Version.class, f ).load();
			v.setSFile( v.getFile().getAbsolutePath().substring( length ) );
			//System.out.println(f);
			current.changeset.versions.add( v );
		
		}
		
		tracer.exiting(Activity.class.getSimpleName(), "create", activities);
		
		return activities;
	}
	
	public static Activity get( String name ) throws UnableToInitializeEntityException {
		tracer.entering(Activity.class.getSimpleName(), "get", name);
		
		if( !name.startsWith( "activity:" ) ) {
			name = "activity:" + name;
		}
		Activity entity = (Activity) UCMEntity.getEntity( Activity.class, name );
		
		tracer.exiting(Activity.class.getSimpleName(), "get", entity);
		
		return entity;
	}

	public static Activity get( String name, PVob pvob ) throws UnableToInitializeEntityException {
		tracer.entering(Activity.class.getSimpleName(), "get", new Object[]{name,pvob});
		
		if( !name.startsWith( "activity:" ) ) {
			name = "activity:" + name;
		}
		Activity entity = (Activity) UCMEntity.getEntity( Activity.class, name + "@" + pvob );
		
		tracer.exiting(Activity.class.getSimpleName(), "get", entity);
		
		return entity;
	}
}
