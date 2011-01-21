package net.praqma.cli;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Tag;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class GetTagValue
{
	public static void main( String[] args ) throws UCMException
	{
		Options o = new Options();
		
		Option oentity  = new Option( "entity", "e", true, 1, "The UCM entity" );
		Option okey     = new Option( "key", "k", true, 1, "The tag key" );
		Option otagtype = new Option( "tagtype", "y", true, 1, "The tag type" );
		Option otagid   = new Option( "tagid", "i", true, 1, "The tag id" );
		Option ohelp    = new Option( "help", "h", false, 0, "The help" );
		
		o.setOption( oentity );
		o.setOption( okey );
		o.setOption( otagtype );
		o.setOption( otagid );
		o.setOption( ohelp );
		
		o.parse( args );
		
		if( ohelp.used )
		{
			o.display();
			System.exit( 0 );
		}
		
		try
		{
			o.checkOptions();
		}
		catch ( Exception e )
		{
			System.err.println( "Incorrect option: " + e.getMessage() );
			o.display();
			System.exit( 1 );
		}
		
		/* Do the ClearCase thing... */
		UCM.SetContext( UCM.ContextType.CLEARTOOL );
		
		UCMEntity e = null;
		
		try
		{
			e = UCMEntity.GetEntity( oentity.getString() );
		}
		catch( UCMException ex )
		{
			System.err.println( ex.getMessage() );
			System.exit( 1 );
		}
		
		Tag tag = e.GetTag( otagtype.getString(), otagid.getString() );
		
		String value = tag.GetEntry( okey.getString() );
		
		if( value == null )
		{
			System.out.println( "" );
		}
		else
		{
			System.out.println( value );
		}
	}
		
}
