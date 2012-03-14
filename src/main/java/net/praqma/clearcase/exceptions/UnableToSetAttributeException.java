package net.praqma.clearcase.exceptions;

import java.io.File;

import net.praqma.clearcase.ucm.entities.UCMEntity;

public class UnableToSetAttributeException extends CleartoolException {

	private UCMEntity entity;
	private File context;
	private String key;
	private String value;
	
	public UnableToSetAttributeException( UCMEntity entity, String key, String value, File context, Exception e ) {
		super( e );
		
		this.entity = entity;
		this.context = context;
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public UCMEntity getEntity() {
		return entity;
	}

	public File getContext() {
		return context;
	}
}