package net.praqma.clearcase.exceptions;


public class CleartoolException extends ClearCaseException {


	public CleartoolException() {
		super();
	}

	public CleartoolException( String s ) {
		super( s );
	}
	
	public CleartoolException( String s, Exception e ) {
		super( s, e );
	}

}