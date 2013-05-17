package net.praqma.clearcase.exceptions;

import java.io.File;

import net.praqma.clearcase.ClearCase;
import net.praqma.clearcase.ucm.entities.UCMEntity;

public class UnableToListAttributesException extends ClearCaseException {

	private ClearCase entity;
	private File context;
	
	public UnableToListAttributesException( ClearCase entity2, File context, Exception e ) {
		super( e );
		
		this.entity = entity2;
		this.context = context;
	}

	public ClearCase getEntity() {
		return entity;
	}

	public File getContext() {
		return context;
	}
}