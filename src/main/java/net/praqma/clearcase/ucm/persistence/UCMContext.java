package net.praqma.clearcase.ucm.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.ucm.entities.*;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.UCMEntity.Plevel;
import net.praqma.utils.Debug;
import net.praqma.utils.Printer;
import net.praqma.utils.Tuple;

public class UCMContext
{
	private Debug logger = Debug.GetLogger();
	private UCMStrategyInterface strategy;
	
	private final Pattern pattern_activity = Pattern.compile( "^>>\\s*(\\S+)$" );
	
	public UCMContext( UCMStrategyInterface strategy )
	{
		this.strategy = strategy;
	}
	
	/* Baseline specific */
	public ArrayList<Activity> GetBaselineDiff( Baseline baseline )
	{
		return GetBaselineDiff( baseline, null, true );
	}
	
	public ArrayList<Activity> GetBaselineDiff( Baseline baseline, boolean nmerge )
	{
		return GetBaselineDiff( baseline, null, nmerge );
	}
	
	public ArrayList<Activity> GetBaselineDiff( Baseline baseline, Baseline other, boolean nmerge )
	{
		/* Change if other than -pre */
		String result = strategy.GetBaselineDiff( baseline.GetFQName(), "", nmerge );
		
		
		//ArrayList<Version> list = new ArrayList<Version>();
		ArrayList<Activity> activities = new ArrayList<Activity>();
		

//		result = result.replaceAll( "(?m)^>>.*$", "" );
//		result = result.replaceAll( "(?m)\\@\\@.*$", "" );
//		result = result.replaceAll( "(?m)^\\s+", "" );

		String[] rs = result.split( "\n" );
		Activity current = null;
		for( String s : rs )
		{
//			System.out.println( "s="+s );
			/* Get activity */
			Matcher match = pattern_activity.matcher( s );
			
			/* This line is a new activity */
			if( match.find() )
			{
				current = (Activity)UCMEntity.GetEntity( match.group( 1 ) );
				activities.add( current );
				continue;
			}
			
			if( current == null )
			{
				logger.warning( "Whoops, the result does not start with an activity" );
				continue;
			}
			
			/* If not an activity, it must be a version */
			current.changeset.versions.add( (Version)UCMEntity.GetEntity( s.trim() ) );
		}

		return activities;
	}
	
	public ArrayList<Activity> GetActivities( Baseline baseline )
	{
		return null;
	}
	
	/* Version */
	
	public HashMap<String, String> GetVersion( Version version )
	{
		String result = strategy.GetVersion( version.GetFQName(), "::" );
		String[] rs = result.split( "::" );
		
		HashMap<String, String> v = new HashMap<String, String>();
		
		v.put( "date", rs[0] );
		v.put( "user", rs[1] );
		v.put( "machine", rs[2] );
		v.put( "comment", rs[3] );
		v.put( "checkedout", rs[4] );
		v.put( "kind", rs[5] );
		v.put( "branch", rs[6] );
		
		return v;
	}
	
	/* Tags */
	
	public ArrayList<Tag> ListTags( UCMEntity entity )
	{
		ArrayList<Tag> tags = new ArrayList<Tag>();
		
		/* Load Tag from clearcase */
		String result = strategy.GetTags( entity.GetFQName() ).trim();
		String[] rs = result.split( "\n" );
		
		//if( rs.length > 0 )
		if( result.length() > 0 )
		{
			for( String s : rs )
			{
				tags.add( (Tag)UCMEntity.GetEntity( s.trim() ) );
			}
		}
		
		return tags;
	}
	
	public Tuple<String, String> GetTag( Tag tag )
	{
		String result = strategy.GetTag( tag.GetFQName() );
		//System.out.println( result );
		//HashMap<String, String> entries = Tag.CGIToHash( s );
		Tuple<String, String> tuple = new Tuple<String, String>( "oid", result );
		
		return tuple;
	}
	
