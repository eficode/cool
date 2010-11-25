package test;

import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Tag;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.Component.BaselineList;
import net.praqma.clearcase.ucm.entities.UCMEntity.Plevel;



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
		BaselineList bls = co1.GetBaselines( st1, Plevel.INITIAL );

		System.out.println( "SIZE=" + bls.size() );
		
		for( Baseline b : bls )
		{
			System.out.println( b.toString() );
		}
		
		System.out.println( bls.get( 0 ).Stringify() );
		Baseline bl1 = bls.get( 0 );
		Tag t1 = bl1.CreateTag( "HUDSON", "007", "2010", "pending" );
		
		System.out.println( t1 );
	}
}