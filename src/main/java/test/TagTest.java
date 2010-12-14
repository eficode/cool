package test;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Tag;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.utils.BaselineList;



public class TagTest
{

	/**
	 * @param args
	 */
	public static void main( String[] args )
	{
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
		
		System.out.println( "Starting..." );
		
		String comp = "component:_Server@\\Cool_PVOB";
		String stre = "stream:Server_int@\\Cool_PVOB";
		
		Stream st1 = UCMEntity.GetStream( stre );
		Component co1 = UCMEntity.GetComponent( comp );
		BaselineList bls = co1.GetBaselines( st1, Project.Plevel.INITIAL );

		System.out.println( "SIZE=" + bls.size() );
		
		for( Baseline b : bls )
		{
			System.out.println( b.toString() );
		}
		
		System.out.println( bls.get( 0 ).Stringify() );
		Baseline bl1 = bls.get( 0 );
		
		Tag t = null;
		try
		{
			t = bl1.GetTag( "hudson", "001" );
		}
		catch( UCMException e )
		{
			System.out.println( "UCMException: " + e.type );
			return;
		}
		System.out.println( t.Stringify() );
		
		t.SetEntry( "status", "pending" );
		
		t.Persist();
		
		Tag t2 = bl1.GetTag( "hudson", "001" );
		System.out.println( t2.Stringify() );
		
		t2.SetEntry( "status", "building" );
		
		t2.Persist();
		
		Tag t3 = bl1.GetTag( "hudson", "001" );
		System.out.println( t3.Stringify() );
		
		t3.SetEntry( "status", "failed" );
		
		t3.Persist();
		
		Tag t4 = bl1.GetTag( "hudson", "001" );
		System.out.println( t4.Stringify() );
	}
}



