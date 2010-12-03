package test;

import java.io.File;
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

public class RebaseTest
{
	public static void main( String[] args )
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
		
		String comp = "component:_System@\\Cool_PVOB";
		String stre = "stream:chw_Server_10_dev@\\Cool_PVOB";
		String intstre = "stream:Server_int@\\Cool_PVOB";
		File root = new File( "C:\\Temp\\views\\chw_Server_10_dev_view" );
		
		if( args.length > 0 )
		{
			comp = args[0];
			System.out.println( "Setting component to " + comp );
		}
		
		if( args.length > 1 )
		{
			stre = args[1];
			System.out.println( "Setting stream to " + stre );
		}
		
		if( args.length > 2 )
		{
			intstre = args[1];
			System.out.println( "Setting integration stream to " + intstre );
		}
		
		if( args.length > 3 )
		{
			root = new File( args[3] );
			System.out.println( "Setting view root to " + root );
		}
		
		
		
		Stream st1 = UCMEntity.GetStream( intstre );
		Stream st2 = UCMEntity.GetStream( stre );
		Component co1 = UCMEntity.GetComponent( comp );
		BaselineList bls = co1.GetBaselines( st1, Project.Plevel.INITIAL );
		
		SnapshotView view = UCMView.GetSnapshotView( root );
		
		List<Baseline> rec_bls = st1.GetRecommendedBaselines();
		
		st2.Rebase( view, rec_bls.get( 0 ), true );		
		
	}
}
