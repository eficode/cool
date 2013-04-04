package net.praqma.clearcase.exceptions;

public class UnknownUserException extends ClearCaseException {
	/**
	 * 
	 */
	public UnknownUserException() {
		super();
	}

	public UnknownUserException( String s ) {
		super( s );
	}
	
	public UnknownUserException( Exception e ) {
		super( e );
	}
	
	public UnknownUserException( String s, Exception e ) {
		super( s, e );
	}

	
}