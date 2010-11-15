package net.praqma.clearcase.cleartool;

import net.praqma.utils.AbnormalProcessTerminationException;
import net.praqma.utils.Command;
import net.praqma.utils.CommandLineException;

import java.util.Arrays;
import java.util.List;



/**
 * The Cleartool proxy class
 * All calls to cleartool, should be done through these static functions.
 * run( String )  : returns the return value as String.
 * run_a( String ): returns the return value as an array of Strings, separated by new lines. 
 * @author wolfgang
 *
 */
public abstract class Cleartool extends Command
{	
	public static String run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
	{
		return Command.run( "cleartool " + cmd );
	}
	
	/**
	 * Executes a cleartool command.
	 * @param cmd
	 * @return The return value of the cleartool command as an array Strings, separated by new lines.
	 */
	public static List<String> run_a( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
	{
		logger.trace_function();
		
		/* Just call the run method an split the result */
		String result = run( cmd );
		List<String> rs = Arrays.asList( result.split( "\n" ) );
		return rs;
	}
	
	
	
	
	public static String run_qx( String cmd )
	{
		return "";
	}
}