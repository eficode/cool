package net.praqma.utils;


import java.io.File;
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
	

	
	public static CmdResult run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
	{
		return run( cmd, null );
	}
	
	public static CmdResult run( String cmd, File dir ) throws CommandLineException, AbnormalProcessTerminationException
	{
		logger.trace_function();
		
		logger.debug( "$ " + cmd );
		
		String[] cmds = new String[3];
		cmds[0] = "cmd.exe";
		cmds[1] = "/C";
		cmds[2] = cmd;
		//cmds[3] = "2>&1";
		
		try
		{
			ProcessBuilder pb = new ProcessBuilder( cmds );
			//Process p = Runtime.getRuntime().exec( cmd );
			//Process p = Runtime.getRuntime().exec( cmd, null, dir );
			
			if( dir != null )
			{
				logger.debug( "Changing current working directory to " + dir );
				pb.directory( dir );
			}
			
			CmdResult result = new CmdResult();
			logger.debug( "1" );
			Process p = pb.start();
			logger.debug( "2" );	
			StreamGobbler output = new StreamGobbler( p.getInputStream() );
			logger.debug( "3" );
			StreamGobbler errors = new StreamGobbler( p.getErrorStream() );
			logger.debug( "4" );
			//System.out.println( "Running output" );
			output.run();
			logger.debug( "5" );
			//System.out.println( "Running errors" );
			errors.run();
			logger.debug( "6" );
			int exitValue = p.waitFor();
			logger.debug( "7" );
			/* Abnormal process termination, with error out as message */
			if ( exitValue != 0 )
			{
				logger.debug( "Abnormal process termination: " + errors.sres.toString() );
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