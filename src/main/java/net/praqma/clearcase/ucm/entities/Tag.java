package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.ucm.entities.UCMEntity.ClearcaseEntityType;
import net.praqma.clearcase.ucm.utils.TagQuery;
import net.praqma.utils.Printer;
import net.praqma.utils.Tuple;

public class Tag extends UCMEntity
{
	/* Tag specific fields */
	private String tagType = "";
	private String tagID   = "";
	private String OID     = "";
	
	private UCMEntity entity = null;
	
	//private static final Pattern pattern_cgi = Pattern.compile( "" );
	
	private HashMap<String, String> keyval = new HashMap<String, String>();
	
	Tag()
	{
	}
	
	/**
	 * This method is only available to the package, because only ClearcaseEntity should
	 * be allowed to call it.
	 * @return A new Tag Entity
	 */
	static Tag GetEntity()
	{
		return new Tag();
	}
	
	public void PostProcess()
	{
		this.OID = this.shortname;
	}
	
	public static HashMap<String, String> CGIToHash( String cgi )
	{
		HashMap<String, String> hash = new HashMap<String, String>();
		logger.debug( "cgi="+ cgi );
		String[] entries = cgi.split( "&" );
		for( String e : entries )
		{
			logger.debug( "entry="+ e );
			String[] entry = e.split( "=" );
			hash.put( entry[0].trim(), entry[1].trim() );
		}
		
		return hash;
	}
	
	public static String HashToCGI( HashMap<String, String> keyval )
	{
		return HashToCGI( keyval, false );
	}
	
	//public boolean QueryTag( ArrayList<Tuple<String, String>> query )
	public boolean QueryTag( TagQuery query )
	{
		for( Tuple<String, String> t : query )
		{
			if( this.keyval.containsKey( t.t1 ) )
			{
				logger.debug( "(" + t.t1 + ", " + t.t2 + ") = " + this.keyval.get( t.t1 ) );
				
				Pattern pattern = Pattern.compile( t.t2 );
				Matcher match   = pattern.matcher( this.keyval.get( t.t1 ) );
				
				//if( !this.keyval.get( t.t1 ).matches( t.t2 ) )
				if( !match.find() )
				{
					return false;
				}
			}
			else
			{
				logger.debug( "(" + t.t1 + ", " + t.t2 + ") = " );
				
				/* Handling non-existing keys, if the condition can't fall back to an empty string, return false */
				if( !"".matches( t.t2 ) )
				{	
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static String HashToCGI( HashMap<String, String> keyval, boolean skiptaginfo )
	{
		StringBuffer sb = new StringBuffer();
		Iterator<Entry<String, String>> it = keyval.entrySet().iterator();
	    while( it.hasNext() )
	    {
	    	Map.Entry<String, String> entry = (Map.Entry<String, String>)it.next();
	    	if( skiptaginfo && ( entry.getKey().equalsIgnoreCase( "tagid" ) || entry.getKey().equalsIgnoreCase( "tagtype" ) ) ) continue;
	    	sb.append( entry.getKey() + "=" + entry.getValue() + "&" );
	    }
	    
	    return sb.toString();
	}
	
	public void Load()
	{
		Tuple<String, String> t = context.GetTag( this );
		//this.OID = t.t1;
		
		//System.out.println( "CGI=" + t.t2 );
		
		keyval = Tag.CGIToHash( t.t2 );
		this.tagType = keyval.get( "tagtype" );
		this.tagID   = keyval.get( "tagid" );
		
		this.loaded = true;
	}
	
	public void SetKeyValue( String cgi )
	{
		keyval = Tag.CGIToHash( cgi );
		this.tagType = keyval.get( "tagtype" );
		this.tagID   = keyval.get( "tagid" );
		
		this.loaded = true;
	}
	
	
	public void SetEntry( String key, String value )
	{
		logger.log( "Setting " + key + " = " + value );
		keyval.put( key, value );
	}
	
	public String GetEntry( String key )
	{
		if( keyval.containsKey( key ) )
		{
			return keyval.get( key );
		}
		
		return null;
	}
	
	public HashMap<String, String> GetEntries()
	{
		return this.keyval;
	}
	
	private void Set( String newFqname )
	{
		Matcher match = pattern_tag_fqname.matcher( newFqname );
		if( match.find() )
		{
			/* Set the Entity variables */
			shortname = match.group( 1 ); // This is also the oid
			OID       = match.group( 1 );
			pvob      = match.group( 2 );
			fqname    = newFqname;
		}
		else
		{
			logger.warning( "The new fully qualified name was not correct!" );
		}
	}
	
	public void SetTagEntity( UCMEntity entity )
	{
		this.entity = entity;
	}
	
	public UCMEntity GetTagEntity( )
	{
		return this.entity;
	}
	
	public Tag Persist()
	{
		//Set( context.StoreTag( this ) );
		Printer.HashMapPrinter( this.GetEntries() );
		return context.StoreTag( this );
	}
	
	

	
	public String GetTagType()
	{
		return this.tagType;
	}
	
	public String GetTagID()
	{
		return this.tagID;
	}
	
	public String Stringify()
	{
		if( !this.loaded ) Load();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append( super.Stringify() );
		
		sb.append( "Tag ID   : " + this.tagID + linesep );
		sb.append( "Tag Type : " + this.tagType + linesep );
		sb.append( "Tag OID  : " + this.OID + linesep );
		
		sb.append( "Tag Ent  : " + this.GetTagEntity().toString() + linesep );
		
		sb.append( "Entries  : " + keyval.size() + " " );
		Iterator<Entry<String, String>> it = keyval.entrySet().iterator();
	    while( it.hasNext() )
	    {
	    	Map.Entry<String, String> entry = (Map.Entry<String, String>)it.next();
	    	sb.append( "(" + entry.getKey() + " = " + entry.getValue() + ")" );
	    	if( it.hasNext() ) sb.append( ", " );
	    }
		
		return sb.toString();
	}
}
