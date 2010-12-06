package net.praqma.clearcase.ucm.persistence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.UCMEntity;
import net.praqma.utils.AbnormalProcessTerminationException;
import net.praqma.utils.CmdResult;
import net.praqma.utils.Command;
import net.praqma.utils.Debug;
import net.praqma.utils.IO;
import net.praqma.utils.Tuple;


public class UCMStrategyCleartool implements UCMStrategyInterface
{
	private static Debug logger = Debug.GetLogger();
	
	private static final String rx_ccdef_allowed = "[\\w\\.-_\\\\]";
	
	public static final String __TAG_NAME = "tag";
	
	static
	{
		//logger.ExcludeClass( UCMStrategyXML.class.getName() );
	}
	
	
	private static final String filesep = System.getProperty( "file.separator" );
	private static final String linesep = System.getProperty( "line.separator" );
	
	public UCMStrategyCleartool()
	{
		logger.log( "Using ClearTool strategy" );
	}
	
	
	public void ChangeDirectoryToView( String path )
	{
		logger.trace_function();
		logger.debug( path );
		
		String cwd = System.getProperty( "user.dir" );
		
		if( !cwd.equals( path ) )
		{
			System.setProperty( "user.dir", path );
		}
	}
	
	public void CheckViewContext( File dir )
	{
		logger.trace_function();
		logger.debug( "" );
		
		String cmd = "pwv -root";
		try
		{
			Cleartool.run( cmd, dir ).stdoutBuffer.toString();
		}
		catch( AbnormalProcessTerminationException e )
		{
			if( e.getMessage().equalsIgnoreCase( "cleartool: Error: operation requires a view" ) )
			{
				throw new UCMException( "operation requires a view" );
			}
			
			throw e;
		}
	}
	
	public boolean IsVob( File dir )
	{
		logger.debug( "Testing " + dir );
		
		String cmd = "lsvob \\" + dir.getName();
		try
		{
			Cleartool.run( cmd );
		}
		catch( Exception e )
		{
			logger.debug( "E=" + e.getMessage() );
			return false;
		}
		
		return true;
	}
	
	public List<String> ListVobs( File viewroot )
	{
		logger.debug( "Listing vobs at " + viewroot );
		
		File[] files = viewroot.listFiles();
		List<String> vobs = new ArrayList<String>();
		
		for( File f : files )
		{
			if( f.isDirectory() )
			{
				if( IsVob( f ) )
				{
					vobs.add( f.getName() );
				}
			}
		}
		
		return vobs;
	}
	
	@Override
	public String LoadBaseline( String baseline )
	{
		String cmd = "desc -fmt %n" + delim + "%[component]p" + delim + "%[bl_stream]p" + delim + "%[plevel]p" + delim + "%u " + baseline;
		return Cleartool.run( cmd ).stdoutBuffer.toString();
	}
	
	@Override
	public List<String> GetBaselineDiff( File dir, String baseline, String other, boolean nmerge, String pvob )
	{
		/* Check if we are in view context */
		CheckViewContext( dir );
		
		// cleartool('diffbl -pre -act -ver '.$sw_nmerge.$self->get_fqname );
		String cmd = "diffbl -pre -act -ver -nmerge " + baseline;
		
		try
		{
			return Cleartool.run( cmd, dir ).stdoutList;
		}
		catch( AbnormalProcessTerminationException e )
		{
			if( e.getMessage().equalsIgnoreCase( "cleartool: Error: The -nmerge option requires that both baselines be from the same stream." ) )
			{
				logger.log( "The given Baseline, \"" + baseline + "\" is the first on the Stream" );
				
				List<String> result = new ArrayList<String>();
				//result.add( ">> no_activity@\\Cool_PVOB \"FAKE ACTIVITY\"" );
				result.add( ">> no_activity@" + pvob + " \"NO ACTIVITY\"" );
				
				List<String> vobs = ListVobs( dir );
				
				for( String vob : vobs )
				{
					List<String> files = Cleartool.run( "ls -s -rec " + vob, dir ).stdoutList;
					
					/* Remove lost + found folder */
					for( int i = 0 ; i < files.size() ; i++ )
					{
						if( !files.get( i ).matches( "^lost+found@@.*" ) )
						{
							//files.remove( i );
							result.add( dir + filesep + files.get( i ) );
						}
					}
				}
				
				return result;
			}
			
			/* The exception could not be handled! */
			throw e;
		}
	}
	
