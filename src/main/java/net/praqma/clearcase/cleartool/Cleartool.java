package net.praqma.clearcase.cleartool;

import net.praqma.utils.AbnormalProcessTerminationException;
import net.praqma.utils.CmdResult;
import net.praqma.utils.Command;
import net.praqma.utils.CommandLineException;

import java.io.File;



/**
 * The Cleartool proxy class
 * All calls to cleartool, should be done through these static functions.
 * run( String )  : returns the return value as String.
 * run_a( String ): returns the return value as an array of Strings, separated by new lines. 
 * @author wolfgang
 *
 */
public abstract class Cleartool
{	
	public static CmdResult run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
	{
		return Command.run( cmd, null, false );
	}
	
	public static CmdResult run( String cmd, File dir ) throws CommandLineException, AbnormalProcessTerminationException
	{
		return Command.run( "cleartool " + cmd, dir, false );
	}
	
	public static CmdResult run( String cmd, File dir, boolean merge ) throws CommandLineException, AbnormalProcessTerminationException
	{
		return Command.run( "cleartool " + cmd, dir, merge );
	}
}