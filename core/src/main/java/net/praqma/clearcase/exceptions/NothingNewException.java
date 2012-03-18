package net.praqma.clearcase.exceptions;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.praqma.util.debug.Logger;

//public class UCMException extends RuntimeException
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