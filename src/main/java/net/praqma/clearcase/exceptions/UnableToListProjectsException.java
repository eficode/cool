package net.praqma.clearcase.exceptions;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.praqma.util.debug.Logger;

//public class UCMException extends RuntimeException
public class UnableToListProjectsException extends CleartoolException {
	/**
	 * 
	 */
	public UnableToListProjectsException() {
		super();
	}

	public UnableToListProjectsException( String s ) {
		super( s );
	}
	
	public UnableToListProjectsException( Exception e ) {
		super( e );
	}
	
	public UnableToListProjectsException( String s, Exception e ) {
		super( s, e );
	}

	
}