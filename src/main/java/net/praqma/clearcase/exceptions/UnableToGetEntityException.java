package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.UCMEntity;

//public class UCMException extends RuntimeException
public class UnableToGetEntityException extends ClearCaseException {
	
	private UCMEntity entity;

	
	public UnableToGetEntityException( UCMEntity entity, Exception e ) {
		super( e );
		this.entity = entity;
	}

	public UCMEntity getEntity() {
		return entity;
	}
	
}