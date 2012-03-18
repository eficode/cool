package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.UCMEntity;

//public class UCMException extends RuntimeException
public class UnableToLoadEntityException extends ClearCaseException {
	
	private UCMEntity entity;
	
	public UnableToLoadEntityException() {
		super();
	}

	public UnableToLoadEntityException( String s ) {
		super( s );
	}
	
	public UnableToLoadEntityException( Exception e ) {
		super( e );
	}
	
	public UnableToLoadEntityException( UCMEntity entity, Exception e ) {
		super( e );
		this.entity = entity;
	}

	public UCMEntity getEntity() {
		return entity;
	}
	
}