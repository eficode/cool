package test;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.entities.Baseline.BaselineDiff;
import net.praqma.clearcase.ucm.entities.Component.BaselineList;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.ucm.view.SnapshotView.COMP;

public class UpdateTest
{
	public static void main( String[] args )
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
		
		File root = new File( "C:\\Temp\\views\\chw_Server_11_dev_view\\" );
		
		if( args.length > 0 )
		{
			root = new File( args[0] );
			System.out.println( "Setting view root to " + root );
		}

		System.out.println( "Starting..." );
		
		SnapshotView view = UCMView.GetSnapshotView( root );
		view.Update( true, true, true, false, COMP.MODIFIABLE, null );
		
	}
}
