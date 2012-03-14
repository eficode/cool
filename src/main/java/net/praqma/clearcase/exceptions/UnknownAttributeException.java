package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.UCMEntity;

public class UnknownAttributeException extends CleartoolException {

	private UCMEntity entity;
	private String attribute;
	
	public UnknownAttributeException( UCMEntity entity, String attribute, Exception e ) {
		super( e );
		
		this.entity = entity;
		this.attribute = attribute;
	}

	public UCMEntity getEntity() {
		return entity;
	}

	public String getAttribute() {
		return attribute;
	}
}