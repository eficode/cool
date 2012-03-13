package net.praqma.clearcase.ucm.entities;

import java.io.File;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UCMException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class Activity extends UCMEntity {
	
	/* Activity specific fields */
	public Changeset changeset = new Changeset();
	private boolean specialCase = false;

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
	 * 
	 * @throws UCMException
	 */
	public UCMEntity load() throws UnableToLoadEntityException {
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
				throw new UnableToLoadEntityException( this, e );
			}
		}
		
		setUser( result );
		
		return this;
	}
	
	/**
	 * Create an activity. If name is null an anonymous activity is created and the return value is null.
	 * @param name
	 * @param pvob
	 * @param force
	 * @param comment
	 * @param view
	 * @return
	 * @throws UnableToCreateEntityException 
	 */
	public static Activity create( String name, PVob pvob, boolean force, String comment, File view ) throws UnableToCreateEntityException {
		String cmd = "mkactivity" + ( comment != null ? " -c \"" + comment + "\"" : "" ) + ( force ? " -force" : "" ) + ( name != null ? " " + name + "@" + pvob : "" );

		try {
			Cleartool.run( cmd, view );
		} catch( Exception e ) {
			//throw new UCMException( e.getMessage(), UCMType.CREATION_FAILED );
			throw new UnableToCreateEntityException( Activity.class, e );
		}
		
		Activity activity = null;
		
		if( name != null ) {
			activity = get( name, pvob, true );
		}
		return activity;
	}
	
	
	
	public static Activity get( String name ) {
		return get( name, true );
	}

	public static Activity get( String name, boolean trusted ) {
		if( !name.startsWith( "activity:" ) ) {
			name = "activity:" + name;
		}
		Activity entity = (Activity) UCMEntity.getEntity( Activity.class, name, trusted );
		return entity;
	}
	
	public static Activity get( String name, PVob pvob, boolean trusted ) {
		if( !name.startsWith( "activity:" ) ) {
			name = "activity:" + name;
		}
		Activity entity = (Activity) UCMEntity.getEntity( Activity.class, name + "@" + pvob, trusted );
		return entity;
	}
	
}
