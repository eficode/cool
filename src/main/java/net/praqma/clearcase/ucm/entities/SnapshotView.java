package net.praqma.clearcase.ucm.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnapshotView extends UCM
{
	protected static final String rx_view_uuid  = "view_uuid:(.*)";
	
	private String viewroot = "";
	private String viewtag  = "";
	private Stream stream   = null;
	private String pvob     = "";
	
	private SnapshotView( String viewroot )
	{
		logger.debug( "Running experimental code." );
		
		this.viewroot = viewroot;		
		
		String cwd = System.getProperty( "user.dir" );
		String newdir = viewroot != null ? viewroot : cwd;
		
		if( !cwd.equals( newdir ) )
		{
			/* Experimental!!! */
			System.setProperty( "user.dir", newdir );
		}
		
		// cleartool("pwv -root");
		String cmd = "pwv -root";
		String wvroot = Cleartool.run( cmd ).trim();
		
		String viewtag = this.ViewrootIsValid( wvroot );
		
		// cleartool( 'lsstream -fmt %Xn -view ' . $viewtag );
		cmd = "lsstream -fmt %Xn -view " + viewtag;
		String fqstreamstr = Cleartool.run( cmd ).trim();
		
		/* Still experimental!!! */
		System.setProperty( "user.dir", cwd );
		
		this.viewtag  = viewtag;
		this.viewroot = viewroot;
		//this.stream   = new Stream( fqstreamstr, false );
		//this.stream   = Stream.GetObject( fqstreamstr, false );
		this.stream   = UCMEntity.GetStream( fqstreamstr, false );
		this.pvob     = this.stream.GetPvob();
	}
	
	
	public static void Create( Stream stream, String viewtag, String viewroot )
	{
		File vroot = new File( viewroot );
		
		if( vroot.exists() )
		{
			DeleteDirectory( vroot );
		}
		
		context.MakeSnapshotView( stream, viewroot, viewtag );
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
		String viewtag = Cleartool.run( cmd ).trim();
		
		return viewtag;
	}
	
	
	public static boolean DeleteDirectory( File directory )
	{
		if ( directory.isDirectory() )
		{
			String[] elements = directory.list();
			for( int i = 0 ;  i < elements.length ; i++ ) 
			{
				boolean success = DeleteDirectory( new File( directory, elements[i] ) ); 
				if ( !success ) return false;
			} 
		} 
		
		return directory.delete();
	}
	
}
