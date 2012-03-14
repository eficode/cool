package net.praqma.clearcase.ucm.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.Region;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.changeset.ChangeSet2;
import net.praqma.clearcase.changeset.ChangeSetElement2;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToListProjectsException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnknownUserException;
import net.praqma.clearcase.exceptions.UCMException;
import net.praqma.clearcase.exceptions.UnknownVobException;
import net.praqma.clearcase.interfaces.Diffable;

import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.entities.UCMEntity.Kind;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.entities.Version.Status;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLineException;
import net.praqma.util.io.IO;
import net.praqma.util.structure.Tuple;

public class UCMStrategyCleartool extends Cool implements UCMStrategyInterface {

	private static final String rx_ccdef_allowed = "[\\w\\.-_\\\\]";
	private Logger logger = Logger.getLogger();

	/* Some relatively hard coded "variables" */
	public static final String __TAG_NAME = "tag";
	public static final String tag_install_description = "Installation: \"cleartool mkhltype " + __TAG_NAME + " -c \"Hyperlink type for tagging entities\"\"";

	static {
		// logger.ExcludeClass( UCMStrategyXML.class.getName() );
	}
	private static final String filesep = System.getProperty( "file.separator" );

	public UCMStrategyCleartool() {
		logger.debug( "Using ClearTool strategy" );
	}

	/**/
	public String getMastership( String fqname ) throws UCMException {

	}

	// private static final Pattern rx_versionName = Pattern.compile(
	// "^(\\S+)\\s+([\\S\\s.^@]+)@@(.*)$" );
	private static final Pattern rx_versionName = Pattern.compile( "^(\\S+)\\s+([\\S\\s.^@]+@@.*)$" );

	public ChangeSet2 getChangeset( Diffable e1, Diffable e2, boolean merge, File viewContext ) throws UCMException {

	}

	private static Pattern rx_versionVersion = Pattern.compile( "^(.*?)\\\\(\\d+)\\\\.*?$" );
	private static Pattern rx_versionVersionSimple = Pattern.compile( "^(.*?)\\\\(\\d+)$" );

	public Tuple<String, Integer> getVersionVersion( String version ) throws UCMException {
		Matcher simple = rx_versionVersionSimple.matcher( version );

		if( simple.find() ) {
			return new Tuple<String, Integer>( simple.group( 1 ), Integer.parseInt( simple.group( 2 ) ) );
		} else {
			Matcher advanced = rx_versionVersion.matcher( version );

			if( advanced.find() ) {
				return new Tuple<String, Integer>( advanced.group( 1 ), Integer.parseInt( advanced.group( 2 ) ) );
			} else {
				throw new UCMException( "Could not find any version information on " + version );
			}
		}
	}

	private static final Pattern rx_diffAction = Pattern.compile( "^-{5}\\[\\s*(.+)\\s*\\]-{5}$" );
	private static final Pattern rx_diffFileName = Pattern.compile( "^..(.*)\\s+--\\d+.*$" );