	public Tag StoreTag( Tag tag )
	{
		/* Make the new tag */
		Tag newtag = NewTag( tag.GetTagType(), tag.GetTagID(), tag.GetTagEntity(), Tag.HashToCGI( tag.GetEntries(), true ) );

		/* Delete the old tag */
		strategy.DeleteTag( tag.GetFQName() );
		
		return newtag;
	}
	
	public void DeleteTag( Tag tag )
	{
		strategy.DeleteTag( tag.GetFQName() );
	}
	
	/**
	 * This function creates a new Tag entity and automatically persists it!!!.
	 * The tagType and tagID constitutes the unique id.
	 * The cgi string SHOULD NOT contain tagType or tagID.  
	 * @param tagType The tag type.
	 * @param tagID
	 * @param entity The "owner" entity of the tag.
	 * @param cgi
	 * @return
	 */
	public Tag NewTag( String tagType, String tagID, UCMEntity entity, String cgi )
	{
		logger.debug( "ENTITY="+entity.toString() );
		logger.debug( "CGI FOR NEW = " + cgi );
		//System.out.println( "CGI==="+cgi );
		
		cgi = "tagtype=" + tagType + "&tagid=" + tagID + ( cgi.length() > 0 ? "&" + cgi : "" );
		String fqname = strategy.NewTag( entity, cgi );
		
		Tag tag = (Tag)UCMEntity.GetEntity( fqname );
		//tag.SetEntry( "tagtype", tagType );
		//tag.SetEntry( "tagid", tagID );
		tag.SetKeyValue( cgi );
		tag.SetTagEntity( entity );
		
		return tag;
	}
	
	public String[] LoadBaseline( Baseline baseline )
	{
		logger.log( "Loading baseline " + baseline.GetFQName() );
		
		//String result = CTF.LoadBaseline( this.fqname );
		String result = strategy.LoadBaseline( baseline.GetFQName() );
		
		String[] rs = result.split( UCMEntity.delim );
		
		return rs;
	}
	
	
	public void SetPromotionLevel( Baseline baseline )
	{
		strategy.SetPromotionLevel( baseline.GetFQName(), baseline.GetPromotionLevel( true ).toString() );
	}
	
	
	public ArrayList<Baseline> GetRecommendedBaselines( Stream stream )
	{
		ArrayList<Baseline> bls = new ArrayList<Baseline>();
		
		String result = strategy.GetRecommendedBaselines( stream.GetFQName() );
		String[] rs = result.split( " " );
		
		for( int i = 0 ; i < rs.length ; i++ )
		{
			/* There is something in the element. */
			if( rs[i].matches( "\\S+" ) )
			{
				bls.add( (Baseline)UCMEntity.GetEntity( rs[i], true ) );
			}
		}
		
		return bls;
	}
	
	public boolean RecommnedBaseline( Stream stream, Baseline baseline )
	{
		try
		{
			//Cleartool.run( cmd );
			strategy.RecommendBaseline( stream.GetFQName(), baseline.GetFQName() );
			return true;
		}
		catch( UCMException e )
		{
			logger.error( "Stream " + stream.GetShortname() + " could not recommend baseline " + baseline.GetShortname() );
			return false;
		}
	}
	
	
	public List<Baseline> GetBaselines( Stream stream, Component component, Plevel plevel )
	{
		String pl = plevel == null ? "" : plevel.toString() ;
		List<String> bls_str = strategy.GetBaselines( component.GetFQName(), stream.GetFQName(), pl );
		List<Baseline> bls = new ArrayList<Baseline>();
		for( String bl : bls_str )
		{
			bls.add( UCMEntity.GetBaseline( bl, true ) );
		}
		
		return bls;
	}
	
	public void MakeSnapshotView( Stream stream, String viewroot, String viewtag )
	{
		strategy.MakeSnapshotView( stream.GetFQName(), viewtag, viewroot );
	}
	
	
	public String GetXML()
	{
		return strategy.GetXML();
	}
	
	public void SaveState()
	{
		strategy.SaveState();
	}
}





