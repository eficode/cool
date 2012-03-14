package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.UCMEntity;

public class TagException extends CleartoolException {

	private UCMEntity entity;
	private String cgi;
	private String tagName;
	private Type type;
	
	public enum Type {
		NO_SUCH_HYPERLINK,
		CREATION_FAILED,
		DELETION_FAILED
	}
	
	public TagException( UCMEntity entity, String cgi, String tagName, Type type ) {
		super();
		
		this.entity = entity;
		this.tagName = tagName;
		this.cgi = cgi;
		this.type = type;
	}
	
	public TagException( UCMEntity entity, String cgi, String tagName, Type type, Exception e ) {
		super( e );
		
		this.entity = entity;
		this.tagName = tagName;
		this.cgi = cgi;
		this.type = type;
	}

	public UCMEntity getEntity() {
		return entity;
	}
	
	public String getCgi() {
		return cgi;
	}	
	
	public String getTagName() {
		return tagName;
	}
	
	public Type getType() {
		return type;
	}
}