	public void getDirectoryStatus( Version version, ChangeSet2 changeset ) throws UCMException {

		String cmd = "diff -diff -pre \"" + version.getFullyQualifiedName() + "\"";

		// System.out.println( "$ " + cmd );

		try {
			List<String> lines = Cleartool.run( cmd, null, true, true ).stdoutList;

			for( int i = 0; i < lines.size(); ++i ) {
				// System.out.println( "[" + i + "] " + lines.get( i ) );
				Matcher m = rx_diffAction.matcher( lines.get( i ) );

				/* A diff action */
				if( m.find() ) {
					String action = m.group( 1 ).trim();

					/* ADDED action */
					if( action.equals( "added" ) ) {
						/* This is an add, the next line is the file added */
						Matcher mname = rx_diffFileName.matcher( lines.get( i + 1 ) );
						if( mname.find() ) {
							changeset.addElement( new File( version.getFile(), mname.group( 1 ).trim() ), Version.Status.ADDED, version );
						} else {
							logger.warning( "Unknown filename line: " + lines.get( i + 1 ) );
						}

						/* Fast forward one line */
						i++;
						/* ADDED action */
					} else if( action.equals( "deleted" ) ) {
						/* This is an add, the next line is the file added */
						Matcher mname = rx_diffFileName.matcher( lines.get( i + 1 ) );
						if( mname.find() ) {
							changeset.addElement( new File( version.getFile(), mname.group( 1 ).trim() ), Version.Status.DELETED, version );
						} else {
							logger.warning( "Unknown filename line: " + lines.get( i + 1 ) );
						}

						/* Fast forward one line */
						i++;

					} else if( action.equals( "renamed to" ) ) {
						/* This is a rename, the next line is the file added */
						Matcher oldname = rx_diffFileName.matcher( lines.get( i + 1 ) );
						Matcher newname = rx_diffFileName.matcher( lines.get( i + 3 ) );

						File newFile = null;
						File oldFile = null;

						if( newname.find() ) {
							newFile = new File( version.getFile(), newname.group( 1 ).trim() );
						} else {
							logger.warning( "Unknown filename line: " + lines.get( i + 1 ) );
						}

						if( oldname.find() ) {
							oldFile = new File( version.getFile(), oldname.group( 1 ).trim() );
						} else {
							logger.warning( "Unknown filename line: " + lines.get( i + 1 ) );
						}

						// changeset.addElement( newFile,
						// Version.Status.CHANGED, version );

						logger.debug( "[" + oldFile + "]" );
						logger.debug( "[" + newFile + "]" );
						ChangeSetElement2 element = new ChangeSetElement2( newFile, Version.Status.CHANGED, version );
						element.setOldFile( oldFile );
						changeset.addElement( element );

						/* Fast forward four line */
						i += 4;

					} else {
						/* I don't know this action, let's move on */
						logger.warning( "Unhandled diff action: " + action );
					}
				}
			}
		} catch( AbnormalProcessTerminationException e ) {
			throw new UCMException( "Could not execute the command: " + e.getMessage(), e );
		} catch( IndexOutOfBoundsException e1 ) {
			throw new UCMException( "Out of bounds: " + e1.getMessage(), e1 );
		} catch( Exception e2 ) {
			throw new UCMException( "Something new, something unhandled: " + e2.getMessage(), e2 );
		}
	}

