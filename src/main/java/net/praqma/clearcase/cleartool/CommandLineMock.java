package net.praqma.clearcase.cleartool;

import java.io.File;

import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLine;
import net.praqma.util.execute.CommandLineException;
import net.praqma.util.execute.CommandLineInterface;

public class CommandLineMock implements CommandLineInterface
{
	private CommandLineMock()
	{
		
	}
	
	private static CommandLineMock instance = new CommandLineMock();

	public static CommandLineMock getInstance()
	{
		return instance;
	}

	@Override
	public CmdResult run( String cmd ) throws CommandLineException, AbnormalProcessTerminationException
	{
		return run( cmd, null, false, false );
	}

	@Override
	public CmdResult run( String cmd, File dir ) throws CommandLineException, AbnormalProcessTerminationException
	{
		return run( cmd, dir, false, false );
	}

	@Override
	public CmdResult run( String cmd, File dir, boolean merge ) throws CommandLineException, AbnormalProcessTerminationException
	{
		return run( cmd, dir, merge, false );
	}

	@Override
	public CmdResult run( String cmd, File dir, boolean merge, boolean ignore ) throws CommandLineException, AbnormalProcessTerminationException
	{
		/* This is the final  */
		
		System.out.println( "$ " + cmd + ", " + dir + ", " + merge + ", " + ignore );
		
		CmdResult res = new CmdResult();
		res.stdoutBuffer = new StringBuffer();
		
		/* Load() a Baseline 
		 * 
		 * cleartool desc -fmt %n::%[component]p::%[bl_stream]p::%[plevel]p::%u baseline:CHW_BASELINE_51@\Cool_PVOB
		 * CHW_BASELINE_51::_System::Server_int::TESTED::chw
		 * 
		 * */
		if( cmd.equalsIgnoreCase( "cleartool desc -fmt %n::%[component]p::%[bl_stream]p::%[plevel]p::%u cleartool desc -fmt %n::%[component]p::%[bl_stream]p::%[plevel]p::%u baseline:CHW_BASELINE_51@\\Cool_PVOB" ) )
		{
			res.stdoutBuffer.append( "CHW_BASELINE_51::_System::Server_int::TESTED::chw" );
		}
		
		return res;
	}

}
