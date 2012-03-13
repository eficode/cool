package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.UCMEntity;

public class UnableToCreateEntityException extends CleartoolException {

	Class<? extends UCMEntity> clazz;

	public UnableToCreateEntityException( Class<? extends UCMEntity> clazz, Exception e ) {
		super( e );
		this.clazz = clazz;
	}
	
	public Class<? extends UCMEntity> getClazz() {
		return clazz;
	}

	
}