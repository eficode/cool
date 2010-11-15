package net.praqma.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import net.praqma.utils.Debug;

/**
 * UNTESTED cmd class
 * @author wolfgang
 *
 */
public abstract class Command
{
	protected static Debug logger = Debug.GetLogger();
	
	public static String run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
	{
		logger.trace_function();
		
		logger.debug( "$ " + cmd );
		
		try
		{
			Process p = Runtime.getRuntime().exec( cmd );
			p.waitFor();
			BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
			
			br.close();
			
			/* Abnormal process termination */
			if ( p.exitValue() != 0 )
			{
				logger.log( "Abnormal process termination" );
				throw new AbnormalProcessTerminationException();
			}
			
			return br.toString();
		}
		catch ( Exception e )
		{
			logger.warning( "Could not execute the command \"" + cmd + "\" correctly: " + e.getMessage() );
			throw new CommandLineException( "Could not execute the command \"" + cmd + "\" correctly" );
		}
	}
}