package net.praqma.clearcase.exceptions;

import java.io.File;

import net.praqma.clearcase.ucm.entities.UCMEntity;

public class UnableToListAttributesException extends ClearCaseException {

	private UCMEntity entity;
	private File context;
	
	public UnableToListAttributesException( UCMEntity entity, File context, Exception e ) {
		super( e );
		
		this.entity = entity;
		this.context = context;
	}

	public UCMEntity getEntity() {
		return entity;
	}

	public File getContext() {
		return context;
	}
}