package net.praqma.clearcase.ucm.entities;

import java.io.File;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.UCMException;

public class Activity extends UCMEntity {
	/* Activity specific fields */
	public Changeset changeset = new Changeset();
	private boolean specialCase = false;

	Activity() {
	}

	public void setSpecialCase( boolean b ) {
		this.specialCase = b;
	}

	public boolean isSpecialCase() {
		return this.specialCase;
	}

	/**
	 * This method is only available to the package, because only
	 * ClearcaseEntity should be allowed to call it.
	 * 
	 * @return A new Activity Entity
	 */
	static Activity getEntity() {
		return new Activity();
	}

	/**
	 * Load the Activity into memory from ClearCase.<br>
	 * This function is automatically called when needed by other functions.
	 * 
	 * @throws UCMException
	 */
	public void load() throws UCMException {
		context.loadActivity( this );
	}
	
	public Activity create( String name, PVob pvob, boolean force, String comment ) throws UCMException {
		context.createActivity( name, pvob, force, comment );
		
		Activity activity = UCMEntity.getActivity( name, pvob, true );
		return activity;
	}
	
}
