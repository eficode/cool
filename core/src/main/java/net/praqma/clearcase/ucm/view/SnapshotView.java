package net.praqma.clearcase.ucm.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToListViewsException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.exceptions.ViewException.Type;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.io.IO;
import net.praqma.util.structure.Printer;
import net.praqma.util.structure.Tuple;

/**
 * The OO implementation of the ClearCase entity Snapshot view. The next line
 * 
 * @author wolfgang
 * 
 */
public class SnapshotView extends UCMView {

	transient private static Logger logger = Logger.getLogger();

	//protected static final String rx_view_uuid = "view_uuid:(.*)";
	protected static final Pattern rx_view_uuid_file = Pattern.compile( "view_uuid:(.*)" );
	protected static final Pattern rx_view_uuid = Pattern.compile( "View uuid:(.*)" );
	private static final Pattern rx_view_rebasing = Pattern.compile( "^\\.*Error: This view is currently being used to rebase stream \"(.+)\"\\.*$" );
	private static final Pattern pattern_cache = Pattern.compile( "^\\s*log has been written to\\s*\"(.*?)\"", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE );
	
	private final String rx_co_file = ".*CHECKEDOUT$";
	private final String rx_ctr_file = ".*\\.contrib";
	private final String rx_keep_file = ".*\\.keep$";

	private File viewroot = null;

	private PVob pvob;
	private String uuid = "";
	private String globalPath = "";

	private Stream stream;

	public enum Components {
		ALL, MODIFIABLE
	}

	public static class LoadRules {
		private String loadRules;

		/**
		 * Create load rules based on {@link Components}
		 * @throws UnableToLoadEntityException 
		 * 
		 */
		public LoadRules( SnapshotView view, Components components ) throws UnableToInitializeEntityException, CleartoolException, UnableToLoadEntityException {
			loadRules = " -add_loadrules ";

			if( components.equals( Components.ALL ) ) {
				logger.debug( "All components" );

				List<Baseline> bls = view.stream.getLatestBaselines();
				for( Baseline b : bls ) {
					String rule = b.load().getComponent().getRootDir();
					rule = rule.replaceFirst( "^\\\\", " " );
					loadRules += rule;
				}
			} else {
				logger.debug( "Modifiable components" );

				Project project = view.stream.getProject();
				List<Component> comps = project.getModifiableComponents();
				for( Component c : comps ) {
					String rule = c.getRootDir();
					rule = rule.replaceFirst( "^\\\\", " " );
					loadRules += rule;
				}
			}
		}

		/**
		 * Create load rules based on a string
		 * 
		 * @param loadRules
		 */
		public LoadRules( String loadRules ) {
			this.loadRules = loadRules = " -add_loadrules " + loadRules;
		}

		public String getLoadRules() {
			return loadRules;
		}
	}

	public SnapshotView() {

	}

	public SnapshotView( File viewroot ) throws UnableToInitializeEntityException, CleartoolException, ViewException, IOException {
		/* TODO Test the view root? Does it exist? Is it a directory? */

		this.viewroot = viewroot;

		Tuple<Stream, String> t = getStreamFromView( viewroot );

		this.viewtag = t.t2;
		this.viewroot = viewroot;
		this.stream = t.t1;
		this.pvob = this.stream.getPVob();
	}

	/**
	 * Create a Snapshot view given a Stream, view root and a view tag.
	 * 
	 * @param stream
	 *            The Stream
	 * @param viewroot
	 * @param viewtag
	 * @return SnapShotView
	 */
	public static SnapshotView create( Stream stream, File viewroot, String viewtag ) throws ViewException, UnableToInitializeEntityException, CleartoolException, IOException {
		//context.makeSnapshotView( stream, viewroot, viewtag );

		logger.debug( "The view \"" + viewtag + "\" in \"" + viewroot + "\"" );

		if( viewroot.exists() ) {
			IO.deleteDirectory( viewroot );
		}

		//this.generate( stream );
		stream.generate();

		String cmd = "mkview -snap -tag " + viewtag + " -stream " + stream + " \"" + viewroot.getAbsolutePath() + "\"";

		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			logger.warning( "Could not create snapshot view \"" + viewtag + "\"" );
			//throw new UCMException( "Could not create snapshot view \"" + viewtag + "\"", e.getMessage(), UCMType.VIEW_ERROR );
			throw new ViewException( "Unable to create view " + viewtag + " at " + viewroot, viewroot.getAbsolutePath(), Type.CREATION_FAILED, e );
		}

