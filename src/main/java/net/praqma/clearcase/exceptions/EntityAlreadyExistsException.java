package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.UCMEntity;

public class EntityAlreadyExistsException extends ClearCaseException {

	private String name;
	
	public EntityAlreadyExistsException( String name, Exception e ) {
		super( e );
		this.name = name;
	}

	public String getName() {
		return name;
	}

	
}