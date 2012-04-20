package net.praqma.clearcase.exceptions;

import java.io.File;

import net.praqma.clearcase.ClearCase;
import net.praqma.clearcase.ucm.entities.UCMEntity;

public class UnableToSetAttributeException extends ClearCaseException {

	private ClearCase entity;
	private File context;
	private String key;
	private String value;
	
	public UnableToSetAttributeException( ClearCase clearCase, String key, String value, File context, Exception e ) {
		super( e );
		
		this.entity = clearCase;
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

	public ClearCase getEntity() {
		return entity;
	}

	public File getContext() {
		return context;
	}
}