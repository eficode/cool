package test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.SnapshotView.UpdateInfo;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.ucm.view.SnapshotView.COMP;
import net.praqma.util.debug.Logger;

public class UpdateTest
{
//	private static net.praqma.util.debug.Logger loggerj = Logger.getLogger( "test.UpdateTest" );
//	
//	static
//	{
//		System.out.println( "Setting configuration" );
//		//UCM.class.getClassLoader().getResourceAsStream( "log4j.conf" );
//		
//
//		Properties props = new Properties();
//		try
//		{
//			props.load( UCM.class.getClassLoader().getResourceAsStream( "log4j.xml" ) );
//		}
//		catch ( IOException e )
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		PropertyConfigurator.configure( props );
//		//PropertyConfigurator.configure( "/log4j.conf" );
//	}
	
//	public static void main( String[] args )
//	{
//		UCM.SetContext( UCM.ContextType.CLEARTOOL );
//		
//		loggerj.debug( "YEAH" );
//		
//		//File root = new File( "C:\\Temp\\views\\chw_Server_11_dev_view\\" );
//		File root = new File( "C:\\Documents and Settings\\chw\\.hudson\\jobs\\test10\\workspace\\view" );
//
//		
//		if( args.length > 0 )
//		{
//			root = new File( args[0] );
//			System.out.println( "Setting view root to " + root );
//		}
//
//		System.out.println( "Starting..." );
//		
//		try
//		{
//			SnapshotView view = UCMView.GetSnapshotView( root );
//			UpdateInfo info = view.Update( true, true, true, false, COMP.ALL, null );
//	
//			
//			
//			System.out.println( "The update operations succeeded: " + info.success );
//			
//			System.out.println( "Total files to delete: " + info.totalFilesToBeDeleted );
//			System.out.println( "Files deleted: " + info.filesDeleted );
//			System.out.println( "Dirs deleted: " + info.dirsDeleted );
//		}
//		catch( UCMException e )
//		{
//			
//		}
//		
//	}
}
