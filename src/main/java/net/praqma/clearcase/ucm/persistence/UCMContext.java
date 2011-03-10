package net.praqma.clearcase.ucm.persistence;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.ucm.UCMException.UCMType;
import net.praqma.clearcase.ucm.entities.*;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.structure.Tuple;
import net.praqma.util.debug.PraqmaLogger;
import net.praqma.util.debug.PraqmaLogger.Logger;

public class UCMContext extends Cool
{
	private UCMStrategyInterface strategy;
	
	private final Pattern pattern_activity = Pattern.compile( "^>>\\s*(\\S+)\\s*.*$" );
	private final Pattern pattern_cc_name  = Pattern.compile( "[\\w\\.][\\w\\.-]*" );
	
	public UCMContext( UCMStrategyInterface strategy )
	{
		this.strategy = strategy;
	}
	
	/* COMMON */
	public String getMastership( UCMEntity entity ) throws UCMException
	{
		return strategy.getMastership( entity.GetFQName() );
	}
	
	/* Baseline specific */
	public ArrayList<Activity> GetBaselineDiff( SnapshotView view, Baseline baseline ) throws UCMException
	{
		return GetBaselineDiff( view, baseline, null, true );
	}
	
	public ArrayList<Activity> GetBaselineDiff( SnapshotView view, Baseline baseline, boolean nmerge ) throws UCMException
	{
		return GetBaselineDiff( view, baseline, null, nmerge );
	}
	
