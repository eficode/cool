package net.praqma.clearcase.exceptions;

public class ViewException extends ClearCaseException {

	private String context;
	private Type type;
	
	public enum Type {
		ACTIVITY_FAILED,
		REMOVE_FAILED,
		CREATION_FAILED,
		LOAD_FAILED,
		END_VIEW_FAILED,
		START_VIEW_FAILED,
		INFO_FAILED,
		DOES_NOT_EXIST,
		REBASING,
		VIEW_DOT_DAT,
		EMPTY,
		UNKNOWN
	}
	
	public ViewException( String message, String context, Type type ) {
		super( message );
		
		this.context = context;
		this.type = type;
	}
	
	public ViewException( String message, String context, Type type, Exception e ) {
		super( message, e );
		
		this.context = context;
		this.type = type;
	}

	public String getContext() {
		return context;
	}
	
	public Type getType() {
		return type;
	}
}