	@Override
	public void SetPromotionLevel( String baseline, String plevel )
	{
		// cleartool( 'chbl -level ' . $plevel . ' ' . $self->get_fqname() )
		String cmd = "chbl -level " + plevel + " " + baseline;
		Cleartool.run( cmd );
	}
	
	@Override
	public String GetBaselineActivities( String baseline )
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<String> GetBaselines( String component, String stream, String plevel )
	{
		// my @retval = cleartool_qx(' lsbl -s -component '.$self->get_fqname().' -stream '.$stream->get_fqname().' -level '.$params{'plevel'});
		String cmd = "lsbl -s -component " + component + " -stream " + stream + " -level " + plevel;
		return Cleartool.run( cmd ).stdoutList;
	}
	@Override
	public void RecommendBaseline( String stream, String baseline ) throws UCMException
	{
		// "chstream " . $comment . " -recommend " . $baseline->get_fqname() . ' ' . $self->get_fqname();
		String cmd = "chstream -recommend " + baseline + " " + stream;
		Cleartool.run( cmd );
		
	}
	@Override
	public String GetRecommendedBaselines( String stream )
	{
		// cleartool( 'desc -fmt %[rec_bls]p stream:' . $self->{'fqstream'} );
		String cmd = "desc -fmt %[rec_bls]p " + stream;
		return Cleartool.run( cmd ).stdoutBuffer.toString();
	}
	@Override
	public String GetVersion( String version, String separator )
	{
		// 'desc -fmt [date:%d]\n[user:%u]\n[machine:%h]\n[comment:%c]\n[checkedout:%Rf]\n[kind:%m]\n[branch:%Vn]\n[xname:%Xn]\n ' . $self->{'fqpname'};
		String cmd = "desc -fmt %d" + separator + "%u" + separator + "%h" + separator + "%c" + separator + "%Rf" + separator + "%m" + separator + "%Vn" + separator + "%Xn " + version;
		return Cleartool.run( cmd ).stdoutBuffer.toString();
	}
	
	/*
wolles_baseline_02.6448
  Hyperlinks:
    tag@377@\Cool_PVOB ->  "test"
    tag@394@\Cool_PVOB ->  "test nummer 2"
    
    describe -ahlink tag -l
	 */
	
	private static final Pattern pattern_tags = Pattern.compile( "^\\s*(tag@\\d+@" + rx_ccdef_allowed + "+)\\s*->\\s*\"(.*?)\"\\s*$" );
	
	@Override
	public List<Tuple<String, String>> GetTags( String fqname )
	{
		logger.trace_function();
		logger.debug( fqname );
		
		String cmd = "describe -ahlink " + __TAG_NAME + " -l " + fqname;
		List<String> list = Cleartool.run( cmd ).stdoutList;
		
		List<Tuple<String, String>> tags = new ArrayList<Tuple<String, String>>();
				
		/* There are tags */
		if( list.size() > 2 )
		{
			for( int i = 2 ; i < list.size() ; i++ )
			{
				logger.debug( "["+i+"]" + list.get( i ) );
				Matcher match = pattern_tags.matcher( list.get( i ) );
				if( match.find() )
				{
					tags.add( new Tuple<String, String>( match.group( 1 ), match.group( 2 ) ) );
				}
			}
		}
		
		return tags;
	}
	
	@Override
	public String GetTag( String fqname )
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	private static final Pattern pattern_remove_verbose_tag = Pattern.compile( "^.*?\"(.*)\".*?$" );
	
