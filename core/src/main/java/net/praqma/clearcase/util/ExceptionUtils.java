package net.praqma.clearcase.util;

import java.io.PrintStream;

import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;

public abstract class ExceptionUtils {
	
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static Logger logger = Logger.getLogger();

	public static void print( Throwable e, PrintStream out, boolean stack ) {
		tracer.entering(ExceptionUtils.class.getSimpleName(), "print", new Object[]{e, out, stack});
		tracer.finest("Checking if Throwable is a ClearCaseException.");
		
		if( e instanceof ClearCaseException ) {
			tracer.finest("Throwable is a ClearCaseException.");
			((ClearCaseException)e).print( out );
		} else {
			tracer.finest("Throwable is not a ClearCaseException.");
			out.println( e.getMessage() );
		}
		
		tracer.finest("Checking if Throwable has a cause.");
		if( e.getCause() != null ) {
			tracer.finest("Throwable has a cause.");
			print( e.getCause(), out, stack );
		} else {
			tracer.finest("Throwable has no cause.");
			tracer.finest("Checking if stack flag is set.");
			if( stack ) { 
				tracer.finest("stack flag is set.");
				e.printStackTrace( out );
			} else {
				tracer.finest("stack flag is not set.");
				out.println( e.getMessage() );
			}
		}
		tracer.exiting(ExceptionUtils.class.getSimpleName(), "print");
	}
	
	public static void log( Throwable e, boolean stack ) {
		tracer.entering(ExceptionUtils.class.getSimpleName(), "log", new Object[]{e, stack});
		tracer.finest("Checking if Throwable is a ClearCaseException.");
		
		if( e instanceof ClearCaseException ) {
			tracer.finest("Throwable is a ClearCaseException.");
			((ClearCaseException)e).log();
		} else {
			tracer.finest("Throwable is not a ClearCaseException.");
			logger.warning( e.getMessage() );
		}
		tracer.finest("Checking if Throwable has a cause.");
		if( e.getCause() != null ) {
			tracer.finest("Throwable has a cause.");
			log( e.getCause(), stack );
		} else {
			tracer.finest("Throwable has no cause.");
			tracer.finest("Checking if stack flag is set.");
			if( stack ) {
				tracer.finest("stack flag is set.");
				logger.warning( e );
			} else {
				tracer.finest("stack flag is not set.");
				logger.warning( e.getMessage() );
			}
		}
		tracer.exiting(ExceptionUtils.class.getSimpleName(), "log");
	}
}
