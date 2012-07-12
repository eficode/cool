package net.praqma.clearcase.exceptions;

public class NotMountedException extends ClearCaseException {

	public NotMountedException( String msg ) {
		super( msg );
	}
	
	public NotMountedException( String msg, Exception e ) {
		super( msg, e );
	}

}