	@Override
	public String NewTag( UCMEntity entity, String cgi )
	{
		logger.trace_function();
		logger.debug( entity.GetFQName() );
		
		// mkhlink -ttext "test nummer 2" tag baseline:wolles_baseline_02.6448@\Cool_PVOB
		//String cmd = "mkhlink -ttext \"" + cgi + "\" tag " + entity.GetFQName();
		String cmd = "mkhlink -ttext \"" + cgi + "\" " + __TAG_NAME + " " + entity.GetFQName();
		String tag = Cleartool.run( cmd ).stdoutBuffer.toString();
		Matcher match = pattern_remove_verbose_tag.matcher( tag );
		if( match.find() )
		{
			return match.group( 1 );
		}
		
		return "";
	}
	
	@Override
	public void DeleteTag( String fqname )
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void DeleteTagsWithID( String tagType, String tagID, String entity )
	{
		logger.trace_function();
		logger.debug( tagType + tagID );
		
		List<Tuple<String, String>> list = GetTags( entity );
		logger.debug( list.size() + " Tags!" );
		
		for( Tuple<String, String> t : list )
		{
			logger.debug( "Testing " + t.t1 + " > " + t.t2 );
			if( t.t2.matches( "^.*tagtype=" + tagType + ".*$" ) && t.t2.matches( "^.*tagid=" + tagID + ".*$" ) )
			{
				String cmd = "rmhlink " + t.t1;
				Cleartool.run( cmd );
			}
		}
		
	}
	@Override
	public String PutTag( String fqname, String keyval, UCMEntity entity )
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<String> GetLatestBaselines( String stream )
	{
		String cmd = "desc -fmt %[latest_bls]p stream:" + stream;
		String[] t = Cleartool.run( cmd ).stdoutBuffer.toString().split( " " );
		List<String> bls = new ArrayList<String>();
		for( String s : t )
		{
			if( s.matches( "\\S+" ) )
			{
				bls.add( s );
			}
		}
		
		return bls;
	}
	
	@Override
	public void MakeSnapshotView( String stream, File viewroot, String viewtag )
	{
		if( viewroot.exists() )
		{
			IO.DeleteDirectory( viewroot );
		}
		
	}
	
	private static final Pattern pattern_view_uuid = Pattern.compile( "^.*?View uuid: ([\\w\\.:]+).*?$" );
	
	public void RegenerateViewDotDat( File dir, String viewtag ) throws IOException
	{
		logger.trace_function();
		logger.debug( dir + ", " +  viewtag );
		
		File viewdat = new File( dir, "view.dat" );
		
		String cmd = "lsview -l " + viewtag;
		/* TODO Check this functions behavior, if the view doesn't exist */
		List<String> result = Cleartool.run( cmd ).stdoutList;
		
		Matcher match = pattern_view_uuid.matcher( result.get( 8 ) );
		if( !match.find() )
		{
			logger.warning( "The UUID of the view " + viewtag + " does not exist!" );
			throw new UCMException( "The UUID of the view " + viewtag + " does not exist!" );
		}
		
		String uuid = match.group( 1 );
		
		cmd = "lsview -uuid " + uuid;
		
		try
		{
			Cleartool.run( cmd );
		}
		catch( AbnormalProcessTerminationException e )
		{
			throw new UCMException( "Unable to read the UUID(" + uuid + ") from view tag " + viewtag );
		}
		
		if( dir.exists() )
		{
			logger.warning( "The view root, " + dir + ",  already exists - reuse may be problematic" );
		}
		else
		{
			dir.mkdir();
		}
		
		FileOutputStream fos = new FileOutputStream( viewdat );
		fos.write( ( "ws_oid:00000000000000000000000000000000 view_uuid:" + uuid ).getBytes() );
		fos.close();
		
		// cmd("attrib +h +r $view_dat_pn");
		cmd = "attrib +h +r " + viewdat;
		Command.run( cmd );
	}
	
	public boolean ViewExists( String viewtag )
	{
		logger.trace_function();
		logger.debug( viewtag );
		
		String cmd = "lsview " + viewtag;
		
		try
		{
			Cleartool.run( cmd );
			return true;
		}
		catch( AbnormalProcessTerminationException e )
		{
			return false;
		}
	}
	
	private final String rx_co_file = ".*CHECKEDOUT$";
	private final String rx_ctr_file = ".*\\.contrib";
	private final String rx_keep_file = ".*\\.keep$";
	
