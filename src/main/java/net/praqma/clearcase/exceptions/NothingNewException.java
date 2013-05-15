package net.praqma.clearcase.exceptions;

public class NothingNewException extends ClearCaseException {
	/**
	 * 
	 */
	public NothingNewException() {
		super();
	}

	public NothingNewException( String s ) {
		super( s );
	}
	
	public NothingNewException( Exception e ) {
		super( e );
	}
	
	public NothingNewException( String s, Exception e ) {
		super( s, e );
	}

	
}