		SnapshotView view = new SnapshotView( viewroot );
		view.setStream( stream );
		
		return view;
	}

	public static void createEnvironment( File viewroot ) {
		createEnvironment( viewroot, "" );
	}

	public static void createEnvironment( File viewroot, String viewtagsuffix ) {
		String viewtag = "cool_" + System.getenv( "COMPUTERNAME" ) + "_env" + viewtagsuffix;
	}

	public static void regenerateViewDotDat( File dir, String viewtag ) throws IOException, UnableToListViewsException {
		logger.debug( dir + ", " + viewtag );

		File viewdat = new File( dir + File.separator + "view.dat" );

		if( viewdat.exists() ) {
			throw new IOException( "view.dat file already exist. No need for regenrating." );
		}

		String cmd = "lsview -l " + viewtag;
		/* TODO Check this functions behavior, if the view doesn't exist */
		String result = "";
		try {
			result = Cleartool.run( cmd ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			throw new UnableToListViewsException( viewtag, dir, e );
		}

		// System.out.println(result);

		Matcher match = rx_view_uuid.matcher( result );
		if( !match.find() ) {
			logger.warning( "The UUID of the view " + viewtag + " does not exist!" );
			throw new IOException( "The UUID of the view " + viewtag + " does not exist!" );
		}

		String uuid = match.group( 1 );

		cmd = "lsview -uuid " + uuid;

		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			throw new IOException( "Unable to read the UUID(" + uuid + ") from view tag " + viewtag, e );
		}

		if( dir.exists() ) {
			logger.warning( "The view root, " + dir + ",  already exists - reuse may be problematic" );
		} else {
			dir.mkdirs();
		}

		try {
			FileOutputStream fos = new FileOutputStream( viewdat );
			fos.write( ( "ws_oid:00000000000000000000000000000000 view_uuid:" + uuid ).getBytes() );
			fos.close();
		} catch( IOException e ) {
			throw new IOException( "Could not create view.dat", e );
		}

		/* TODO Too much windows.... */
		// cmd = "attrib +h +r " + viewdat;
		if( !viewdat.setReadOnly() ) {
			logger.warning( "Could not set view.dat as read only" );
			throw new IOException( "Could not set view.dat as read only" );
		}
		// viewdat.set
		// Command.run( cmd );
	}

	public File getViewRoot() {
		return this.viewroot;
	}

	@Override
	public String getPath() {
		return this.viewroot.toString();
	}

	public Stream getStream() throws UnableToInitializeEntityException, CleartoolException, ViewException, IOException {
		if( this.stream == null ) {
			Stream stream = getStreamFromView( getViewRoot() ).getFirst();
			this.stream = stream;
		}
		return stream;
	}

	private void setStream( Stream stream ) {
		this.stream = stream;
	}

	public static String getViewtag( File context ) throws CleartoolException {
		String cmd = "pwv -s";
		try {
			return Cleartool.run( cmd, context ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to get view tag at " + context , e );
		}
	}

	public static SnapshotView getSnapshotViewFromPath( File viewroot ) throws ClearCaseException, IOException {
		String viewtag = getViewtag( viewroot );
		SnapshotView view = null;

		if( UCMView.viewExists( viewtag ) ) {
			view = get( viewroot );
		} else {
			throw new ClearCaseException( "View is not valid" );
		}

		return view;
	}

	/**
	 * Determine if the views view root is valid, returning its view tag
	 * 
	 * @return The view tag
	 * @throws IOException 
	 * @throws CleartoolException 
	 * @throws ViewException 
	 * @throws UCMException
	 */
	public static String viewrootIsValid( File viewroot ) throws IOException, CleartoolException, ViewException {
		logger.debug( viewroot.getAbsolutePath() );

		File viewdotdatpname = new File( viewroot + File.separator + "view.dat" );

		logger.debug( "The view file = " + viewdotdatpname );

		FileReader fr = null;
		try {
			fr = new FileReader( viewdotdatpname );
		} catch( FileNotFoundException e1 ) {
			logger.warning( "\"" + viewdotdatpname + "\" not found!" );
			throw new ViewException( "No view .dat file found", viewroot.getAbsolutePath(), Type.VIEW_DOT_DAT, e1 );
		}

		BufferedReader br = new BufferedReader( fr );
		String line;
		StringBuffer result = new StringBuffer();
		try {
			while( ( line = br.readLine() ) != null ) {
				result.append( line );
			}
		} catch( IOException e ) {
			logger.warning( "Couldn't read lines from " + viewdotdatpname );
			throw e;
		}

		logger.debug( "FILE CONTENT=" + result.toString() );

		Matcher match = rx_view_uuid_file.matcher( result.toString() );

		String uuid = "";

		if( match.find() ) {
			/* A match is found */
			uuid = match.group( 1 ).trim();
		} else {
			logger.warning( "UUID not found!" );
			throw new IOException( "UUID not found" );
		}

		String cmd = "lsview -s -uuid " + uuid;
		try {
			String viewtag = Cleartool.run( cmd ).stdoutBuffer.toString().trim();
			return viewtag;
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to list view with " + uuid, e );
		}
	}

	public class UpdateInfo {
		public Integer totalFilesToBeDeleted = 0;
		public boolean success = false;
		public Integer filesDeleted = 0;
		public Integer dirsDeleted = 0;
	}

	public Tuple<Stream, String> getStreamFromView( File viewroot ) throws UnableToInitializeEntityException, CleartoolException, ViewException, IOException {
		File wvroot = getCurrentViewRoot( viewroot );
		String viewtag = viewrootIsValid( wvroot );
		String streamstr = getStreamFromView( viewtag );
		Stream stream = Stream.get( streamstr );
		return new Tuple<Stream, String>( stream, viewtag );
	}

	public File getCurrentViewRoot( File viewroot ) throws ViewException {
		logger.debug( viewroot.getAbsolutePath() );

		try {
			String wvroot = Cleartool.run( "pwv -root", viewroot ).stdoutBuffer.toString();

			return new File( wvroot );
		} catch( Exception e ) {
			throw new ViewException( "Unable to get current view " + viewroot, path, Type.INFO_FAILED, e );
		}
	}

	public String getStreamFromView( String viewtag ) throws ViewException {
		try {
			String fqstreamstr = Cleartool.run( "lsstream -fmt %Xn -view " + viewtag ).stdoutBuffer.toString();
			return fqstreamstr;
		} catch( AbnormalProcessTerminationException e ) {
			throw new ViewException( "Unable to get stream from view " + viewtag, path, Type.INFO_FAILED, e );
		}
	}

	/*
	 * public void cancel() throws UCMException { context.cancelDeliver(
	 * viewroot, null ); }
	 */

	public UpdateInfo Update( boolean swipe, boolean generate, boolean overwrite, boolean excludeRoot, LoadRules loadRules ) throws CleartoolException, ViewException {

		UpdateInfo info = new UpdateInfo();

		// TODO generate the streams config spec if required
		if( generate ) {
			this.stream.generate();
		}

		logger.debug( "STREAM GENEREATES" );

		if( swipe ) {
			Map<String, Integer> sinfo = swipe( this.viewroot, excludeRoot );
			info.success = sinfo.get( "success" ) == 1 ? true : false;
			info.totalFilesToBeDeleted = sinfo.get( "total" );
			info.dirsDeleted = sinfo.get( "dirs_deleted" );
			info.filesDeleted = sinfo.get( "files_deleted" );
		}

		logger.debug( "SWIPED" );

		// Cache current directory and chdir into the viewroot
		String result = updateView( this, overwrite, loadRules.getLoadRules() );
		logger.debug( result );

		return info;
	}
	
	private static String updateView( SnapshotView view, boolean overwrite, String loadrules ) throws CleartoolException, ViewException {
		//String result = strategy.viewUpdate( view.getViewRoot(), overwrite, loadrules );
		
		String result = "";
		
		logger.debug( view.getViewRoot().getAbsolutePath() );

		String cmd = "setcs -stream";
		try {
			Cleartool.run( cmd, view.getViewRoot() );
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to set cs stream: " + view.getViewRoot() , e );
		}

		logger.debug( "Updating view" );

		cmd = "update -force " + ( overwrite ? " -overwrite " : "" ) + loadrules;
		try {
			result = Cleartool.run( cmd, view.getViewRoot(), true ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			Matcher m = rx_view_rebasing.matcher( e.getMessage() );
			if( m.find() ) {
				logger.warning( "The view is currently rebasing the stream " + m.group( 1 ) + ": " + e.getMessage() );
				logger.warning( e );
				throw new ViewException( "The view is currently rebasing the stream " + m.group( 1 ), view.getViewRoot().getAbsolutePath(), Type.REBASING, e );
			} else {
				logger.warning( e );
				throw new ViewException( "Unable to update view " + m.group( 1 ), view.getViewRoot().getAbsolutePath(), Type.UNKNOWN, e );
			}
		}

		
		Matcher match = pattern_cache.matcher( result );
		if( match.find() ) {
			return match.group( 1 );
		}

		return "";
	}
	
	public Map<String, Integer> swipe( File viewroot, boolean excludeRoot ) throws CleartoolException {
		logger.debug( viewroot.toString() );

		File[] files = viewroot.listFiles();
		String fls = "";
		List<File> notVobs = new ArrayList<File>();
		List<File> rootVPFiles = new ArrayList<File>();

		/*
		 * Scanning root folder for directories that are not vobs and files, not
		 * view.dat
		 */
		for( File f : files ) {
			if( !f.canWrite() ) {
				logger.debug( f + " is write protected." );
				continue;
			}

			if( f.isDirectory() ) {
				if( Vob.isVob( f ) ) {
					fls += "\"" + f.getAbsolutePath() + "\" ";
				} else {
					notVobs.add( f );
				}
			} else {
				if( f.getName().equalsIgnoreCase( "view.dat" ) ) {
					continue;
				}
				rootVPFiles.add( f );
			}
		}

		/* Remove all other dirs */
		for( File notVob : notVobs ) {
			logger.debug( "Removing " + notVob );
			net.praqma.util.io.IO.deleteDirectory( notVob );
		}

		Map<String, Integer> info = new HashMap<String, Integer>();
		info.put( "success", 1 );

		if( fls.length() == 0 ) {
			logger.debug( "No files to delete" );
			return info;
		}

		/* Get view private files from vobs */
		String cmd = "ls -short -recurse -view_only " + fls;
		List<String> result = null;
		try {
			result = Cleartool.run( cmd ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to list files " + fls, e );
		}
		List<File> vpFiles = new ArrayList<File>();

		if( !excludeRoot ) {
			vpFiles.addAll( rootVPFiles );
		}

		for( String vpFile : result ) {
			if( vpFile.matches( rx_co_file ) || vpFile.matches( rx_keep_file ) || vpFile.matches( rx_ctr_file ) ) {
				continue;
			}

			vpFiles.add( new File( vpFile ) );
		}

		int total = vpFiles.size();

		info.put( "total", total );

		logger.debug( "Found " + total + " files, of which " + ( total - vpFiles.size() ) + " were CO, CTR or KEEP's." );

		List<File> dirs = new ArrayList<File>();
		int dircount = 0;
		int filecount = 0;

		/* Removing view private files, saving directories for later */
		logger.verbose( "Removing files:" );
		for( File f : vpFiles ) {
			// logger.debug( "FILE=" + f );

			if( f.exists() ) {
				if( f.isDirectory() ) {
					dirs.add( f );
				} else {
					logger.verbose( " * " + f );
					f.delete();
					filecount++;
				}
			} else {
				logger.debug( "The file " + f + " does not exist." );
			}
		}

		info.put( "files_deleted", filecount );

		/* TODO Remove the directories, somehow!? Only the empty!? */
		logger.verbose( "Removing directories:" );
		for( File d : dirs ) {
			try {
				logger.verbose( " * " + d );
				d.delete();
				dircount++;
			} catch( SecurityException e ) {
				logger.debug( "Unable to delete \"" + d + "\". Probably not empty." );
			}
		}

		info.put( "dirs_deleted", dircount );

		logger.debug( "Deleted " + dircount + " director" + ( dircount == 1 ? "y" : "ies" ) + " and " + filecount + " file" + ( filecount == 1 ? "" : "s" ) );

		if( dircount + filecount == total ) {
			info.put( "success", 1 );
		} else {
			logger.warning( "Some files were not deleted." );
			info.put( "success", 0 );
		}

		return info;
	}


	public Map<String, Integer> swipe( boolean excludeRoot ) throws CleartoolException {
		logger.debug( "Swiping " + this.getViewRoot() );
		Map<String, Integer> sinfo = swipe( viewroot, excludeRoot );
		//Printer.mapPrinter( sinfo );

		return sinfo;
	}
	
	public static SnapshotView get( File viewroot ) throws IOException, ViewException, UnableToInitializeEntityException, CleartoolException {
		String viewtag = getViewtag( viewroot );
		SnapshotView view = null;

		if( UCMView.viewExists( viewtag ) ) {
			view = new SnapshotView( viewroot );
		} else {
			throw new ViewException( "View is not valid", viewroot.getAbsolutePath(), Type.DOES_NOT_EXIST );
		}

		return view;
	}
}
