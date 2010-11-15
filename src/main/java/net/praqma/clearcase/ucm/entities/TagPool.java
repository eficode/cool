package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;
import java.util.HashMap;

import net.praqma.clearcase.ucm.persistence.UCMContext;
import net.praqma.clearcase.ucm.persistence.UCMStrategyXML;
import net.praqma.utils.Debug;
import net.praqma.utils.Printer;

class TagPool extends UCM
{
	private static TagPool instance = new TagPool();
	
	protected static Debug logger = Debug.GetLogger();
	
	HashMap<String, Tag> pool = null;
	
	private TagPool()
	{
		pool = new HashMap<String, Tag>();
	}
	
	public static TagPool GetInstance()
	{
		return instance;
	}
	
	/**
	 * 
	 * @param tagType
	 * @param tagID
	 * @param timestamp
	 * @param buildStatus
	 * @param entity
	 * @return
	 */
	public Tag CreateTag( String tagType, String tagID, String timestamp, String buildStatus, UCMEntity entity )
	{
		return context.NewTag( tagType, tagID, entity, "timestamp=" + timestamp + "&buildstatus=" + buildStatus );
	}
	
	public boolean TagExists( String tagType, String tagID, UCMEntity entity )
	{
		ArrayList<Tag> tags = context.ListTags( entity );
		
		for( Tag t : tags )
		{
			t.Load();
			
			/* Is it the correct tag? Return it! */
			if( t.GetTagType().equals( t.GetTagType() ) && t.GetTagID().equals( tagID ) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	public Tag GetTag( String tagType, String tagID, UCMEntity entity )
	{
		logger.debug( entity.toString() );
		ArrayList<Tag> tags = context.ListTags( entity );
		
		for( Tag t : tags )
		{
			t.Load();
			
			/* Is it the correct tag? Return it! */
			if( t.GetTagType().equals( t.GetTagType() ) && t.GetTagID().equals( tagID ) )
			{
				t.SetTagEntity( entity );
				return t;
			}
		}
		
		logger.log( "Could not find the Tag with ID " + tagType + tagID + ". Creating new." );
		
		/* Create new Tag */
		Tag tag = context.NewTag( tagType, tagID, entity, "" );
		
		return tag;
	}
}
