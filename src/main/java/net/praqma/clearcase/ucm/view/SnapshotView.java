package net.praqma.clearcase.ucm.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.utils.Tuple;

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
		
		this.viewroot = viewroot;		
		
//		String cwd    = System.getProperty( "user.dir" );
//		String newdir = viewroot != null ? viewroot : cwd;
//		
//		if( !cwd.equals( newdir ) )
//		{
//			/* Experimental!!! */
//			System.setProperty( "user.dir", newdir );
//		}
//		
//		/* TODO vwroot and viewroot are the same! Right? */
//		// cleartool("pwv -root");
//		String cmd = "pwv -root";
//		String wvroot = Cleartool.run_collapse( cmd ).trim();
//		
//		String viewtag = SnapshotView.ViewrootIsValid( wvroot );
//		
//		// cleartool( 'lsstream -fmt %Xn -view ' . $viewtag );
//		//cmd = "lsstream -fmt %Xn -view " + viewtag;
//		//String fqstreamstr = Cleartool.run_collapse( cmd ).trim();
//		String fqstreamstr = context.GetStreamFromView( viewtag );
//		
//		/* Still experimental!!! */
//		System.setProperty( "user.dir", cwd );
		
		Tuple<Stream, String> t = context.GetStreamFromView( viewroot );
				
		this.viewtag  = t.t2;
		this.viewroot = viewroot;
		this.stream   = t.t1;
		this.pvob     = this.stream.GetPvob();
	}
	
	
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
	
	
	
	
	public static String ViewrootIsValid( String viewroot )
	{
		logger.trace_function();
		
		String viewdotdatpname = viewroot + filesep + "view.dat";
		
		FileReader fr = null;
		try
		{
			fr = new FileReader( viewdotdatpname );
		}
		catch ( FileNotFoundException e1 )
		{
			logger.warning( "\"" + viewdotdatpname + "\" not found!" );
			return null;
		}
		
		BufferedReader br = new BufferedReader( fr );
		String line;
		StringBuffer result = new StringBuffer();
		try
		{
			while( ( line = br.readLine() ) != null )
			{
				result.append( line );
			}
		}
		catch ( IOException e )
		{
			logger.warning( "Couldn't read lines from " + viewdotdatpname );
			result.append( "" );
		}
		
		Pattern pattern = Pattern.compile( rx_view_uuid );
		Matcher match   = pattern.matcher( result.toString() );
		
		/* A match is found */
		String uuid = "";
		try
		{
			uuid = match.group( 1 ).trim();
		}
		catch( IllegalStateException e )
		{
			logger.log( "UUID not found!", "warning" );
			return null;
		}
		
		//my $viewtag = cleartool("lsview -s -uuid $1");
		String cmd = "lsview -s -uuid " + uuid;
		String viewtag = Cleartool.run_collapse( cmd ).trim();
		
		return viewtag;
	}
	
	

	
}
