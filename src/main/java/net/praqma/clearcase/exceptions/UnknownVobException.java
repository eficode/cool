package net.praqma.clearcase.exceptions;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.praqma.util.debug.Logger;

//public class UCMException extends RuntimeException
public class UnknownVobException extends CleartoolException {
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