	public boolean SwipeView( File viewroot, boolean excludeRoot )
	{
		logger.debug( viewroot.toString() );
		
		List<String> vobs = ListVobs( viewroot );
		
		File[] files = viewroot.listFiles();
		//List<File> fls = new ArrayList<File>();
		String fls = "";
		List<File> other = new ArrayList<File>();
		List<File> root = new ArrayList<File>();
		
		for( File f : files )
		{
			logger.debug( "Checking: " + f );
			
			if( !f.canWrite() )
			{
				logger.debug( "Write protected." );
				continue;
			}
			
			if( f.isDirectory() )
			{
				if( IsVob( f ) )
				{
					fls += f.getAbsolutePath() + " ";
				}
				else
				{
					other.add( f );
				}
			}
			else
			{
				if( f.getName().equalsIgnoreCase( "view.dat" ) )
				{
					continue;
				}
				root.add( f );
			}
		}
		
		/* Remove all other dirs */
		for( File f : other )
		{
			logger.log( "Removing " + f );
			net.praqma.utils.IO.DeleteDirectory( f );
		}
		
		String cmd = "ls -short -recurse -view_only " + fls;
		List<String> result = Cleartool.run( cmd ).stdoutList;
		List<File> rnew   = new ArrayList<File>();
		
		if( !excludeRoot )
		{
			rnew.addAll( root );
		}
		
		int total = result.size() + rnew.size();
		
		//for( int i = 0 ; i < result.size() ; i++ )
		for( String s : result )
		{
			logger.debug( s );
			
			/* Speedy, because of lazy evaluation */
			if( s.matches( rx_co_file ) || s.matches( rx_keep_file ) || s.matches( rx_ctr_file ) )
			{
				continue;
			}
			
			rnew.add( new File( s ) );
		}
		
		logger.debug( "Found " + total + " files, of which " + ( total - rnew.size() ) + " were CO, CTR or KEEP's." );
		
		List<File> dirs = new ArrayList<File>();
		int dircount    = 0;
		int filecount   = 0;
		
		/* Removing view private files, saving directories for later */
		for( File f : rnew )
		{
			logger.debug( "FILE=" + f );
			
			if( f.exists() )
			{
				if( f.isDirectory() )
				{
					dirs.add( f );
				}
				else
				{
					logger.debug( "Deleting " + f );
					f.delete();
					filecount++;
				}
			}
			else
			{
				logger.debug( "The file " + f + " does not exist." );
			}
		}
		
		/* TODO Remove the directories, somehow!? Only the empty!? */
		for( File d : dirs )
		{
			try
			{
				d.delete();
				dircount++;
			} 
			catch( SecurityException e )
			{
				logger.log( "Unable to delete \"" + d + "\". Probably not empty." );
			}
		}
		
		logger.print( "Deleted " + dircount + " director" + ( dircount == 1 ? "y" : "ies" ) + " and " + filecount + " file" + ( filecount == 1 ? "" : "s" ) );
		
		if( dircount + filecount == total )
		{
			return true;
		}
		else
		{
			logger.warning( "Some files were not deleted." );
			return false;
		}
	}
	
	@Override
	public String GetXML()
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void SaveState()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String GetStreamFromView( String viewtag )
	{
		String fqstreamstr =  Cleartool.run( "lsstream -fmt %Xn -view " + viewtag ).stdoutBuffer.toString();

		return fqstreamstr;
	}

	@Override
	public File GetCurrentViewRoot( File viewroot )
	{
		logger.trace_function();
		logger.debug( viewroot.getAbsolutePath() );
		
		String wvroot = Cleartool.run( "pwv -root", viewroot ).stdoutBuffer.toString();

		return new File( wvroot );
	}
	
	protected static final Pattern rx_view_uuid  = Pattern.compile( "view_uuid:(.*)" );
	
