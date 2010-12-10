package test;

import java.io.File;

import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.ucm.view.SnapshotView.COMP;

public class UpdateTest
{
	public static void main( String[] args )
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
		
		//File root = new File( "C:\\Temp\\views\\chw_Server_11_dev_view\\" );
		File root = new File( "C:\\Documents and Settings\\chw\\.hudson\\jobs\\test10\\workspace\\view" );

		
		if( args.length > 0 )
		{
			root = new File( args[0] );
			System.out.println( "Setting view root to " + root );
		}

		System.out.println( "Starting..." );
		
		SnapshotView view = UCMView.GetSnapshotView( root );
		view.Update( true, true, true, false, COMP.ALL, null );
		
	}
}
