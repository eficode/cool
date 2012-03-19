package net.praqma.jenkins.utils.exceptions;

public class UCMViewException extends Exception {

	public UCMViewException( String message ) {
		super( message );
	}
	
	public UCMViewException( String message, Exception e ) {
		super( message, e );
	}
}
