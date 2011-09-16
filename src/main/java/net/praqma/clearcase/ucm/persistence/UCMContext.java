package net.praqma.clearcase.ucm.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.*;

import net.praqma.clearcase.changeset.ChangeSet;
import net.praqma.clearcase.changeset.ChangeSet2;
import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.clearcase.ucm.UCMException.UCMType;
import net.praqma.clearcase.ucm.entities.*;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.HyperLink;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Tag;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;
import net.praqma.util.structure.Tuple;

public class UCMContext extends Cool {
	private UCMStrategyInterface strategy;
	
	transient private static Logger logger = Logger.getLogger();

	private final Pattern pattern_activity = Pattern.compile( "^>>\\s*(\\S+)\\s*.*$" );
	private final Pattern pattern_cc_name = Pattern.compile( "[\\w\\.][\\w\\.-]*" );

	public UCMContext( UCMStrategyInterface strategy ) {
		this.strategy = strategy;
	}

	/* COMMON */
	public String getMastership( UCMEntity entity ) throws UCMException {
		return strategy.getMastership( entity.getFullyQualifiedName() );
	}
	
	public void changeOwnership( String fqname, String username, File viewContext ) throws UCMException {
		strategy.changeOwnership( fqname, username, viewContext );
	}
	
	public void changeOwnership( UCMEntity entity, String username, File viewContext ) throws UCMException {
		strategy.changeOwnership( entity, username, viewContext );
	}

	/* Baseline specific */
	public List<Activity> getBaselineDiff( SnapshotView view, Baseline baseline ) throws UCMException {
		return getBaselineDiff( view, baseline, null, true );
	}

	public List<Activity> getBaselineDiff( SnapshotView view, Baseline baseline, boolean nmerge ) throws UCMException {
		return getBaselineDiff( view, baseline, null, nmerge );
	}

	public List<Activity> getBaselineDiff( SnapshotView view, Baseline baseline, Baseline other, boolean nmerge ) throws UCMException {
		logger.log( view.getViewtag() );

		/* Change if other than -pre */
		List<String> result = strategy.getBaselineDiff( view.getViewRoot(), baseline.getFullyQualifiedName(), "", nmerge, baseline.getPvobString() );

		int length = view.getViewRoot().getAbsoluteFile().toString().length();
		
		return parseActivityStrings( result, length );
	}
	
	public List<Activity> getBaselineDiff( Diffable d1, Diffable d2, boolean merge, File viewContext ) throws UCMException {
		List<String> lines = strategy.getBaselineDiff( d1, d2, merge, viewContext );
		//System.out.println("LINES:");
		//for( String l : lines ) {
		//	System.out.println(l);
		//}
		return parseActivityStrings( lines, viewContext.getAbsoluteFile().toString().length() );
	}
	
	public List<Activity> parseActivityStrings( List<String> result, int length ) throws UCMException {
		ArrayList<Activity> activities = new ArrayList<Activity>();
		Activity current = null;
		//System.out.println("PARSING:");
		for( String s : result ) {
			/* Get activity */
			Matcher match = pattern_activity.matcher( s );

			/* This line is a new activity */
			if( match.find() ) {
				current = UCMEntity.getActivity( match.group( 1 ), true );

				/* A special case? */
				if( current.getShortname().equals( "no_activity" ) ) {
					logger.debug( "Recorded a special activity case" );
					current.setSpecialCase( true );
				}
				activities.add( current );
				continue;
			}

			if( current == null ) {
				logger.debug( "Not an activity: " + s );
				continue;
			}

			/* If not an activity, it must be a version */
			String f = s.trim();

			/*
			 * If the version cannot be instantiated and is a special case, skip
			 * it
			 */
			try {
				Version v = (Version) UCMEntity.getEntity( f );
				v.setSFile( v.getFileAsString().substring( length ) );
				//System.out.println(f);
				current.changeset.versions.add( v );
			} catch (UCMException e) {

				//System.out.println("Whoops: " + f + ": " + e.getMessage());
				
				if( e.type == UCMType.ENTITY_ERROR && !current.isSpecialCase() ) {
					/* Let's try to move on */
					continue;
				}
			}
		}

		return activities;
	}
	
