package net.praqma.utils;


import java.io.IOException;
import java.util.ArrayList;
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
	

	
	//public static List<String> run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
	public static CmdResult run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
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
			
			CmdResult result = new CmdResult();
			
			StreamGobbler output = new StreamGobbler( p.getInputStream() );
			StreamGobbler errors = new StreamGobbler( p.getErrorStream() );

			//System.out.println( "Running output" );
			output.run();
			//System.out.println( "Running errors" );
			errors.run();
			
			int exitValue = p.waitFor();
			
			/* Abnormal process termination, with error out as message */
			if ( exitValue != 0 )
			{
				logger.error( "Abnormal process termination: " + errors.sres.toString() );
				throw new AbnormalProcessTerminationException( errors.sres.toString() );
			}
			
			p.getErrorStream().close();
			p.getOutputStream().close();
			p.getInputStream().close();
			
			/* Setting command result */
			result.stdoutBuffer = output.sres;
			result.stdoutList   = output.lres;
			
			result.errorBuffer  = errors.sres;
			result.errorList    = errors.lres;
			
			//net.praqma.utils.Printer.ListPrinter( output.lres );
			
			//return output.lres;
			return result;
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