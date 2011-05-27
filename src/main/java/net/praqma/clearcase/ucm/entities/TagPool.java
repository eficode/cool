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
	

	
	public boolean tagExists( String tagType, String tagID, UCMEntity entity ) throws UCMException
	{
		ArrayList<Tag> tags = context.getTags( entity );
		
		for( Tag t : tags )
		{			
			/* Is it the correct tag? Return it! */
			if( t.getTagType().equals( t.getTagType() ) && t.getTagID().equals( tagID ) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public Tag getTag( String tagType, String tagID, UCMEntity entity ) throws UCMException
	{
		Cool.logger.debug( entity.toString() );
		ArrayList<Tag> tags = context.getTags( entity );
		
		for( Tag t : tags )
		{
			
			/* Is it the correct tag? Return it! */
			if( t.getTagType().equals( tagType ) && t.getTagID().equals( tagID ) )
			{
				t.setTagEntity( entity );
				return t;
			}
		}
		
		Cool.logger.log( "Could not find the Tag with ID " + tagType + tagID + ". Creating new." );
		
		/* Create new Tag */
		//Tag tag = context.NewTag( tagType, tagID, entity, "" );
		
		Tag tag = (Tag)UCMEntity.getEntity( "tag@0@" + entity.getPvob() );
		//tag.SetEntry( "tagtype", tagType );
		//tag.SetEntry( "tagid", tagID );
		String cgi = "tagtype=" + tagType + "&tagid=" + tagID;
		tag.setKeyValue( cgi );
		tag.setTagEntity( entity );
		
		tag.setCreated( true );
		
		return tag;
	}
}
