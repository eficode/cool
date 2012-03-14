package net.praqma.clearcase.exceptions;

import java.io.File;

import net.praqma.clearcase.ucm.entities.UCMEntity;

public class HyperlinkException extends CleartoolException {

	private UCMEntity entity;
	private File context;
	private String hyperlinkType;
	
	public HyperlinkException( UCMEntity entity, File context, String hyperlinkType, Exception e ) {
		super( e );
		
		this.entity = entity;
		this.context = context;
		
		this.hyperlinkType = hyperlinkType;
	}

	public UCMEntity getEntity() {
		return entity;
	}

	public File getContext() {
		return context;
	}
	
	public String getHyperlinkType() {
		return hyperlinkType;
	}
}