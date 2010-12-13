package test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
import net.praqma.utils.Command;

public class UpdateTest2
{
	public static void main( String[] args )
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
		
		//File root = new File( "C:\\Temp\\views\\chw_Server_11_dev_view\\" );
		File root = new File( "C:\\Temp\\views2\\test1" );

		
		if( args.length > 0 )
		{
			root = new File( args[0] );
			System.out.println( "Setting view root to " + root );
		}

		System.out.println( "Starting..." );
		
		//SnapshotView view = UCMView.GetSnapshotView( root );
		String cmd = "cleartool update -force  -overwrite  -add_loadrules  Cool\\Model Cool\\Trace Cool\\ServerTest Cool\\Gui";
		//String[] cmd = { "cleartool", "update", "-force", "-overwrite", "-add_loadrules", "Cool\\Model", "Cool\\Trace", "Cool\\ServerTest", "Cool\\Gui" };
		
		String s = Command.run( cmd, root, true ).stdoutBuffer.toString();
		System.out.println( s );
		
		/*
		PipedOutputStream output = new PipedOutputStream();
		PumpStreamHandler psh = new PumpStreamHandler(output);
		DataInputStream is = null;
		
		CommandLine cl = CommandLine.parse( cmd );
		DefaultExecutor executor = new DefaultExecutor();
		executor.setExitValue( 0 );
		executor.setWorkingDirectory( root );
		int exitValue = 0;
		try
		{
			System.out.println( "1" );
			is = new DataInputStream( new PipedInputStream( output ) );
			System.out.println( "2" );
			executor.setStreamHandler(psh);
			System.out.println( "3" );
			exitValue = executor.execute(cl);
			System.out.println( "4" );
		}
		catch ( ExecuteException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println( "VALUE=" + exitValue );*/
		
/*		BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
		
		String line = "";
		try
		{
			while( ( line = br.readLine() ) != null )
			{
				System.out.println( "LINE="+line );
				
			}
		}
		catch ( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
}
