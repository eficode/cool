package net.praqma.clearcase.exceptions;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.praqma.util.debug.Logger;

//public class UCMException extends RuntimeException
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