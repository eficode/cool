package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.PVob;

public class UnableToListProjectsException extends ClearCaseException {

	private PVob pvob;
	
	public UnableToListProjectsException( PVob pvob, Exception e ) {
		super( e );
		this.pvob = pvob;
	}

	
	public PVob getPvob() {
		return pvob;
	}
}