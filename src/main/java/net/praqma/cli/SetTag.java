package net.praqma.cli;


import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.UCMException.UCMType;
import net.praqma.clearcase.ucm.entities.Tag;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;

public class SetTag
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
		Option otag     = new Option( "tag", "t", true, 1, "The tag. Given as: \"key1=val1&key2=val2\"" );
		Option otagtype = new Option( "tagtype", "y", true, 1, "The tag type" );
		Option otagid   = new Option( "tagid", "i", true, 1, "The tag id" );
		
		o.setOption( oentity );
		o.setOption( otag );
		o.setOption( otagtype );
		o.setOption( otagid );
		
		o.setDefaultOptions();
		
		o.setHeader( "Set a tag for an UCM entity" );
		o.setSyntax( "SetTag -e <entity> -t <tag> -y <tag type> -i <tag id>" );
		o.setDescription( "Examples:" + Options.linesep + "SetTag -e baseline:bls@\\somevob -T \"key1=val1&key2=val2\" -y myjob -i 10101" + Options.linesep + "SetTag -e baseline:bls@\\somevob -T \"key1=&key2=val2\" -y myjob -i 10101" + Options.linesep + "The last example will remove key1 from the tag" );
		
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
		
		UCM.setVerbose( o.verbose() );
		
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
		
		/* Split key value structure */
		String[] tags = otag.getString().split( "&" );
		
		for( String t : tags )
		{
			
			String[] entry = t.split( "=" );

			try
			{
				if( o.verbose() )
				{
					System.out.print( "+(" + entry[0] + ", " + entry[1] + ") " );
				}
				
				tag.SetEntry( entry[0].trim(), entry[1].trim() );
			}
			catch( ArrayIndexOutOfBoundsException ea )
			{
				if( o.verbose() )
				{
					System.out.print( "-(" + entry[0] + ") " );
				}
				tag.RemoveEntry( entry[0] );
			}
		}
		
		if( o.verbose() )
		{
			System.out.println( "" );
		}
		
		try
		{
			tag.Persist();
		}
		catch( UCMException ex )
		{
			if( ex.type == UCMType.TAG_CREATION_FAILED )
			{
				System.err.println( "Could not persist the tag." );
				System.exit( 1 );
			}
		}
		
		if( tag.IsCreated() )
		{
			System.out.println( "Tag created." );
		}
		else
		{
			System.out.println( "Tag updated." );
		}
	}
		
}
