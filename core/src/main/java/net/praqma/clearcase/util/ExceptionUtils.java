package net.praqma.clearcase.util;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.praqma.clearcase.exceptions.ClearCaseException;

public abstract class ExceptionUtils {
	
	private static Logger logger = Logger.getLogger( ExceptionUtils.class.getName() );

	public static void print( Throwable e, PrintStream out, boolean stack ) {
		
		if( e instanceof ClearCaseException ) {
			((ClearCaseException)e).print( out );
		} else {
			out.println( e.getMessage() );
		}
		
		if( e.getCause() != null ) {
			print( e.getCause(), out, stack );
		} else {
			if( stack ) { 
				e.printStackTrace( out );
			} else {
				out.println( e.getMessage() );
			}
		}
	}
	
	public static void log( Throwable e, boolean stack ) {
		
		if( e instanceof ClearCaseException ) {
			((ClearCaseException)e).log();
		} else {
			logger.warning( e.getMessage() );
		}
		
		if( e.getCause() != null ) {
			log( e.getCause(), stack );
		} else {
			if( stack ) { 
				logger.log( Level.WARNING, "", e );
			} else {
				logger.warning( e.getMessage() );
			}
		}
	}
}
