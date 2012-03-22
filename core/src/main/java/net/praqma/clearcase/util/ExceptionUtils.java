package net.praqma.clearcase.util;

import java.io.PrintStream;

import net.praqma.clearcase.exceptions.ClearCaseException;

public abstract class ExceptionUtils {

	public static void print( Throwable e, PrintStream out, boolean stack ) {
		
		if( e instanceof ClearCaseException ) {
			((ClearCaseException)e).print( out );
		}
		
		if( e.getCause() != null ) {
			out.println( e.getMessage() );
			print( e.getCause(), out, stack );
		} else {
			if( stack ) { 
				e.printStackTrace( out );
			} else {
				out.println( e.getMessage() );
			}
		}
	}
}
