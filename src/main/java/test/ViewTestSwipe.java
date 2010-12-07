package test;

import java.io.File;

import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;

public class ViewTestSwipe
{
	public static void main( String[] args )
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );

		File root = new File( "C:\\Temp\\views\\chw_Server_4_int" );
		
		if( args.length > 0 )
		{
			root = new File( args[0] );
			System.out.println( "Setting view root to " + root );
		}
		
		SnapshotView view = UCMView.GetSnapshotView( root );
		
		view.Swipe( false );
	}
}
