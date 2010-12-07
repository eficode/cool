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

public class CITest
{
	public static void main( String[] args )
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
		
		String comp = "component:_System@\\Cool_PVOB";
		String stre = "stream:chw_" + Calendar.getInstance().getTime() + "@\\Cool_PVOB";
		String intstre = "stream:Server_int@\\Cool_PVOB";
		File root = new File( "C:\\Temp\\views\\" );
		String tag = "tag";
		
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
			tag = args[3];
			System.out.println( "Setting view tag to " + tag );
		}
		
		root = new File( root, tag );
		root.mkdir();
		
		Stream integrationStream = UCMEntity.GetStream( intstre );
		
		//Stream developmentStream = UCMEntity.GetStream( stre );
		Component co1 = UCMEntity.GetComponent( comp );
		BaselineList bls = co1.GetBaselines( integrationStream, Project.Plevel.INITIAL );
		
		List<Baseline> rec_bls = integrationStream.GetRecommendedBaselines();
		
		Baseline bl = rec_bls.get( 0 );
		
		Stream developmentStream = Stream.Create( integrationStream, stre, false, bl );
		SnapshotView view = SnapshotView.Create( developmentStream, root, tag );
		
		developmentStream.Rebase( view, bl, true );
		
	}
}
