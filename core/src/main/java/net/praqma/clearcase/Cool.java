package net.praqma.clearcase;

import net.praqma.util.execute.CommandLine;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

public abstract class Cool {

	public static final String filesep = System.getProperty( "file.separator" );
	public static final String qfs = filesep.equals( "\\" ) ? "\\\\" : filesep;
	public static final String linesep = System.getProperty( "line.separator" );
	public static final String delim = "::";

	public static OperatingSystem getOS() {
		return CommandLine.getInstance().getOS();
	}
}
