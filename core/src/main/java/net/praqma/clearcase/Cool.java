package net.praqma.clearcase;

import net.praqma.logging.Config;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

public abstract class Cool {

	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	public static final String filesep = System.getProperty( "file.separator" );
	public static final String qfs = filesep.equals( "\\" ) ? "\\\\" : filesep;
	public static final String qfsor = "[\\\\/]";
	public static final String linesep = System.getProperty( "line.separator" );
	public static final String delim = "::";
	
	public static OperatingSystem getOS() {
		tracer.entering(Cool.class.getSimpleName(), "getOS");
		//return CommandLine.getInstance().getOS();
		OperatingSystem result = OperatingSystem.WINDOWS;
		tracer.exiting(Cool.class.getSimpleName(), "getOS", result);
		return result;
		
	}
}
