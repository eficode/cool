package net.praqma.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
	
	public static List<String> run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
	{
		logger.trace_function();
		
		logger.debug( "$ " + cmd );
		
		try
		{
			Process p = Runtime.getRuntime().exec( cmd );
			p.waitFor();
			BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
			
			String line;
			List<String> list = new ArrayList<String>();
			while( ( line = br.readLine() ) != null )
			{
				list.add( line );
	        }

			
			br.close();
			
			/* Abnormal process termination */
			if ( p.exitValue() != 0 )
			{
				logger.log( "Abnormal process termination" );
				throw new AbnormalProcessTerminationException();
			}
			
			//System.out.println( "=>" + sb.toString() + "<=" );
			net.praqma.utils.Printer.ListPrinter( list );
			
			return list;
		}
		catch ( Exception e )
		{
			logger.warning( "Could not execute the command \"" + cmd + "\" correctly: " + e.getMessage() );
			throw new CommandLineException( "Could not execute the command \"" + cmd + "\" correctly" );
		}
	}
}