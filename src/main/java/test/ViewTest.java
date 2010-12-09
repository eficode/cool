package test;

import java.io.File;

import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Baseline.BaselineDiff;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;

public class ViewTest
{
	public static void main( String[] args )
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
		
		String comp = "component:_System@\\Cool_PVOB";
		String stre = "stream:Server_int@\\Cool_PVOB";
		File root = new File( "C:\\Temp\\views\\chw_Server_10_dev_view" );
		
		Stream st1 = UCMEntity.GetStream( stre );
		Component co1 = UCMEntity.GetComponent( comp );
		BaselineList bls = co1.GetBaselines( st1, Project.Plevel.INITIAL );
		
		System.out.println( "SIZE=" + bls.size() );
		
		for( Baseline b : bls )
		{
			System.out.println( b.toString() );
		}
		
		Baseline bl1 = bls.get( 0 );
		
		SnapshotView view = UCMView.GetSnapshotView( root );
		
		BaselineDiff bdiffs = bl1.GetDiffs( view );
		
		for( Activity a : bdiffs )
		{
			System.out.println( "ACT USER=" + a.GetUser() );
			
			for( Version v : a.changeset.versions )
			{
				System.out.println( "FILE=" + v.GetSFile() + ". USER=" + v.Blame() );
			}
		}
	}
}
