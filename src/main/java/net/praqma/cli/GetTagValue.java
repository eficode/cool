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
		try
		{
			run( args );
		}
		catch( UCMException e )
		{
			System.err.println( UCM.getMessagesAsString() );
			throw e;
		}
	}
	
	public static void run( String[] args ) throws UCMException
	{
		Options o = new Options();
		
		Option oentity  = new Option( "entity", "e", true, 1, "The UCM entity" );
		Option okey     = new Option( "key", "k", true, 1, "The tag key" );
		Option otagtype = new Option( "tagtype", "y", true, 1, "The tag type" );
		Option otagid   = new Option( "tagid", "i", true, 1, "The tag id" );
		
		o.setOption( oentity );
		o.setOption( okey );
		o.setOption( otagtype );
		o.setOption( otagid );

		o.setDefaultOptions();
		
		o.setHeader( "Get the value of a tag" );
		o.setSyntax( "GetTagValue -e <entity> -k <key> -y <tag type> -i <tag id>" );
		o.setDescription( "Examples:" + Options.linesep + "GetTagValue -e baseline:bls@\\somevob -k status -y myjob -i 10101" );
		
		o.parse( args );
		
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
			e = UCMEntity.GetEntity( oentity.getString(), false );
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
			System.out.println( "Unknown key, " + okey.getString() );
		}
		else
		{
			System.out.println( value );
		}
	}
		
}
