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
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
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
	
	public void Swipe( boolean excludeRoot, Set<String> firstlevel )
	{
		context.SwipeView( viewroot, excludeRoot, firstlevel );
	}
	
	public String GetViewtag()
	{
		return this.viewtag;
	}
	
	public File GetViewRoot()
	{
		return this.viewroot;
	}
	
	public void Update( boolean swipe, boolean generate, boolean overwrite, boolean force, boolean excludeRoot, String components, String loadrules )
	{
		if( ( components != null && loadrules != null ) || ( components == null && loadrules == null ) )
		{
			throw new UCMException( "Only one of LOAD RULES and COMPONENTS must be set." );
		}
		
		if( components != null )
		{
			if( !components.matches( "" ) )
			{
				
			}
		}
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
