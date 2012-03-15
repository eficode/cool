package net.praqma.clearcase.exceptions;


public class BuildNumberException extends ClearCaseException {

	private Type type = Type.DEFAULT;
	
	public enum Type {
		DEFAULT,
		ZERO_MATCHES,
		MISSING_ATTRIBUTE
	}

	public BuildNumberException() {
		super();
	}

	public BuildNumberException( String s ) {
		super( s );
	}
	
	public BuildNumberException( String s, Type type ) {
		super( s );
		this.type = type;
	}
	
	public BuildNumberException( String s, Exception e ) {
		super( s, e );
	}
	
	public BuildNumberException( String s, Exception e, Type type ) {
		super( s, e );
		this.type = type;
	}

}