	public List<Version> baselineDifferences( Baseline bl1, Baseline bl2, boolean nomerge, SnapshotView view ) throws UCMException {
		return strategy.baselineDifferences( bl1, bl2, nomerge, view );
	}

	public ArrayList<Activity> getActivities( Baseline baseline ) {
		return null;
	}

	/* Version */
	
	public void loadVersion( Version version ) throws UCMException {
		strategy.loadVersion( version );
	}

	public HashMap<String, String> getVersion( Version version ) {
		String result = strategy.getVersion( version.getFullyQualifiedName(), "::" );
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
	
	public void removeVersion( File file, File viewContext ) throws UCMException {
		strategy.removeVersion( file, viewContext );
	}

	public Version getVersionExtension( File file, File viewroot ) throws UCMException {
		String f = strategy.getVersionExtension( file, viewroot );
		return (Version) UCMEntity.getEntity( f, false );
	}
	
	public void addToSourceControl( File file, boolean mkdir, File view ) throws UCMException {
		strategy.addToSourceControl( file, mkdir, view );
	}
	
	public void checkOut( Version version, File viewContext ) throws UCMException {
		strategy.checkOut( version.getFile(), viewContext );
	}
	
	public void checkIn( Version version, boolean identical, File viewContext ) throws UCMException {
		strategy.checkIn( version.getFile(), identical, viewContext );
	}
	
	public void checkIn( File file, boolean identical, File viewContext ) throws UCMException {
		strategy.checkIn( file, identical, viewContext );
	}

	public void checkOut( File file, File viewContext ) throws UCMException {
		strategy.checkOut( file, viewContext );
	}
		
	public void uncheckout( File file, boolean keep, File viewContext ) throws UCMException {
		strategy.uncheckout( file, keep, viewContext );
	}
	
	public void removeName( File file, File viewContext ) throws UCMException {
		strategy.removeName( file, viewContext );
	}
	
	public void moveFile( File file, File destination, File viewContext ) throws UCMException {
		strategy.moveFile( file, destination, viewContext );
	}
	
	public List<File> getUnchecedInFiles( File viewContext ) throws UCMException {
		return strategy.getUnchecedInFiles( viewContext );
	}

	/* Tags */

	public ArrayList<Tag> getTags( UCMEntity entity ) throws UCMException {
		ArrayList<Tag> tags = new ArrayList<Tag>();

		/* Load Tags from clearcase */
		List<String[]> result = strategy.getTags( entity.getFullyQualifiedName() );

		if( result.size() > 0 ) {
			for( String[] s : result ) {
				Tag tag = (Tag) UCMEntity.getEntity( s[0].trim() );
				tag.setKeyValue( s[1] );
				tags.add( tag );
			}
		}

		return tags;
	}

	public Tuple<String, String> getTag( Tag tag ) throws UCMException {
		String result = strategy.getTag( tag.getFullyQualifiedName() );

		Tuple<String, String> tuple = new Tuple<String, String>( "oid", result );

		return tuple;
	}

	public Tag storeTag( Tag tag ) throws UCMException {
		/* Make the new tag */
		Tag newtag = newTag( tag.getTagType(), tag.getTagID(), tag.getTagEntity(), Tag.mapToCGI( tag.GetEntries(), true ) );

		/* Delete the old tag */
		strategy.deleteTag( tag.getFullyQualifiedName() );

		return newtag;
	}

	public void deleteTag( Tag tag ) throws UCMException {
		strategy.deleteTag( tag.getFullyQualifiedName() );
	}

	/**
	 * This function creates a new Tag entity and automatically persists it!!!.
	 * The tagType and tagID constitutes the unique id. The cgi string SHOULD
	 * NOT contain tagType or tagID.
	 * 
	 * @param tagType
	 *            The tag type.
	 * @param tagID
	 * @param entity
	 *            The "owner" entity of the tag.
	 * @param cgi
	 * @return Tag
	 */
	public Tag newTag( String tagType, String tagID, UCMEntity entity, String cgi ) throws UCMException {
		logger.debug( "ENTITY=" + entity.toString() );
		logger.debug( "CGI FOR NEW = " + cgi );
		// System.out.println( "CGI==="+cgi );

		/* Delete any existing Tags with the unique ID */
		logger.log( "Deleting Tags with ID: " + tagType + tagID + " for entity " + entity.getFullyQualifiedName() );
		strategy.deleteTagsWithID( tagType, tagID, entity.getFullyQualifiedName() );

		cgi = "tagtype=" + tagType + "&tagid=" + tagID + ( cgi.length() > 0 ? "&" + cgi : "" );
		String fqname = strategy.newTag( entity, cgi );

		Tag tag = (Tag) UCMEntity.getEntity( fqname );
		// tag.SetEntry( "tagtype", tagType );
		// tag.SetEntry( "tagid", tagID );
		tag.setKeyValue( cgi );
		tag.setTagEntity( entity );

		return tag;
	}

	public String[] loadBaseline( Baseline baseline ) throws UCMException {
		logger.log( "Loading baseline " + baseline.getFullyQualifiedName() );

		// String result = CTF.LoadBaseline( this.fqname );
		String result = strategy.loadBaseline( baseline.getFullyQualifiedName() );
		logger.debug( "RESULT=" + result );

		String[] rs = result.split( UCMEntity.delim );

		return rs;
	}

	public void createBaseline( String fqname, Component component, File view, boolean incremental, boolean identical, Activity[] activities, Component[] depends ) throws UCMException {
		strategy.createBaseline( fqname, component, view, incremental, identical, activities, depends );
	}

	public void setPromotionLevel( Baseline baseline ) throws UCMException {
		strategy.setPromotionLevel( baseline.getFullyQualifiedName(), baseline.getPromotionLevel( true ).toString() );
	}

	public ArrayList<Baseline> getRecommendedBaselines( Stream stream ) throws UCMException {
		ArrayList<Baseline> bls = new ArrayList<Baseline>();

		String result = strategy.getRecommendedBaselines( stream.getFullyQualifiedName() );
		String[] rs = result.split( " " );

		for( int i = 0; i < rs.length; i++ ) {
			/* There is something in the element. */
			if( rs[i].matches( "\\S+" ) ) {
				// bls.add( (Baseline)UCMEntity.GetEntity( rs[i], true ) );
				bls.add( UCMEntity.getBaseline( rs[i] + "@" + stream.getPvobString(), true ) );
			}
		}

		return bls;
	}

	public void recommendBaseline( Stream stream, Baseline baseline ) throws UCMException {
		strategy.recommendBaseline( stream.getFullyQualifiedName(), baseline.getFullyQualifiedName() );
	}

	public List<Baseline> getBaselines( Stream stream, Component component, Project.Plevel plevel, String pvob ) throws UCMException {
		logger.debug( "Getting baselines from " + stream.getFullyQualifiedName() + " and " + component.getFullyQualifiedName() + " with level " + plevel + " in VOB=" + pvob );
		List<String> bls_str = strategy.getBaselines( component.getFullyQualifiedName(), stream.getFullyQualifiedName(), plevel );

		logger.debug( "I got " + bls_str.size() + " baselines." );
		List<Baseline> bls = new ArrayList<Baseline>();

		for( String bl : bls_str ) {
			bls.add( UCMEntity.getBaseline( bl + "@" + pvob, true ) );
		}

		return bls;
	}

	// private static final Pattern rx_deliver_find_baseline = Pattern.compile(
	// "Baselines to be delivered:\\s*baseline:", Pattern.MULTILINE );
	// private static final String rx_deliver_find_baseline =
	// "(?mi).*baselines to be delivered:\\s*baseline:.*";
	// private static final String rx_deliver_find_nobaseline =
	// "(?mi).*baselines to be delivered:\\s*baseline:.*";
	private static final Pattern rx_deliver_find_baseline = Pattern.compile( "Baselines to be delivered:\\s*baseline:", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE );
	private static final Pattern rx_deliver_find_nobaseline = Pattern.compile( "Baselines to be delivered:\\s*baseline:", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE );

	public boolean deliver( Baseline baseline, Stream stream, Stream target, File context, String viewtag, boolean force, boolean complete, boolean abort ) throws UCMException {
		logger.debug( "Delivering " + baseline + ", " + stream + ", " + target + ", " + context + ", " + viewtag );
		String bl = baseline != null ? baseline.getFullyQualifiedName() : null;
		String st = stream != null ? stream.getFullyQualifiedName() : null;
		String ta = target != null ? target.getFullyQualifiedName() : null;
		// strategy.deliver( baseline.GetFQName(), stream.GetFQName(),
		// target.GetFQName(), view.GetViewRoot(), view.GetViewtag(), force,
		// complete, abort );
		String result = strategy.deliver( bl, st, ta, context, viewtag, force, complete, abort );

		// System.out.println( "I GOT: \n\"" + result + "\"\n" );

		/* Test for baseline == true */
		if( baseline != null ) {
			Matcher m = rx_deliver_find_baseline.matcher( result );
			if( !m.find() ) {
				return false;
			}
		}

		if( baseline == null ) {
			Matcher m = rx_deliver_find_nobaseline.matcher( result );
			if( !m.find() ) {
				return false;
			}
		}

		return true;
	}

	public void cancelDeliver( File viewcontext, Stream stream ) throws UCMException {
		strategy.cancelDeliver( viewcontext, stream );
	}

	private static final Pattern rx_deliver_status = Pattern.compile( "" );

	public boolean isDelivering( Stream stream ) throws UCMException {
		String r = strategy.deliverStatus( stream.getFullyQualifiedName() ).trim();

		if( r.startsWith( "No deliver operation in progress on stream" ) ) {
			return false;
		}

		return true;
	}

	public SnapshotView makeSnapshotView( Stream stream, File viewroot, String viewtag ) throws UCMException {
		strategy.makeSnapshotView( stream.getFullyQualifiedName(), viewroot, viewtag );
		return UCMView.getSnapshotView( viewroot );
	}
	
	public void removeView( UCMView view ) throws UCMException {
		strategy.removeView( view );
	}

	public boolean viewExists( String viewtag ) {
		return strategy.viewExists( viewtag );
	}

	public void regenerateViewDotDat( File dir, String viewtag ) throws UCMException {
		strategy.regenerateViewDotDat( dir, viewtag );
	}

	public Map<String, Integer> swipeView( File viewroot, boolean excludeRoot ) {
		return strategy.swipeView( viewroot, excludeRoot );
	}

	public Stream createStream( Stream pstream, String nstream, boolean readonly, Baseline baseline ) throws UCMException {
		//strategy.createStream( pstream.getFullyQualifiedName(), nstream, readonly, ( baseline != null ? baseline.getFullyQualifiedName() : "" ) );
		strategy.createStream( pstream.getFullyQualifiedName(), nstream, readonly, ( baseline != null ? baseline.getFullyQualifiedName() : null ) );

		Stream stream = UCMEntity.getStream( nstream );

		return stream;
	}
	
	public void createIntegrationStream( String name, Project project, Baseline baseline ) throws UCMException {
		strategy.createIntegrationStream( name, project, baseline );
	}

	public void genereate( Stream stream ) {
		strategy.generate( stream.getFullyQualifiedName() );
	}

	public void rebaseStream( SnapshotView view, Stream stream, Baseline baseline, boolean complete ) {
		if( strategy.rebaseStream( view.getViewtag(), stream.getFullyQualifiedName(), baseline.getFullyQualifiedName(), complete ) ) {
			logger.debug( "Rebasing complete" );
		} else {
			logger.debug( "No rebasing needed" );
		}
	}

	public boolean isRebasing( Stream stream ) {
		return strategy.isRebasing( stream.getFullyQualifiedName() );
	}

	public void cancelRebase( Stream stream ) {
		strategy.cancelRebase( stream.getFullyQualifiedName() );
	}

	public boolean streamExists( String fqname ) {
		return strategy.streamExists( fqname );
	}

	public List<Baseline> getLatestBaselines( Stream stream ) throws UCMException {
		logger.debug( "STREAM: " + stream.getFullyQualifiedName() );

		List<String> bs = strategy.getLatestBaselines( stream.getFullyQualifiedName() );
		List<Baseline> bls = new ArrayList<Baseline>();

		for( String s : bs ) {
			bls.add( UCMEntity.getBaseline( s.trim() ) );
		}

		return bls;
	}
	public void startView( UCMView view ) throws UCMException {
		strategy.startView( view );
	}

	// public Stream GetStreamFromView( String viewtag )
	// {
	// logger.debug( "Getting stream from " + viewtag );
	//
	// String stream = strategy.GetStreamFromView( viewtag );
	// return UCMEntity.GetStream( stream );
	// }

	public Tuple<Stream, String> getStreamFromView( File viewroot ) throws UCMException {
		logger.debug( "1" );
		File wvroot = strategy.getCurrentViewRoot( viewroot );
		logger.debug( "2" );
		String viewtag = strategy.viewrootIsValid( wvroot );
		logger.debug( "3" );
		String streamstr = strategy.getStreamFromView( viewtag );
		logger.debug( "4" );
		Stream stream = UCMEntity.getStream( streamstr );
		logger.debug( "5" );
		return new Tuple<Stream, String>( stream, viewtag );
	}

	public void loadProject( Project project ) throws UCMException {
		String result = strategy.loadProject( project.getFullyQualifiedName() );

		logger.debug( "Result: " + result );

		/* TODO currently the result only returns the stream name. Add more? */
		project.setStream( UCMEntity.getStream( result ) );
	}

	public void createProject( String name, String root, PVob pvob, int policy, String comment, Component... mcomps ) throws UCMException {
		strategy.createProject( name, root, pvob, policy, comment, mcomps );
	}

	public List<Project> getProjects( PVob vob ) throws UCMException {
		return strategy.getProjects( vob );
	}

	private static final Pattern pattern_cache = Pattern.compile( "^\\s*log has been written to\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );

	/**
	 * Returns the update log on completion
	 * 
	 * @param view
	 * @param overwrite
	 * @param loadrules
	 * @return String
	 */
	public String updateView( SnapshotView view, boolean overwrite, String loadrules ) {
		String result = strategy.viewUpdate( view.getViewRoot(), overwrite, loadrules );
		Matcher match = pattern_cache.matcher( result );
		if( match.find() ) {
			return match.group( 1 );
		}

		return "";
	}

	public String getRootDir( Component component ) {
		logger.debug( component.getFullyQualifiedName() );

		return strategy.getRootDir( component.getFullyQualifiedName() );
	}

	public String viewrootIsValid( File view ) throws UCMException {
		return strategy.viewrootIsValid( view );
	}

	public Project getProjectFromStream( Stream stream ) throws UCMException {
		return UCMEntity.getProject( strategy.getProjectFromStream( stream.getFullyQualifiedName() ) + "@" + stream.getPvobString() );
	}

	public List<Component> getModifiableComponents( Project project ) throws UCMException {
		List<String> cs = strategy.getModifiableComponents( project.getFullyQualifiedName() );
		List<Component> comps = new ArrayList<Component>();

		for( String c : cs ) {
			comps.add( UCMEntity.getComponent( c + "@" + project.getPvobString() ) );
		}

		return comps;
	}

	public void loadActivity( Activity activity ) throws UCMException {
		String result = "";

		/* The special case branch */
		if( activity.isSpecialCase() ) {
			result = "System";
		} else {
			result = strategy.loadActivity( activity.getFullyQualifiedName() );
		}
		activity.setUser( result );
	}
	
	public void createActivity( String name, PVob pvob, boolean force, String comment, File view ) throws UCMException {
		strategy.createActivity( name, pvob, force, comment, view );
	}

	public String loadComponent( Component component ) throws UCMException {
		return strategy.loadComponent( component.getFullyQualifiedName() );
	}

	public void createComponent( String name, PVob pvob, String root, String comment, File view ) throws UCMException {
		strategy.createComponent( name, pvob, root, comment, view );
	}

	public String loadStream( Stream stream ) throws UCMException {
		logger.log( "Loading stream " + stream.getFullyQualifiedName() );

		return strategy.loadStream( stream.getFullyQualifiedName() );
	}

	public List<Stream> getChildStreams( Stream stream ) throws UCMException {
		logger.log( "Finding all child streams on " + stream );

		return strategy.getChildStreams( stream.getFullyQualifiedName() );
	}

	/* Attributes */

	/**
	 * Retrieve the attributes for an entity, executed from the current working
	 * directory
	 * 
	 * @param entity
	 *            The given entity
	 * @return A Map of key, value pairs of the attributes
	 * @throws UCMException
	 */
	public Map<String, String> getAttributes( UCMEntity entity ) throws UCMException {
		return strategy.getAttributes( entity.getFullyQualifiedName() );
	}

	/**
	 * Retrieve the attributes for an entity
	 * 
	 * @param entity
	 *            The given entity
	 * @param dir
	 *            A File object of the directory where the command should be
	 *            executed
	 * @return A Map of key, value pairs of the attributes
	 * @throws UCMException
	 */
	public Map<String, String> getAttributes( UCMEntity entity, File dir ) throws UCMException {
		return strategy.getAttributes( entity.getFullyQualifiedName(), dir );
	}

	public void setAttribute( UCMEntity entity, String attribute, String value ) throws UCMException {
		strategy.setAttribute( entity.getFullyQualifiedName(), attribute, value );
	}

	public List<HyperLink> getHlinks( UCMEntity entity, String hlinkType, File dir ) throws UCMException {
		logger.debug( "Entity: " + entity.getFullyQualifiedName() );

		List<Tuple<String, String>> result = strategy.getHlinks( entity.getFullyQualifiedName(), hlinkType, dir );

		List<HyperLink> hlinks = new ArrayList<HyperLink>();

		for( Tuple<String, String> t : result ) {
			HyperLink h = HyperLink.getHyperLink( t.t1, t.t2 );

			hlinks.add( h );
		}

		return hlinks;
	}

	public HyperLink loadHyperLink( HyperLink hlink, File dir ) throws UCMException {
		String result = strategy.loadHyperLink( hlink.getFullyQualifiedName(), dir );

		return null;
	}

	/* VOBS */

	public List<Vob> getVobs( Region region ) {
		return strategy.getVobs( region );
	}

	public void createVob( String vobname, boolean UCMProject, String path, String comment ) throws UCMException {
		strategy.createVob( vobname, UCMProject, path, comment );
	}

	public void loadVob( Vob vob ) throws UCMException {
		strategy.loadVob( vob );
	}

	public void mountVob( Vob vob ) throws UCMException {
		strategy.mountVob( vob );
	}

	public void unmountVob( Vob vob ) throws UCMException {
		strategy.unmountVob( vob );
	}

	public void removeVob( Vob vob ) throws UCMException {
		strategy.removeVob( vob );
	}
	
	public List<Vob> getVobs( boolean pvobs ) throws UCMException {
		return strategy.getVobs( pvobs );
	}
	
	public boolean isUnderSourceControl( File element, File viewContext ) throws UCMException {
		return strategy.isUnderSourceControl( element, viewContext );
	}
	
	public boolean isCheckedout( File element, File viewContext ) throws UCMException {
		return strategy.isCheckedout( element, viewContext );
	}

	/* Views */

	public void createView( String tag, String path, boolean snapshotView, Stream stream ) throws UCMException {
		strategy.createView( tag, path, snapshotView, stream );
	}
	
	public Map<String, String> loadView( UCMView view ) throws UCMException {
		return strategy.loadView( view );
	}

	public List<UCMView> getViews( Region region ) {
		return strategy.getViews( region );
	}
	
	
	
	
	public ChangeSet2 getChangeset( Diffable e1, Diffable e2, boolean merge, File viewContext ) throws UCMException {
		return strategy.getChangeset( e1, e2, merge, viewContext );
	}
	
	public void getDirectoryStatus(  Version version, ChangeSet2 changeset ) throws UCMException {
		strategy.getDirectoryStatus( version, changeset );
	}
	
	public String getPreviousVersion( String version, File viewContext ) throws UCMException {
		return strategy.getPreviousVersion( version, viewContext );
	}
	
	public String getObjectId( String fqname, File viewContext ) throws UCMException {
		return strategy.getObjectId( fqname, viewContext );
	}

	public String getXML() {
		return strategy.getXML();
	}

	public void saveState() {
		strategy.saveState();
	}
}
