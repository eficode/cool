package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;
import java.util.HashMap;

import net.praqma.clearcase.ucm.entities.Cool;

import net.praqma.clearcase.ucm.UCMException;

class TagPool extends UCM
{
	private static TagPool instance = new TagPool();
	
	HashMap<String, Tag> pool = null;
	
	private TagPool()
	{
		pool = new HashMap<String, Tag>();
	}
	
	public static TagPool GetInstance()
	{
		return instance;
	}
	

	
	public boolean TagExists( String tagType, String tagID, UCMEntity entity ) throws UCMException
	{
		ArrayList<Tag> tags = context.ListTags( entity );
		
		for( Tag t : tags )
		{			
			/* Is it the correct tag? Return it! */
			if( t.GetTagType().equals( t.GetTagType() ) && t.GetTagID().equals( tagID ) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public Tag GetTag( String tagType, String tagID, UCMEntity entity ) throws UCMException
	{
		Cool.logger.debug( entity.toString() );
		ArrayList<Tag> tags = context.ListTags( entity );
		
		for( Tag t : tags )
		{
			
			/* Is it the correct tag? Return it! */
			if( t.GetTagType().equals( tagType ) && t.GetTagID().equals( tagID ) )
			{
				t.SetTagEntity( entity );
				return t;
			}
		}
		
		Cool.logger.log( "Could not find the Tag with ID " + tagType + tagID + ". Creating new." );
		
		/* Create new Tag */
		//Tag tag = context.NewTag( tagType, tagID, entity, "" );
		
		Tag tag = (Tag)UCMEntity.GetEntity( "tag@0@" + entity.GetPvob() );
		//tag.SetEntry( "tagtype", tagType );
		//tag.SetEntry( "tagid", tagID );
		String cgi = "tagtype=" + tagType + "&tagid=" + tagID;
		tag.SetKeyValue( cgi );
		tag.SetTagEntity( entity );
		
		tag.SetCreated( true );
		
		return tag;
	}
}
