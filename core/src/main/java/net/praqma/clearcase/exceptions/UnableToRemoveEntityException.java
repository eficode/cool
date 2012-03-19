package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.UCMEntity;

public class UnableToRemoveEntityException extends ClearCaseException {

	private UCMEntity entity;

	public UnableToRemoveEntityException( UCMEntity entity, Exception e ) {
		super( e );
		this.entity = entity;
	}
	
	public UCMEntity getEntity() {
		return entity;
	}

	
}