	public String ViewrootIsValid( File viewroot )
	{
		logger.debug( viewroot.getAbsolutePath() );
		
		//viewroot. 
		//String viewdotdatpname = viewroot + filesep + "view.dat";
		File viewdotdatpname = new File( viewroot, "view.dat" );
		
		logger.debug( "The view file = " + viewdotdatpname );
		
		FileReader fr = null;
		try
		{
			fr = new FileReader( viewdotdatpname );
		}
		catch ( FileNotFoundException e1 )
		{
			logger.warning( "\"" + viewdotdatpname + "\" not found!" );
			throw new UCMException( e1.getMessage() );
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
			throw new UCMException( e.getMessage() );
		}
		
		logger.debug( "FILE CONTENT=" + result.toString() );
		
		Matcher match = rx_view_uuid.matcher( result.toString() );
		
		String uuid = "";
		
		if( match.find() )
		{
			/* A match is found */
			uuid = match.group( 1 ).trim();
		}
		else
		{
			logger.log( "UUID not found!", "warning" );
			throw new UCMException( "UUID not found" );
		}
		
		//my $viewtag = cleartool("lsview -s -uuid $1");
		String cmd = "lsview -s -uuid " + uuid;
		String viewtag = Cleartool.run( cmd ).stdoutBuffer.toString().trim();
		
		return viewtag;
	}

	@Override
	public void CreateStream( String pstream, String nstream, boolean readonly, String baseline )
	{
		// "mkstream $c $baseline $readonly -in stream:" . $params{'parent_stream'}->get_fqname() . " " . $stream_fqname );
		String cmd = "mkstream " + ( baseline != null ? baseline + " " : "" ) + ( readonly ? " -readonly " : "" ) + " -in stream " + pstream + " "  + nstream;
		Cleartool.run( cmd );
	}
	
	public void Generate( String stream )
	{
		//cleartool( 'chstream -generate ' . $self->get_fqname );
		String cmd = "chstream -generate " + stream;
		Cleartool.run( cmd );
	}
	
	public boolean StreamExists( String fqname )
	{
		String cmd = "describe " + fqname;
		try
		{
			Cleartool.run( cmd );
			return true;
		}
		catch( AbnormalProcessTerminationException e )
		{
			return false;
		}
	}
	
	public boolean RebaseStream( String viewtag, String stream, String baseline, boolean complete )
	{
		logger.debug( "Rebasing " + viewtag );
		
		String cmd = "rebase " + ( complete ? "-complete " : "" ) + " -force -view " + viewtag + " -stream " + stream + " -baseline " + baseline;
		CmdResult res = Cleartool.run( cmd );

		if( res.stdoutBuffer.toString().matches( "^No rebase needed.*" ) )
		{
			return false;
		}
		
		return true;
	}
	
	private final String rx_rebase_in_progress = "^Rebase operation in progress on stream";
	
	public boolean IsRebaseInProgress( String stream )
	{
		//my ($rebase_in_progress) = grep( /^Rebase operation in progress on stream/, cleartool_qx( 'rebase -status -stream ' . $self->get_fqname ) );
		String cmd = "rebase -status -stream " + stream;
		String result = Cleartool.run( cmd ).stdoutBuffer.toString();
		if( result.matches( rx_rebase_in_progress ) )
		{
			return true;
		}
			
		return false;
	}
	
	public void CancelRebase( String stream )
	{
		// cleartool( 'rebase -cancel -force -stream ' . $self->get_fqname );
		String cmd = "rebase -cancel -force -stream " + stream;
		Cleartool.run( cmd );
	}


	@Override
	public String GetRootDir( String component )
	{
		logger.debug( component );
		
		//cleartool( "desc -fmt %[root_dir]p " . $self->get_fqname() );
		String cmd = "desc -fmt %[root_dir]p " + component;
		return Cleartool.run( cmd ).stdoutBuffer.toString();
	}
	
	
	public String GetProjectFromStream( String stream )
	{
		String cmd = "desc -fmt %[project]p stream: " + stream;
		return Cleartool.run( cmd ).stdoutBuffer.toString().trim();
	}
	
	public List<String> GetModifiableComponents( String project )
	{
		String cmd = "desc -fmt %[mod_comps]p project: " + project;
		return Arrays.asList( Cleartool.run( cmd ).stdoutBuffer.toString().split( "\\s+" ) );
	}
	
	
}