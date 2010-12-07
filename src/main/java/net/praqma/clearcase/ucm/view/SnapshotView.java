package net.praqma.clearcase.ucm.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.utils.Tuple;

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
	
	SnapshotView( File viewroot )
	{
		logger.debug( "Running experimental code." );
		
		/* TODO Test the view root? Does it exist? Is it a directory? */
		
		this.viewroot = viewroot;		
		
		Tuple<Stream, String> t = context.GetStreamFromView( viewroot );
				
		this.viewtag  = t.t2;
		this.viewroot = viewroot;
		this.stream   = t.t1;
		this.pvob     = this.stream.GetPvob();
	}
	
	/**
	 * Create a Snapshot view given a Stream, view root and a view tag.
	 * @param stream The Stream
	 * @param viewroot
	 * @param viewtag
	 * @return
	 */
	public static SnapshotView Create( Stream stream, File viewroot, String viewtag )
	{		
		context.MakeSnapshotView( stream, viewroot, viewtag );
		
		return new SnapshotView( viewroot );
	}
	
	public static void CreateEnvironment( File viewroot )
	{
		CreateEnvironment( viewroot, "" );
	}
	
	public static void CreateEnvironment( File viewroot, String viewtagsuffix )
	{
		String viewtag = "cool_" + System.getenv( "COMPUTERNAME" ) + "_env" + viewtagsuffix;
		System.out.println( viewtag );
	}
	
	/**
	 * Swipe the view. Leaving only the ClearCase specific files and folders, deleting view private.
	 * @param excludeRoot Whether to swipe the view root or not
	 */
	public void Swipe( boolean excludeRoot )
	{
		context.SwipeView( viewroot, excludeRoot );
	}
	
	public String GetViewtag()
	{
		return this.viewtag;
	}
	
	public File GetViewRoot()
	{
		return this.viewroot;
	}
	
	/**
	 * Determine if the views view root is valid, returning its view tag
	 * @return The UUID as a string
	 * @throws UCMException
	 */
	public String ViewrootIsValid() throws UCMException
	{
		return context.ViewrootIsValid( this );
	}
	
	/**
	 * Update the view
	 * @param swipe
	 * @param generate
	 * @param overwrite
	 * @param force
	 * @param excludeRoot
	 * @param components
	 * @param loadrules
	 */
	public void Update( boolean swipe, boolean generate, boolean overwrite, boolean excludeRoot, COMP components, String loadrules )
	{
		logger.debug( "Updating view: " + components );
		
		if( ( components != null && loadrules != null ) || ( components == null && loadrules == null ) )
		{
			throw new UCMException( "Only one of LOAD RULES and COMPONENTS must be set." );
		}
		
		String myloadrules = "";
		
		/* If the components part is set */
		if( components != null )
		{			
			myloadrules = " -add_loadrules ";
			
			if( components == COMP.ALL )
			{
				BaselineList bls = this.stream.GetLatestBaselines();
				for( Baseline b : bls )
				{
					String rule  = b.GetComponent().GetRootDir();
					rule         = rule.replaceFirst( "^\\", " " );
					myloadrules += rule;
				}
			}
			else
			{
				Project project = context.GetProjectFromStream( this.stream );
				List<Component> comps = context.GetModifiableComponents( project );
				for( Component c : comps )
				{
					String rule = c.GetRootDir();
					rule         = rule.replaceFirst( "^\\", " " );
					myloadrules += rule;
				}
			}
		}
		
		// TODO Set the load rules from the applied parameter
		if( loadrules != null )
		{
			loadrules = " -add_loadrules " + loadrules;
		}

		// TODO generate the streams config spec if required
		if( generate )
		{
			this.stream.Generate();
		}
		
		if( swipe )
		{
			this.Swipe( excludeRoot );
		}
		
		// Cache current directory and chdir into the viewroot
		String result = context.Cache( this, overwrite, myloadrules );
		logger.log( result );
	}
	
	private void SwipeDir( File dir, FileFilter viewfilter )
	{
		File[] files = dir.listFiles( viewfilter );
		for( File f : files )
		{
			if( f.isDirectory() )
			{
				net.praqma.utils.IO.DeleteDirectory( f );
			}
			else
			{
				f.delete();
			}
		}
	}
	
	

	
}
