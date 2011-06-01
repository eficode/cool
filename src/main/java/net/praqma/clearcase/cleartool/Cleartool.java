package net.praqma.clearcase.cleartool;


import java.io.File;

import net.praqma.clearcase.Cool;
import net.praqma.util.debug.PraqmaLogger.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLine;
import net.praqma.util.execute.CommandLineException;
import net.praqma.util.execute.CommandLineInterface;



/**
 * The Cleartool proxy class
 * All calls to cleartool, should be done through these static functions.
 * run( String )  : returns the return value as String.
 * run_a( String ): returns the return value as an array of Strings, separated by new lines. 
 * @author wolfgang
 *
 */
public abstract class Cleartool extends Cool
{

	private static CommandLineInterface cli = null;
	
	static
	{
		logger.debug( "Cleartool environment: " + System.getProperty( "cleartool" ) );
		if( System.getProperty( "cleartool" ) != null && System.getProperty( "cleartool" ).equalsIgnoreCase( "mock" ) )
		{
			cli = CommandLineMock.getInstance();
			//System.out.println( "Setting cli to MOCK" );
			//logger.debug( "Setting cli to MOCK" );
		}
		else
		{
			cli = CommandLine.getInstance();
			//System.out.println( "Setting cli to THE REAL THING" );
		}
	}
	
	public static void setCLILogger( Logger logger )
	{
		cli.setLogger( logger );
	}
	
	public static CmdResult run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
	{
		return cli.run( "cleartool " + cmd, null, true );
	}
	
	public static CmdResult run( String cmd, File dir ) throws CommandLineException, AbnormalProcessTerminationException
	{
		return cli.run( "cleartool " + cmd, dir, true );
	}
	
	public static CmdResult run( String cmd, File dir, boolean merge ) throws CommandLineException, AbnormalProcessTerminationException
	{
		return cli.run( "cleartool " + cmd, dir, merge );
	}
}


