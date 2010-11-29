package net.praqma.utils;


import java.io.IOException;
import java.util.List;

import net.praqma.utils.Debug;

/**
 * UNTESTED cmd class
 * @author wolfgang
 *
 */
public abstract class Command
{
	protected static Debug logger = Debug.GetLogger();
	protected static final String linesep = System.getProperty( "line.separator" );
	
	public static List<String> run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
	{
		logger.trace_function();
		
		logger.debug( "$ " + cmd );
		
		String[] cmds = new String[3];
		cmds[0] = "cmd.exe";
		cmds[1] = "/C";
		cmds[2] = cmd;
		
		try
		{
			Process p = Runtime.getRuntime().exec( cmd );
			
			//StreamGobbler errors = new StreamGobbler( p.getErrorStream() );
			StreamGobbler output = new StreamGobbler( p.getInputStream() );

			//System.out.println( "Running errors" );
			//errors.run();
			System.out.println( "Running output" );
			output.run();			
			
			int exitValue = p.waitFor();
			
			/* Abnormal process termination */
			if ( exitValue != 0 )
			{
				logger.log( "Abnormal process termination: " + output.sres.toString() );
				throw new AbnormalProcessTerminationException();
			}
			
			p.getErrorStream().close();
			p.getOutputStream().close();
			p.getInputStream().close();
			
			//net.praqma.utils.Printer.ListPrinter( output.lres );
			
			return output.lres;
		}
		catch ( IOException e )
		{
			logger.warning( "Could not execute the command \"" + cmd + "\" correctly: " + e.getMessage() );
			throw new CommandLineException( "Could not execute the command \"" + cmd + "\" correctly: " + e.getMessage() );
		}
		catch ( InterruptedException e )
		{
			logger.warning( "The command \"" + cmd + "\" was interrupted: " + e.getMessage() );
			throw new CommandLineException( "The command \"" + cmd + "\" was interrupted: " + e.getMessage() );
		}
	}
}