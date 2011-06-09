package net.praqma.clearcase.ucm.view;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Map;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.util.structure.Tuple;

/**
 * The OO implementation of the ClearCase entity Snapshot view.
 * The next line
 * @author wolfgang
 *
 */
public class SnapshotView extends UCMView
{
	protected static final String rx_view_uuid  = "view_uuid:(.*)";
	
	private File   viewroot   = null;
	private String viewtag    = "";
	private Stream stream     = null;
	private String pvob       = "";
	private String uuid       = "";
	private String globalPath = "";
	
	public enum COMP
	{
		ALL,
		MODIFIABLE
	}
	
	SnapshotView( File viewroot ) throws UCMException
	{
		logger.debug( "Running experimental code." );
		
		/* TODO Test the view root? Does it exist? Is it a directory? */
		
		this.viewroot = viewroot;		
		
		Tuple<Stream, String> t = context.getStreamFromView( viewroot );
				
		this.viewtag  = t.t2;
		this.viewroot = viewroot;
		this.stream   = t.t1;
		this.pvob     = this.stream.getPvob();
	}
	
	/**
	 * Create a Snapshot view given a Stream, view root and a view tag.
	 * @param stream The Stream
	 * @param viewroot
	 * @param viewtag
	 * @return SnapShotView
	 */
	public static SnapshotView Create( Stream stream, File viewroot, String viewtag ) throws UCMException
	{		
		context.makeSnapshotView( stream, viewroot, viewtag );
		
		return new SnapshotView( viewroot );
	}
	
	public static void CreateEnvironment( File viewroot )
	{
		CreateEnvironment( viewroot, "" );
	}
	
	public static void CreateEnvironment( File viewroot, String viewtagsuffix )
	{
		String viewtag = "cool_" + System.getenv( "COMPUTERNAME" ) + "_env" + viewtagsuffix;
		//System.out.println( viewtag );
	}
	
	/**
	 * Swipe the view. Leaving only the ClearCase specific files and folders, deleting view private.
	 * @param excludeRoot Whether to swipe the view root or not
	 */
	public Map Swipe( boolean excludeRoot )
	{
		return context.swipeView( viewroot, excludeRoot );
	}
	
	public static void RegenerateViewDotDat( File dir, String viewtag ) throws UCMException
	{
		context.regenerateViewDotDat( dir, viewtag );
	}
	
	public String GetViewtag()
	{
		return this.viewtag;
	}
	
	public File GetViewRoot()
	{
		return this.viewroot;
	}
	
	public Stream getStream() throws UCMException{
		return context.getStreamFromView(GetViewRoot()).getFirst();
	}
	
	/**
	 * Determine if the views view root is valid, returning its view tag
	 * @return The UUID as a string
	 * @throws UCMException
	 */
	public static String ViewrootIsValid( File view ) throws UCMException
	{
		return context.viewrootIsValid( view );
	}
	
	public class UpdateInfo
	{
		public Integer totalFilesToBeDeleted = 0;
		public boolean success = false;
		public Integer filesDeleted = 0;
		public Integer dirsDeleted = 0;		
	}
	

	/**
	 * 
	 * @param swipe
	 * @param generate
	 * @param overwrite
	 * @param excludeRoot
	 * @param components
	 * @param loadrules
	 * @return A Class of result info, currently only about the swipe. 
	 */
	public UpdateInfo Update( boolean swipe, boolean generate, boolean overwrite, boolean excludeRoot, COMP components, String loadrules ) throws UCMException
	{
		logger.debug( "Updating view: " + components );
		
		if( ( components != null && loadrules != null ) || ( components == null && loadrules == null ) )
		{
			throw new UCMException( "Only one of LOAD RULES and COMPONENTS must be set." );
		}
		
		logger.debug( "components and loadrules" );
		
		String myloadrules = "";
		
		UpdateInfo info = new UpdateInfo();
		
		/* If the components part is set */
		if( components != null )
		{			
			myloadrules = " -add_loadrules ";
			
			if( components == COMP.ALL )
			{
				logger.debug( "COMP=ALL" );
				
				BaselineList bls = this.stream.getLatestBaselines();
				for( Baseline b : bls )
				{
					String rule  = b.getComponent().getRootDir();
					rule         = rule.replaceFirst( "^\\\\", " " );
					myloadrules += rule;
				}
			}
			else
			{
				logger.debug( "COMP=MOD" );
				
				Project project = context.getProjectFromStream( this.stream );
				List<Component> comps = context.getModifiableComponents( project );
				for( Component c : comps )
				{
					String rule = c.getRootDir();
					rule         = rule.replaceFirst( "^\\\\", " " );
					myloadrules += rule;
				}
			}
		}
		
		logger.debug( "COMP DONE" );
		
		// TODO Set the load rules from the applied parameter
		if( loadrules != null )
		{
			loadrules = " -add_loadrules " + loadrules;
		}
		
		logger.debug( "LOAD RULES SET" );

		// TODO generate the streams config spec if required
		if( generate )
		{
			this.stream.generate();
		}
		
		logger.debug( "STREAM GENEREATES" );
		
		if( swipe )
		{
			Map<String, Integer> sinfo = this.Swipe( excludeRoot );
			info.success = sinfo.get( "success" ) == 1 ? true : false;
			info.totalFilesToBeDeleted = sinfo.get( "total" );
			info.dirsDeleted = sinfo.get( "dirs_deleted" );
			info.filesDeleted = sinfo.get( "files_deleted" );
		}
		
		logger.debug( "SWIPED" );
		
		// Cache current directory and chdir into the viewroot
		String result = context.updateView( this, overwrite, myloadrules );
		logger.log( result );
		
		return info;
	}
	
	private void SwipeDir( File dir, FileFilter viewfilter )
	{
		File[] files = dir.listFiles( viewfilter );
		for( File f : files )
		{
			if( f.isDirectory() )
			{
				net.praqma.util.io.IO.deleteDirectory( f );
			}
			else
			{
				f.delete();
			}
		}
	}
	
}