	public String getPreviousVersion( String version, File viewContext ) throws UCMException {
		String cmd = "describe -fmt %PVn " + version;

		try {
			return Cleartool.run( cmd, viewContext ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			throw new UCMException( "Could not get previous version: " + e.getMessage(), e );
		}
	}

	public String getObjectId( String fqname, File viewContext ) throws UCMException {
		String cmd = "describe -fmt %On " + fqname;

		try {
			return Cleartool.run( cmd, viewContext ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			throw new UCMException( "Could not get object id: " + e.getMessage(), e );
		}
	}

	

	public void changeOwnership( UCMEntity entity, String username, File viewContext ) throws UCMException, UCMEntityNotFoundException, UnknownUserException, UnknownVobException {
		changeOwnership( entity.getFullyQualifiedName(), username, viewContext );
	}

	public void changeOwnership( String fqname, String username, File viewContext ) throws UCMException, UCMEntityNotFoundException, UnknownUserException, UnknownVobException {
		String cmd = "protect -chown " + username + " \"" + fqname + "\"";

		try {
			Cleartool.run( cmd, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			if( e.getMessage().contains( "Unable to determine VOB for pathname" ) ) {
				throw new UnknownVobException( e );
			}

			if( e.getMessage().contains( "Unknown user name" ) ) {
				throw new UnknownUserException( username, e );
			}

			if( e.getMessage().matches( rx_entityNotFound ) ) {
				throw new UCMEntityNotFoundException( fqname, e );
			}

			if( e.getMessage().contains( " ClearCase object not found" ) ) {
				throw new UCMEntityNotFoundException( fqname, e );
			}

			throw new UCMException( e );
		}
	}

	/************************************************************************
	 * PROJECT FUNCTIONALITY
	 * 
	 * @throws UCMException
	 ************************************************************************/
	public String getProjectFromStream( String stream ) throws UCMException {

	}

	public List<String> getModifiableComponents( String project ) throws UCMException {

	}

	public String loadProject( String project ) throws UCMException {

	}

	public void createProject( String name, String root, PVob pvob, int policy, String comment, Component... mcomps ) throws UnableToCreateEntityException {

	}

	/*
	 * or /f %i in ('cleartool lsproject -s -invob \Cool_PVOB') do @cleartool
	 * desc -fmt "project:%i@\Cool_PVOB %[istream]Xp\n" project:%i@\Cool_PVOB
	 */
	public List<Project> getProjects( PVob vob ) throws UnableToListProjectsException {

	}

	/************************************************************************
	 * ACTIVITY FUNCTIONALITY
	 * 
	 * @throws UCMException
	 ************************************************************************/
	public String loadActivity( String activity ) throws UCMException {

	}

	public void createActivity( String name, PVob pvob, boolean force, String comment, File view ) throws UCMException {
		String cmd = "mkactivity" + ( comment != null ? " -c \"" + comment + "\"" : "" ) + ( force ? " -force" : "" ) + ( name != null ? " " + name + "@" + pvob : "" );

		try {
			Cleartool.run( cmd, view );
		} catch( Exception e ) {
			throw new UCMException( e.getMessage(), UCMType.CREATION_FAILED );
		}
	}

	/************************************************************************
	 * BASELINE FUNCTIONALITY
	 * 
	 * @throws UCMException
	 ************************************************************************/
	public String loadBaseline( String baseline ) throws UCMException {

	}

	public List<String> getBaselineDiff( Diffable d1, Diffable d2, boolean merge, File viewContext ) throws UCMException {

	}

	public List<String> getBaselineDiff( File dir, String baseline, String other, boolean nmerge, String pvob ) throws UCMException {
		/* Check if we are in view context */
		checkViewContext( dir );

		String cmd = "diffbl -pre -act -ver -nmerge " + baseline;

		try {
			return Cleartool.run( cmd, dir ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			if( e.getMessage().equalsIgnoreCase( "cleartool: Error: The -nmerge option requires that both baselines be from the same stream." ) ) {
				logger.debug( "The given Baseline, \"" + baseline + "\" is the first on the Stream" );

				List<String> result = new ArrayList<String>();

				result.add( ">> no_activity@" + pvob + " \"NO ACTIVITY\"" );

				List<String> vobs = ListVobs( dir );

				for( String vob : vobs ) {
					List<String> files = Cleartool.run( "ls -s -rec " + vob, dir ).stdoutList;

					/* Remove lost + found folder */
					for( int i = 0; i < files.size(); i++ ) {
						if( !files.get( i ).matches( "^lost+found@@.*" ) ) {
							result.add( dir + filesep + files.get( i ) );
						}
					}
				}

				return result;
			}

			/* The exception could not be handled! */
			throw new UCMException( "Could not get the baseline differences", e.getMessage() );
		}
	}

	private final Pattern rx_baselineDiff = Pattern.compile( "^(\\S+)\\s*(.*?)\\s*(.*)\\s*$" );

	public List<Version> baselineDifferences( Baseline bl1, Baseline bl2, boolean merge, SnapshotView view ) throws UCMException {

	}

	public boolean createBaseline( String fqname, Component component, File view, boolean incremental, boolean identical, Activity[] activities, Component[] depends ) throws UCMException {

	}

	@Override
	public void setPromotionLevel( String baseline, String plevel ) throws UCMException {
		String cmd = "chbl -level " + plevel + " " + baseline;
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			throw new UCMException( "Could not set promotion level to " + plevel + " for " + baseline + ": " + e.getMessage(), e.getMessage() );
		}
	}

	@Override
	public String getBaselineActivities( String baseline ) {
		// TODO Auto-generated method stub
		return null;
	}

	private static final Pattern rx_checkMergeError = Pattern.compile( "An error occurred while merging file elements in the target view.*?Unable to perform merge", Pattern.DOTALL );
	private static final Pattern rx_checkDeliverDenied = Pattern.compile( "does not allow deliver operations from streams in other", Pattern.DOTALL );
	private static final Pattern rx_checkProgress = Pattern.compile( "which is currently involved in an.*?active deliver or rebase operation", Pattern.DOTALL );

	public String deliver( String baseline, String stream, String target, File context, String viewtag, boolean force, boolean complete, boolean abort ) throws UCMException {
		
	}

	public void cancelDeliver( File viewcontext, Stream stream ) throws UCMException {

	}



	/************************************************************************
	 * COMPONENT FUNCTIONALITY
	 ************************************************************************/
	private static final String rx_component_load = "\\s*Error: component not found\\s*";

	@Override
	public List<String> getBaselines( String component, String stream, PromotionLevel plevel ) throws UCMException {

	}

	@Override
	public String getRootDir( String component ) throws UCMException {

	}

	public String loadComponent( String component ) throws UCMException {

	}

	public void createComponent( String name, PVob pvob, String root, String comment, File view ) throws UCMException {

	}

	/************************************************************************
	 * STREAM FUNCTIONALITY
	 ************************************************************************/
	private final String rx_rebase_in_progress = "^Rebase operation in progress on stream";
	private static final String rx_stream_load = "\\s*Error: stream not found\\s*";

	public void recommendBaseline( String stream, String baseline ) throws UCMException {


	}

	public String getRecommendedBaselines( String stream ) throws UCMException {

	}

	public String getStreamFromView( String viewtag ) throws UCMException {

	}

	public void createStream( String pstream, String nstream, boolean readonly, String baseline ) throws UCMException {

	}

	public void createIntegrationStream( String name, Project project, Baseline baseline ) throws UCMException {

	}

	public void generate( String stream ) throws UCMException {

	}

	public boolean streamExists( String fqname ) {
		String cmd = "describe " + fqname;
		try {
			Cleartool.run( cmd );
			return true;
		} catch( AbnormalProcessTerminationException e ) {
			return false;
		}
	}

	public boolean rebaseStream( String viewtag, String stream, String baseline, boolean complete ) throws UCMException {

	}

	public boolean isRebasing( String stream ) throws UCMException {

	}

	public void cancelRebase( String stream ) throws UCMException {

	}

	public List<String> getLatestBaselines( String stream ) throws UCMException {

	}

	public void loadStream( Stream stream ) throws UCMException {

	}

	/**
	 * @param fqstream
	 *            - Fully Qulyfied Stream name (stream:name@\P_VOB)
	 * @return List<Stream> {@link Stream}
	 */
	@Override
	public List<Stream> getChildStreams( String fqstream ) throws UCMException {

	}

	public void rebase( Stream stream, Baseline baseline ) {
	}

	/************************************************************************
	 * VERSION FUNCTIONALITY
	 * 
	 * @throws UCMException
	 ************************************************************************/
	public String getVersion( String version, String separator ) throws UCMException {
		String cmd = "desc -fmt %Nd" + separator + "%u" + separator + "%h" + separator + "%c" + separator + "%Rf" + separator + "%m" + separator + "%Vn" + separator + "%Xn \"" + version + "\"";
		try {
			return Cleartool.run( cmd ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			throw new UCMException( "Unable to getversion: " + e.getMessage() );
		}
	}

	private static final String rx_ccdef_voblikename = "[\\\\\\w\\.-/]";
	private static final String rx_ccdef_filename = "[.[^@]+]";
	private static final Pattern rx_extendedName = Pattern.compile( "^(?:(" + rx_ccdef_filename + "+)@@)(?:(" + rx_ccdef_filename + "+)@@)?(.+)$" );
	private static final Pattern rx_getFilename = Pattern.compile( File.pathSeparator + "(.*?)$" );

	public void loadVersion( Version version ) throws UCMException {

	}

	public String getVersionExtension( File file, File viewroot ) throws UCMException {

	}

	private static final Pattern rx_checkExistence = Pattern.compile( ".*?Entry named \".*\" already exists.*?" );

	/*
	 * cleartool: Error: Entry named "myfile1.txt" already exists. cleartool:
	 * Error: Unable to create element
	 * "c:\Temp\views\snade\001\Snade001\Model\myfile1.txt".
	 */
	public void addToSourceControl( File file, boolean mkdir, File view ) throws UCMException {

	}

	public void checkIn( File file, boolean identical, File viewContext ) throws UCMException {

	}

	private static final Pattern rx_AlreadyCheckedOut = Pattern.compile( "" );

	public void checkOut( File file, File viewContext ) throws UCMException {

	}



	public void removeVersion( File file, File viewContext ) throws UCMException {


	}

	public void removeName( File file, File viewContext ) throws UCMException {


	}

	public void moveFile( File file, File destination, File viewContext ) throws UCMException {

	}

	public List<File> getUnchecedInFiles( File viewContext ) throws UCMException {

	}

	/************************************************************************
	 * TAG FUNCTIONALITY
	 ************************************************************************/
	private static final Pattern pattern_tags = Pattern.compile( "^\\s*(tag@\\d+@" + rx_ccdef_allowed + "+)\\s*->\\s*\"(.*?)\"\\s*$" );
	private static final Pattern pattern_hlink = Pattern.compile( "^\\s*(" + rx_ccdef_allowed + "+@\\d+@" + rx_ccdef_allowed + "+)\\s*->\\s*\"*(.*?)\"*\\s*$" );
	private static final Pattern pattern_remove_verbose_tag = Pattern.compile( "^.*?\"(.*)\".*?$" );
	private static final Pattern pattern_hlink_type_missing = Pattern.compile( ".*Error: hyperlink type \"(.*?)\" not found in VOB \"(\\S+)\" .*" );

	// public List<Tuple<String, String>> GetTags( String fqname ) throws
	// UCMException
	public List<String[]> getTags( String fqname ) throws UCMException {
		
	}

	public String getTag( String fqname ) {
		// TODO Auto-generated method stub
		return null;
	}

	public String newTag( UCMEntity entity, String cgi ) throws UCMException {

	}

	public void deleteTag( String fqname ) {
		// TODO Auto-generated method stub
	}

	public void deleteTagsWithID( String tagType, String tagID, String entity ) throws UCMException {

	}

	public String putTag( String fqname, String keyval, UCMEntity entity ) {
		// TODO Auto-generated method stub
		return null;
	}

	public String loadHyperLink( String fqname, File dir ) throws UCMException {
		String cmd = "describe " + fqname;

		CmdResult res = null;
		try {
			res = Cleartool.run( cmd, dir );
		} catch( AbnormalProcessTerminationException e ) {
			throw new UCMException( "Could not load hyperlink", e.getMessage() );
		}

		return res.stdoutBuffer.toString();
	}

	public List<Tuple<String, String>> getHlinks( String fqname, String hlinkType, File dir ) throws UCMException {

	}

	/************************************************************************
	 * SNAPSHOT VIEW FUNCTIONALITY
	 ************************************************************************/
	// View uuid: b2b51957.41a24c7a.b998.67:b8:4a:93:60:01
	private static final Pattern pattern_view_uuid = Pattern.compile( "^\\s*View uuid:\\s*([\\w\\.:]+).*$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE );
	protected static final Pattern rx_view_uuid = Pattern.compile( "view_uuid:(.*)" );
	private final String rx_co_file = ".*CHECKEDOUT$";
	private final String rx_ctr_file = ".*\\.contrib";
	private final String rx_keep_file = ".*\\.keep$";

	public void checkViewContext( File dir ) throws UCMException {
		logger.debug( "" );

		String cmd = "pwv -root";
		try {
			Cleartool.run( cmd, dir ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			if( e.getMessage().equalsIgnoreCase( "cleartool: Error: operation requires a view" ) ) {
				throw new UCMException( "operation requires a view", e.getMessage() );
			}

			throw e;
		}
	}

	public String getViewtag( File viewContext ) throws UCMException {

	}

	public boolean IsVob( File dir ) {

	}

	public List<String> ListVobs( File viewroot ) {
		logger.debug( "Listing vobs at " + viewroot );

		File[] files = viewroot.listFiles();
		List<String> vobs = new ArrayList<String>();

		for( File f : files ) {
			if( f.isDirectory() ) {
				if( IsVob( f ) ) {
					vobs.add( f.getName() );
				}
			}
		}

		return vobs;
	}

	public void makeSnapshotView( String stream, File viewroot, String viewtag ) throws UCMException {

	}

	private static final Pattern rx_view_rebasing = Pattern.compile( "^\\.*Error: This view is currently being used to rebase stream \"(.+)\"\\.*$" );

	public String viewUpdate( File viewroot, boolean overwrite, String loadrules ) throws UCMException {

	}

	public void regenerateViewDotDat( File dir, String viewtag ) throws UCMException {
		
	}

	public boolean viewExists( String viewtag ) {

	}

	public Map<String, Integer> swipeView( File viewroot, boolean excludeRoot ) throws UCMException {
		
	}

	@Override
	public File getCurrentViewRoot( File viewroot ) throws UCMException {

	}

	public String viewrootIsValid( File viewroot ) throws UCMException {

	}

	public void createView( String tag, String path, boolean snapshotView, Stream stream ) throws UCMException {

	}

	public void removeView( UCMView view ) throws UCMException {

	}

	public static final Pattern rx_view_get_path = Pattern.compile( "^\\s*Global path:\\s*(.*?)\\s*$" );

	public Map<String, String> loadView( UCMView view ) throws UCMException {

	}

	public void startView( UCMView view ) throws UCMException {

	}

	public void endView( String viewtag ) throws UCMException {

	}

	/*****************************
	 * Vobs
	 *****************************/
	public static final Pattern rx_vob_get_path = Pattern.compile( "^\\s*VOB storage global pathname\\s*\"(.*?)\"\\s*$" );

	public void createVob( String vobname, boolean UCMProject, String path, String comment ) throws UCMException {

	}

	public void loadVob( Vob vob ) throws UCMException {

	}

	public boolean isCheckedout( File element, File viewContext ) throws UCMException {

	}

	public boolean isUnderSourceControl( File element, File viewContext ) throws UCMException {

	}

	public List<Vob> getVobs( Region region ) throws UCMException {

	}

	public List<Vob> getVobs( boolean pvobs ) throws UCMException {
		String cmd = "lsvob -s";

		List<Vob> vobs = new ArrayList<Vob>();

		try {
			List<String> vs = Cleartool.run( cmd ).stdoutList;

			for( String v : vs ) {
				Vob vob = new Vob( v );
				vob.load();
				if( vob.isProjectVob() && pvobs ) {
					vobs.add( (PVob) vob );
				} else {
					vobs.add( vob );
				}
			}
		} catch( Exception e ) {
			throw new UCMException( "Could not list Vobs: " + e.getMessage() );
		}

		return vobs;
	}

	public void mountVob( Vob vob ) throws UCMException {

	}

	public void unmountVob( Vob vob ) throws UCMException {

	}

	public int getVobCount() throws UCMException {
		String cmd = "lsvob -short";
		try {
			return Cleartool.run( cmd ).stdoutList.size();
		} catch( AbnormalProcessTerminationException e ) {
			throw new UCMException( "Unable to get vob count: " + e.getMessage() );
		}
	}

	public void removeVob( Vob vob ) throws UCMException {

	}

	/*****************************
	 * Attributes
	 *****************************/
	/**
	 * 
	 * @param fqname
	 * @param attribute
	 * @return String
	 * @throws UCMException
	 */
	public String getAttribute( String fqname, String attribute ) throws UCMException {

	}

	private static final String rx_attr_find = "^\\s*\\S+\\s*=\\s*\\S*\\s*$";

	public Map<String, String> getAttributes( String fqname ) throws UCMException {
		return getAttributes( fqname, null );
	}

	public Map<String, String> getAttributes( String fqname, File dir ) throws UCMException {

	}

	/**
	 * 
	 * @param fqname
	 * @param attribute
	 * @param value
	 * @throws UCMException
	 */
	public void setAttribute( String fqname, String attribute, String value ) throws UCMException {

	}

	private static final Pattern __FIND_VIEW_ROOT = Pattern.compile( "^\\s*\\**\\s*([\\w\\.-]+)\\s*(.+)$" );

	@Override
	public List<UCMView> getViews( Region region ) throws UCMException {

	}

	/*****************************
	 * OTHER STUFF
	 *****************************/
	// public List<Stream> getChildStreams(){
	// CmdResult res = null;
	// String cmd = "lsvob"
	//
	// return null;
	// }
	public String getXML() {
		// TODO Auto-generated method stub
		return null;
	}

	public void saveState() {
		// TODO Auto-generated method stub
	}

	/**
	 * rolls back a dlivery on the oldSourceStream using dir as view root
	 * 
	 * @param oldViewTag
	 *            - can be null if so we will just use dir as view root if it
	 *            exists
	 * @param oldSourceStream
	 *            - the stream which the previous deliver was made from
	 * @param dir
	 *            - the view root
	 * @throws UCMException
	 *             - if anything goes whrong we will throw a UCMException
	 */
	@Override
	public void remoteDeliverCancel( String oldViewTag, String oldSourceStream, File dir ) throws UCMException {

	}

	private static boolean deleteDir( File dir ) {

	}
}
