package net.praqma.clearcase.cleartool;

import java.io.File;

import net.praqma.clearcase.*;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLine;
import net.praqma.util.execute.CommandLineException;
import net.praqma.util.execute.CommandLineInterface;

/**
 * The Cleartool proxy class All calls to cleartool, should be done through
 * these static functions. run( String ) : returns the return value as String.
 * run_a( String ): returns the return value as an array of Strings, separated
 * by new lines.
 * 
 * @author wolfgang
 * 
 */
public abstract class Cleartool extends Cool {

	private static CommandLineInterface cli = null;
	private static Logger logger = Logger.getLogger();

	static {
		cli = CommandLine.getInstance();
	}

	public static CmdResult run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException {
		return cli.run( "cleartool " + cmd, null, true, false );
	}

	public static CmdResult run( String cmd, File dir ) throws CommandLineException, AbnormalProcessTerminationException {
		return cli.run( "cleartool " + cmd, dir, true, false );
	}

	public static CmdResult run( String cmd, File dir, boolean merge ) throws CommandLineException, AbnormalProcessTerminationException {
		return cli.run( "cleartool " + cmd, dir, merge, false );
	}
	
	public static CmdResult run( String cmd, File dir, boolean merge, boolean ignore ) throws CommandLineException, AbnormalProcessTerminationException {
		return cli.run( "cleartool " + cmd, dir, merge, ignore );
	}
}