	public ArrayList<Activity> GetBaselineDiff( SnapshotView view, Baseline baseline, Baseline other, boolean nmerge ) throws UCMException
	{
		logger.log( view.GetViewtag() );
		
		/* Change if other than -pre */
		List<String> result = strategy.GetBaselineDiff( view.GetViewRoot(), baseline.GetFQName(), "", nmerge, baseline.GetPvob() );
		
		ArrayList<Activity> activities = new ArrayList<Activity>();
		
		int length = view.GetViewRoot().getAbsoluteFile().toString().length();
		//System.out.println( view.GetViewRoot().getAbsoluteFile().toString() );
		
		Activity current = null;
		for( String s : result )
		{
			/* Get activity */
			Matcher match = pattern_activity.matcher( s );
			
			/* This line is a new activity */
			if( match.find() )
			{
				current = UCMEntity.GetActivity( match.group( 1 ), true );
				
				/* A special case? */
				if( current.GetShortname().equals( "no_activity" ) )
				{
					logger.debug( "Recorded a special activity case" );
					current.SetSpecialCase( true );
				}
				activities.add( current );
				continue;
			}
			
			if( current == null )
			{
				logger.debug( "Not an activity: " + s );
				continue;
			}
			
			/* If not an activity, it must be a version */
			String f = s.trim();
			
			/* If the version cannot be instantiated and is a special case, skip it */
			try
			{
				Version v = (Version)UCMEntity.GetEntity( f );
				v.SetSFile( v.GetFile().substring( length ) );
				current.changeset.versions.add( v );
			}
			catch( UCMException e )
			{
				if( e.type == UCMType.ENTITY_ERROR && !current.isSpecialCase() )
				{
					throw e;
				}
			}
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
	
	public Version getVersionExtension( String file, File viewroot ) throws UCMException
	{
		String f = strategy.getVersionExtension( file, viewroot );
		return (Version)UCMEntity.GetEntity( file, false );
	}
	
	/* Tags */
	
	public ArrayList<Tag> ListTags( UCMEntity entity ) throws UCMException
	{
		ArrayList<Tag> tags = new ArrayList<Tag>();
		
		/* Load Tags from clearcase */
		List<String[]> result = strategy.GetTags( entity.GetFQName() );
		
		if( result.size() > 0 )
		{
			for( String[] s : result )
			{
				Tag tag = (Tag)UCMEntity.GetEntity( s[0].trim() );
				tag.SetKeyValue( s[1] );
				tags.add( tag );
			}
		}
		
		return tags;
	}
	
	public Tuple<String, String> GetTag( Tag tag ) throws UCMException
	{
		String result = strategy.GetTag( tag.GetFQName() );

		Tuple<String, String> tuple = new Tuple<String, String>( "oid", result );
		
		return tuple;
	}
	
	public Tag StoreTag( Tag tag ) throws UCMException
	{
		/* Make the new tag */
		Tag newtag = NewTag( tag.GetTagType(), tag.GetTagID(), tag.GetTagEntity(), Tag.HashToCGI( tag.GetEntries(), true ) );

		/* Delete the old tag */
		strategy.DeleteTag( tag.GetFQName() );
		
		return newtag;
	}
	
	public void DeleteTag( Tag tag ) throws UCMException
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
	public Tag NewTag( String tagType, String tagID, UCMEntity entity, String cgi ) throws UCMException
	{
		logger.debug( "ENTITY="+entity.toString() );
		logger.debug( "CGI FOR NEW = " + cgi );
		//System.out.println( "CGI==="+cgi );
		
		/* Delete any existing Tags with the unique ID */
		logger.log( "Deleting Tags with ID: " + tagType + tagID + " for entity " + entity.GetFQName() );
		strategy.DeleteTagsWithID( tagType, tagID, entity.GetFQName() );
		
		cgi = "tagtype=" + tagType + "&tagid=" + tagID + ( cgi.length() > 0 ? "&" + cgi : "" );
		String fqname = strategy.NewTag( entity, cgi );
		
		Tag tag = (Tag)UCMEntity.GetEntity( fqname );
		//tag.SetEntry( "tagtype", tagType );
		//tag.SetEntry( "tagid", tagID );
		tag.SetKeyValue( cgi );
		tag.SetTagEntity( entity );
		
		return tag;
	}
	
	public String[] LoadBaseline( Baseline baseline ) throws UCMException
	{
		logger.log( "Loading baseline " + baseline.GetFQName() );
		
		//String result = CTF.LoadBaseline( this.fqname );
		String result = strategy.LoadBaseline( baseline.GetFQName() );
		logger.debug( "RESULT=" + result );
		
		String[] rs = result.split( UCMEntity.delim );
		
		return rs;
	}
	
	public void createBaseline( String fqname, Component component, File view, boolean incremental, boolean identical ) throws UCMException
	{
		strategy.createBaseline( fqname, component.GetFQName(), view, incremental, identical );
	}
	
	
	public void SetPromotionLevel( Baseline baseline ) throws UCMException
	{
		strategy.SetPromotionLevel( baseline.GetFQName(), baseline.GetPromotionLevel( true ).toString() );
	}
	
	
	public ArrayList<Baseline> GetRecommendedBaselines( Stream stream ) throws UCMException
	{
		ArrayList<Baseline> bls = new ArrayList<Baseline>();
		
		String result = strategy.GetRecommendedBaselines( stream.GetFQName() );
		String[] rs = result.split( " " );
		
		for( int i = 0 ; i < rs.length ; i++ )
		{
			/* There is something in the element. */
			if( rs[i].matches( "\\S+" ) )
			{
				//bls.add( (Baseline)UCMEntity.GetEntity( rs[i], true ) );
				bls.add( UCMEntity.GetBaseline( rs[i] + "@" + stream.GetPvob(), true ) );
			}
		}
		
		return bls;
	}
	
	public boolean RecommendBaseline( Stream stream, Baseline baseline )
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
	
	
	public List<Baseline> GetBaselines( Stream stream, Component component, Project.Plevel plevel, String pvob ) throws UCMException
	{
		String pl = plevel == null ? "" : plevel.toString();
		logger.debug( "Getting baselines from " + stream.GetFQName() + " and " + component.GetFQName() + " with level " + plevel + " in VOB=" + pvob );
		List<String> bls_str = strategy.GetBaselines( component.GetFQName(), stream.GetFQName(), pl );
		
		logger.debug( "I got " + bls_str.size() + " baselines." );
		net.praqma.util.structure.Printer.listPrinter( bls_str );
		List<Baseline> bls = new ArrayList<Baseline>();
		
		for( String bl : bls_str )
		{
			bls.add( UCMEntity.GetBaseline( bl + "@" + pvob, true ) );
		}
		
		return bls;
	}
	
	public SnapshotView MakeSnapshotView( Stream stream, File viewroot, String viewtag ) throws UCMException
	{
		strategy.MakeSnapshotView( stream.GetFQName(), viewroot, viewtag );
		return UCMView.GetSnapshotView( viewroot );
	}
	
	public boolean ViewExists( String viewtag )
	{
		return strategy.ViewExists( viewtag );
	}
	
	public void RegenerateViewDotDat( File dir, String viewtag ) throws UCMException
	{
		strategy.RegenerateViewDotDat( dir, viewtag );
	}
	
	public Map SwipeView( File viewroot, boolean excludeRoot )
	{
		return strategy.SwipeView( viewroot, excludeRoot );
	}
	
	public Stream CreateStream( Stream pstream, String nstream, boolean readonly, Baseline baseline ) throws UCMException
	{
		strategy.CreateStream( pstream.GetFQName(), nstream, readonly, ( baseline != null ? baseline.GetFQName() : "" ) );
		
		Stream stream = UCMEntity.GetStream( nstream );
		
		return stream;
	}
	
	public void Genereate( Stream stream )
	{
		strategy.Generate( stream.GetFQName() );
	}
	
	public void RebaseStream( SnapshotView view, Stream stream, Baseline baseline, boolean complete )
	{
		if( strategy.RebaseStream( view.GetViewtag(), stream.GetFQName(), baseline.GetFQName(), complete ) )
		{
			logger.debug( "Rebasing complete" );
		}
		else
		{
			logger.debug( "No rebasing needed" );
		}
	}
	
	public boolean IsRebaseInProgress( Stream stream )
	{
		return strategy.IsRebaseInProgress( stream.GetFQName() );
	}
	
	public void CancelRebase( Stream stream )
	{
		strategy.CancelRebase( stream.GetFQName() );
	}
	
	public boolean StreamExists( String fqname )
	{
		return strategy.StreamExists( fqname );
	}
	
	public List<Baseline> GetLatestBaselines( Stream stream ) throws UCMException
	{
		logger.debug( "STREAM: " + stream.GetFQName() );
		
		List<String> bs = strategy.GetLatestBaselines( stream.GetFQName() );
		List<Baseline> bls = new ArrayList<Baseline>();
		
		for( String s : bs )
		{
			bls.add( UCMEntity.GetBaseline( s.trim() ) );
		}
		
		return bls;
	}
	
	
//	public Stream GetStreamFromView( String viewtag )
//	{
//		logger.debug( "Getting stream from " + viewtag );
//		
//		String stream = strategy.GetStreamFromView( viewtag );
//		return UCMEntity.GetStream( stream );
//	}
	
	public Tuple<Stream, String> GetStreamFromView( File viewroot ) throws UCMException
	{
		File wvroot = strategy.GetCurrentViewRoot( viewroot );
		
		String viewtag = strategy.ViewrootIsValid( wvroot );
		
		String streamstr = strategy.GetStreamFromView( viewtag );
		Stream stream    = UCMEntity.GetStream( streamstr );
	
		return new Tuple<Stream, String>( stream, viewtag );
	}
	
	
	public void LoadProject( Project project ) throws UCMException
	{
		String result = strategy.LoadProject( project.GetFQName() );
		
		logger.debug( "Result: " + result );
	
		/* TODO currently the result only returns the stream name. Add more? */
		project.SetStream( UCMEntity.GetStream( result ) );
	}
	
	
	private static final Pattern pattern_cache = Pattern.compile( "^\\s*log has been written to\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );
	
	/**
	 * Returns the update log on completion
	 * @param view
	 * @param overwrite
	 * @param loadrules
	 * @return
	 */
	public String UpdateView( SnapshotView view, boolean overwrite, String loadrules )
	{
		String result = strategy.ViewUpdate( view.GetViewRoot(), overwrite, loadrules );
		Matcher match = pattern_cache.matcher( result );
		if( match.find() )
		{
			return match.group( 1 );
		}
		
		return "";
	}
	
	public String GetRootDir( Component component )
	{
		logger.debug( component.GetFQName() );
		
		return strategy.GetRootDir( component.GetFQName() );
	}
	
	public String ViewrootIsValid( File view ) throws UCMException
	{
		return strategy.ViewrootIsValid( view );
	}
	
	public Project GetProjectFromStream( Stream stream ) throws UCMException
	{
		return UCMEntity.GetProject( strategy.GetProjectFromStream( stream.GetFQName() ) + "@" + stream.GetPvob() );
	}
	
	public List<Component> GetModifiableComponents( Project project ) throws UCMException
	{
		List<String> cs = strategy.GetModifiableComponents( project.GetFQName() );
		List<Component> comps = new ArrayList<Component>();
		
		for( String c : cs )
		{
			comps.add( UCMEntity.GetComponent( c + "@" + project.GetPvob() ) );
		}
		
		return comps;
	}
	
	public void LoadActivity( Activity activity ) throws UCMException
	{
		String result = "";
		
		/* The special case branch */
		if( activity.isSpecialCase() )
		{
			result = "System";
		}
		else
		{
			result = strategy.LoadActivity( activity.GetFQName() );
		}
		activity.SetUser( result );
	}
	
	public String LoadComponent( Component component ) throws UCMException
	{
		return strategy.LoadComponent( component.GetFQName() );
	}
	
	public String LoadStream( Stream stream ) throws UCMException
	{
		logger.log( "Loading stream " + stream.GetFQName() );

		return strategy.LoadStream( stream.GetFQName() );
	}
	
	
	
	/* Attributes */

	/**
	 * Retrieve the attributes for an entity, executed from the current working directory
	 * @param entity The given entity
	 * @return A Map of key, value pairs of the attributes
	 * @throws UCMException
	 */
	public Map<String, String> getAttributes( UCMEntity entity ) throws UCMException
	{
		return strategy.getAttributes( entity.GetFQName() );
	}
	
	/**
	 * Retrieve the attributes for an entity
	 * @param entity The given entity
	 * @param dir A File object of the directory where the command should be executed
	 * @return A Map of key, value pairs of the attributes
	 * @throws UCMException
	 */
	public Map<String, String> getAttributes( UCMEntity entity, File dir ) throws UCMException
	{
		return strategy.getAttributes( entity.GetFQName(), dir );
	}
	
	public void setAttribute( UCMEntity entity, String attribute, String value ) throws UCMException
	{
		strategy.setAttribute( entity.GetFQName(), attribute, value );
	}
	
	
	public List<HyperLink> getHlinks( UCMEntity entity, String hlinkType, File dir ) throws UCMException
	{
		logger.debug( "Entity: " + entity.GetFQName() );
		
		List<Tuple<String, String>> result = strategy.getHlinks( entity.GetFQName(), hlinkType, dir );
		
		List<HyperLink> hlinks = new ArrayList<HyperLink>();
		
		for( Tuple<String, String> t : result )
		{
			HyperLink h = HyperLink.getHyperLink( t.t1, t.t2 );
			
			hlinks.add( h );
		}
		
		return hlinks;
	}
	
	
	public HyperLink loadHyperLink( HyperLink hlink, File dir ) throws UCMException
	{
		String result = strategy.LoadHyperLink( hlink.GetFQName(), dir );
		
		return null;
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





