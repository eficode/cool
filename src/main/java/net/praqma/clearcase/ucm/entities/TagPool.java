package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;
import java.util.HashMap;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.persistence.UCMContext;
import net.praqma.clearcase.ucm.persistence.UCMStrategyXML;
import net.praqma.util.Printer;

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
		logger.debug( entity.toString() );
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
		
		logger.log( "Could not find the Tag with ID " + tagType + tagID + ". Creating new." );
		
		/* Create new Tag */
		Tag tag = context.NewTag( tagType, tagID, entity, "" );
		tag.SetCreated( true );
		
		return tag;
	}
}
