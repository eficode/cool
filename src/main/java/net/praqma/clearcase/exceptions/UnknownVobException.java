package net.praqma.clearcase.exceptions;

public class UnknownVobException extends ClearCaseException {
	/**
	 * 
	 */
	public UnknownVobException() {
		super();
	}

	public UnknownVobException( String s ) {
		super( s );
	}
	
	public UnknownVobException( Exception e ) {
		super( e );
	}
	
	public UnknownVobException( String s, Exception e ) {
		super( s, e );
	}

	
}