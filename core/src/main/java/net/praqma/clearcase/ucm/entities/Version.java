package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.changeset.ChangeSet2;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

public class Version extends UCMEntity implements Comparable<Version> {
	
	private static final Pattern rx_extendedName = Pattern.compile( "^(?:(" + rx_ccdef_filename + "+)@@)(?:(" + rx_ccdef_filename + "+)@@)?(.+)$" );
	private static final Pattern rx_checkExistence = Pattern.compile( ".*?Entry named \".*\" already exists.*?" );
	private static final Pattern rx_versionName = Pattern.compile( "^(\\S+)\\s+([\\S\\s.^@]+@@.*)$" );
	
	transient private static Logger logger = Logger.getLogger();
	
	//private String date = null;
	private String user = null;
	private String machine = null;
	private boolean checkedout = false;
	private String comment = null;
	private String branch = null;
	
	private boolean oldVersion = false;
	private File oldFile;

	private File view = null;

	private String fullfile = null;
	private String sfile = null;
	private File file = null;
	private String version = "";
	
	private Integer revision = 0;

	private static String rx_revision = "(\\d+)$";
	private static Pattern p_revision = Pattern.compile( "@@(.*)$" );
	
	public enum Status {
		UNCHANGED,
		CHANGED,
		ADDED,
		DELETED
	}
	
	private Status status = Status.UNCHANGED;

	Version() {
		super( "version" );
	}

	private static final Pattern rx_findAddedElements = Pattern.compile( qfs + ".*?" + qfs + "(\\d+)" + qfs + "(.*?)" + qfs );
	private static final Pattern rx_findRevision = Pattern.compile( qfs + "(\\d+)$" );

	@Override
	protected void initialize() {
		Matcher match = pattern_version_fqname.matcher( fqname );
		if( match.find() ) {
			/* Set the Entity variables */
			shortname = match.group( 1 );
			pvob = new PVob( match.group( 2 ) );
		}
		
		String fqname = "";
		if( Cool.getOS().equals( OperatingSystem.WINDOWS ) ) {
			fqname = this.fqname.matches( "^\\S:\\\\.*" ) ? this.fqname : System.getProperty( "user.dir" ) + filesep + this.fqname;
		} else {
			fqname = this.fqname.startsWith( "/" ) ? this.fqname : System.getProperty( "user.dir" ) + filesep + this.fqname;
		}

		this.fqname = fqname;

		/* Find the revision number, make it zero if it is not found */
		Matcher m = p_revision.matcher( this.fqname );
		if( m.find() ) {
			// this.revision = Integer.parseInt( m.group( 1 ) );
			this.version = m.group( 1 );
		} else {
			this.version = "0";
		}
		
		String tmp = this.fqname;
		tmp = tmp.replaceFirst( "(?m)@@.*$", "" );
		tmp = tmp.replaceFirst( "(?m)^\\s+", "" );
		this.fullfile = tmp.trim();
		
		/* Check if this is a newly added element
		 * Ie this is only shown as a parent folder change 
		 *  view\MonKit006\MonKit006\src@@\main\monkit006_1_dev\2\test\main\monkit006_1_dev\1\java\main\monkit006_1_dev\1
		 * */
		this.status = Status.CHANGED;
		Matcher ma = rx_findAddedElements.matcher( version );
		while( ma.find() ) {
			this.fullfile += filesep + ma.group(2);
		}

		this.file = new File( this.fullfile );
		
		Matcher r = rx_findRevision.matcher( fqname );
		if( r.find() ) {
			this.revision = Integer.parseInt( r.group(1) );
			if( this.revision == 1 ) {
				this.status = Status.ADDED;
			}
		}
	}

	public boolean hijack() {
		if( this.file.canWrite() ) {
			return true;
		}

		return this.file.setWritable( true );
	}

	/* Getters */

	public static Version getUnextendedVersion( File file, File viewroot ) throws IOException, CleartoolException, UnableToLoadEntityException, UCMEntityNotFoundException, UnableToInitializeEntityException {
		//return context.getVersionExtension( file, viewroot );
		
		if( !file.exists() ) {
			throw new IOException( "The file " + file + " does not exist." );
		}

		String cmd = "desc -fmt %Xn " + file;
		String f = "";
		try {
			CmdResult r = Cleartool.run( cmd, viewroot );
			f = r.stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to get extended version: " + e.getMessage(), e );
		}
		
		return (Version) UCMEntity.getEntity( Version.class, f ).load();
	}

	public String blame() throws UCMEntityNotFoundException, UnableToCreateEntityException, UnableToGetEntityException, UnableToLoadEntityException {
		return this.getUser();
	}

	public String getVersion() throws UnableToLoadEntityException {
		if( !loaded ) load();

		return this.version;
	}

	
	public Version load() throws UnableToLoadEntityException {
		try {
			String cmd = "describe -fmt %u\\n%Vn\\n%Xn\\n%[object_kind]p \"" + this + "\"";
			List<String> list = Cleartool.run( cmd ).stdoutList;

			/* First line, user */
			setUser( list.get( 0 ) );

			/* Second line, version name */
			String vn = list.get( 1 );

			/* Third line, version extended name */
			String ven = list.get( 2 );
			Matcher m = rx_extendedName.matcher( ven );

			if( list.get( 3 ).equals( "file element" ) ) {
				setKind( Kind.FILE_ELEMENT );
			} else if( list.get( 3 ).equals( "directory version" ) ) {
				setKind( Kind.DIRECTORY_ELEMENT );
			}

		} catch( Exception e ) {
			//throw new UCMException( "Could not load Version: " + e.getMessage() );
			throw new UnableToLoadEntityException( this, e );
		}
		
		return this;
	}
	
	public static Version create( File file, boolean mkdir, SnapshotView view ) throws CleartoolException, IOException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException, UnableToLoadEntityException, UnableToInitializeEntityException {

		Version.addToSourceControl( file, mkdir, view.getViewRoot() );
		
		Version version = Version.getUnextendedVersion( file, view.getViewRoot() );
		version.setView( view );
		
		return version;
	}
	
	/**
	 * Create a ClearCase element from a File, that will be checked in
	 * @param file - The relative file
	 * @param viewContext - The view root
	 * @return
	 * @throws ClearCaseException
	 * @throws IOException
	 */
	public static Version create( File file, File viewContext ) throws ClearCaseException, IOException {

		Version.addToSourceControl( file, viewContext, null, true );
		
		Version version = Version.getUnextendedVersion( file, viewContext );
		version.setView( viewContext );
		
		return version;
	}
	
	public static void makeElement( File file, File view, String comment ) throws CleartoolException {
		String cmd = "mkelem " + ( comment != null ? "-c \"" + comment + "\" " : "" ) + file;
		
		try {
			Cleartool.run( cmd, view );
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to make element " + file, e );
		}
	}
	
	public static void makeDirectory( File directory, File view, String comment ) throws CleartoolException {
		String cmd = "mkdir " + ( comment != null ? "-c \"" + comment + "\" " : "" ) + directory;
		
		try {
			Cleartool.run( cmd, view );
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to make directory " + directory, e );
		}
	}
	
	/**
	 * 
	 * @param file
	 * @param view
	 * @param viewContext
	 * @param checkIn
	 * @throws CleartoolException
	 */
	public static void addToSourceControl( File file, File viewContext, String comment, boolean checkIn ) throws CleartoolException {
		String cmd = "mkelem -mkpath ";
		cmd += checkIn ? "-ci " : " ";
		cmd += comment != null ? "-comment \"" + comment + "\" " : "-nc ";
		cmd += file;
		
		try {
			Cleartool.run( cmd, viewContext );
		} catch( Exception e ) {
			throw new CleartoolException( "Could not add " + file + " to source control", e );
		}
	}
	
	public static void addToSourceControl( File file, boolean mkdir, File view ) throws CleartoolException {
			/* Check existence */
			List<File> files = new ArrayList<File>();
			File parent = file.getParentFile();
			logger.debug( "FILE  : " + file );
			logger.debug( "PARENT: " + parent );
			while( !parent.equals( view ) ) {
				files.add( parent );
				parent = parent.getParentFile();
			}

			for( int i = files.size() - 1; i >= 0; i-- ) {
				String cmd = "mkdir " + files.get( i ).getPath();
				try {
					/* The parent must be checked out before adding elements */
					try {
						checkOut( files.get( i ).getParentFile(), view );
					} catch( CleartoolException e ) {
						/* This probably indicates the directory is checked out */
					}
					Cleartool.run( cmd, view );
				} catch( Exception e ) {
				}
			}

			try {
				/* Check out the folder */
				try {
					checkOut( file.getParentFile(), view );
				} catch( CleartoolException e ) {
					/* Maybe it is checked out? */
				}

				/* Determine whether the File is a file or a directory */
				String cmd = "";
				if( mkdir ) {
					cmd = "mkdir -nc " + file;
				} else {
					cmd = "mkelem -nc " + file;
				}
				Cleartool.run( cmd, view );
			} catch( Exception e ) {
				/* Already added to source control */
				logger.debug( "---->" + e.getMessage() );
				Matcher m = rx_checkExistence.matcher( e.getMessage() );
				if( m.find() ) {
					logger.debug( file + " already added to source control" );
					return;
				}

				throw new CleartoolException( "Could not add " + file + " to source control", e );
			}

		}
	
	public void checkIn() throws CleartoolException {
		//context.checkIn( this, false, view.getViewRoot() );
		checkIn( file, false, view );
	}
	
	public static void checkIn( File file, boolean identical, File viewContext ) throws CleartoolException {
		checkIn( file, identical, viewContext, null );
	}
	
	public static void checkIn( File file, boolean identical, File viewContext, String comment ) throws CleartoolException {
		try {
			String cmd = "checkin " + ( comment != null ? "-c \"" + comment + "\" " : "-nc " ) + ( identical ? "-identical " : "" ) + file;
			Cleartool.run( cmd, viewContext, true, false );
		} catch( Exception e ) {
			if( e.getMessage().matches( "(?s).*By default, won't create version with data identical to predecessor.*" ) ) {
				logger.debug( "Identical version, trying to uncheckout" );
				uncheckout( file, false, viewContext );
				return;
			} else {
				throw new CleartoolException( "Could not check in", e );
			}

		}
	}
	
	public void checkIn( boolean identical ) throws CleartoolException {
		checkIn( this.getFile(), identical, view );
	}
	
	public void checkInIdentical() throws CleartoolException {
		checkIn( this.getFile(), true, view );
	}
	
	public void checkOut() throws CleartoolException {
		checkOut( this.getFile(), view );
	}
	
	public static void checkOut( File file, File context ) throws CleartoolException {
		checkOut( file, context, null );
	}
	
	public static void checkOut( File file, File context, String comment ) throws CleartoolException {
		try {
			String cmd = "checkout " + ( comment != null ? "-c \"" + comment + "\" " : "-nc " ) + file;
			Cleartool.run( cmd, context );
		} catch( Exception e ) {
			throw new CleartoolException( "Could not check out " + file, e );
		}
	}
	
	public void removeVersion() throws CleartoolException {
		removeVersion( this.file, view );
	}
	
	public static void removeVersion( File file, File viewContext ) throws CleartoolException {
		/* Firstly, checkout directory */
		try {
			checkOut( file.getParentFile(), viewContext );
		} catch( CleartoolException e ) {
			/*
			 * The file is probably already checked out, let's try to continue
			 */
		}

		String cmd = "rmver -force -xlabel -xattr -xhlink " + file;

		try {
			uncheckout( file, false, viewContext );
		} catch( CleartoolException e ) {
			/* Could not uncheckout */
			logger.warning( "Could not uncheckout " + file );
		}

		try {
			Cleartool.run( cmd, viewContext );
		} catch( Exception e ) {
			throw new CleartoolException( "Could not remove " + file + ": " + e.getMessage(), e );
		}
	}
	
	public void removeName( ) throws CleartoolException {
		removeName( this.file, view );
	}
	
	public static void removeName( File file, File context ) throws CleartoolException {
		/* Firstly, checkout directory */
		try {
			checkOut( file.getParentFile(), context );
		} catch( CleartoolException e ) {
			/*
			 * The file is probably already checked out, let's try to continue
			 */
		}

		try {
			uncheckout( file, false, context );
		} catch( CleartoolException e ) {
			/* Could not uncheckout */
			logger.debug( "Could not uncheckout " + file );
		}

		try {
			// String cmd = "rmname -force " + ( checkedOut ? "" : "-nco " ) +
			// file;
			String cmd = "rmname -force -nco " + file;
			Cleartool.run( cmd, context );
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to remove name " + file + " at " + context, e );
		}
	}
	
	public static void moveFile( File file, File destination, File context ) throws CleartoolException {
		try {
			try {
				checkOut( file.getParentFile(), context );
			} catch( CleartoolException e ) {
				/* Directory could be checked out already, let's proceed */
			}

			/*
			 * If destination is a directory and NOT the same as the source,
			 * let's try to check it out
			 */
			if( destination.isDirectory() && !file.getParentFile().equals( destination ) ) {
				try {
					checkOut( destination, context );
				} catch( CleartoolException e ) {
					/* Directory could be checked out already, let's proceed */
				}
				/*
				 * If destination is a file and its directory is NOT the same as
				 * the source, then try to checkout the directory
				 */
			} else if( destination.isFile() && !destination.getParentFile().equals( file.getParentFile() ) ) {
				try {
					checkOut( destination.getParentFile(), context );
				} catch( CleartoolException e ) {
					/* Directory could be checked out already, let's proceed */
				}
			}

			String cmd = "mv " + file + " " + destination;
			Cleartool.run( cmd, context );
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to move " + file + " to " + destination, e );
		}
	}
	
	public void moveFile( File destination ) throws CleartoolException {
		moveFile( file, destination, view );
	}
	
	public void uncheckout() throws CleartoolException {
		uncheckout( this.getFile(), true, view );
	}
	
	public void uncheckout( boolean keep ) throws CleartoolException {
		uncheckout( this.getFile(), keep, view );
	}
	
	public static void uncheckout( File file, boolean keep, File viewContext ) throws CleartoolException {
		try {
			String cmd = "uncheckout -rm " + ( keep ? "-rm " : "" ) + file;
			Cleartool.run( cmd, viewContext );
		} catch( Exception e ) {
			throw new CleartoolException( "Could not uncheck out", e );
		}
	}
	
	public static void recursiveCheckin( File path ) {
		try {
			List<File> files = Version.getUncheckedIn( path );
			for( File f : files ) {
				logger.debug( "Checking in " + f );
				try {
					Version.checkIn( f, false, path );
				} catch( CleartoolException e1 ) {
					logger.debug( "Unable to checkin " + f );
					/* No op */
				}
			}
		} catch( CleartoolException e1 ) {
			logger.error( e1.getMessage() );				
		}
	}

	public void setView( SnapshotView view ) {
		this.view = view.getViewRoot();
	}
	
	public void setView( File view ) {
		this.view = view;
	}
	
	public File getView() {
		return view;
	}

	public void setSFile( String sfile ) {
		this.sfile = sfile;
	}

	public String getSFile() {
		return sfile;
	}
	
	public void setFile( File file ) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setStatus( Status status ) {
		this.status = status;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public Integer getRevision() {
		return this.revision;
	}
	
	public static List<File> getUncheckedIn( File viewContext ) throws CleartoolException {
		List<File> files = new ArrayList<File>();

		try {
			File[] vobs = viewContext.listFiles();
			for( File vob : vobs ) {
				logger.debug( "Checking " + vob );
				if( !vob.isDirectory() || vob.getName().matches( "^\\.{1,2}$" ) ) {
					continue;
				}
				logger.debug( vob + " is a valid vob" );

				String cmd = "lsco -s -r";
				List<String> list = Cleartool.run( cmd, vob ).stdoutList;

				for( String s : list ) {
					files.add( new File( vob, s ) );
				}
			}

			return files;

		} catch( Exception e ) {
			throw new CleartoolException( "Could not retreive files", e );
		}
	}
	
	public boolean isDirectory() throws UnableToLoadEntityException {
		if( !loaded ) load();
		return kind.equals( Kind.DIRECTORY_ELEMENT );
	}
	
	public boolean isFile() throws UnableToLoadEntityException {
		if( !loaded ) load();
		return kind.equals( Kind.FILE_ELEMENT );
	}

	
	/**
	 * An exception safe way to determine whether the file is under
	 * source control
	 * @param element The File to be checked
	 * @param viewContext The view context as a File path
	 * @return True if the File element is under source control
	 * @throws CleartoolException 
	 */
	public static boolean isUnderSourceControl( File element, File viewContext ) throws CleartoolException {
		String cmd = "describe " + element;
		try {
			String line = Cleartool.run( cmd, viewContext ).stdoutBuffer.toString();

			if( line.contains( "View private file" ) ) {
				return false;
			} else {
				return true;
			}
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to determine whether " + element + " is under source control or not", e );
		}
	}
	
	/**
	 * An exception safe way to determine whether the file is checked out
	 * @param element The File to be checked
	 * @param viewContext The view context as a File path
	 * @return True if the File element is checked out
	 * @throws CleartoolException 
	 */
	public static boolean isCheckedout( File element, File viewContext ) throws CleartoolException {
		String cmd = "describe -s " + element;
		try {
			String line = Cleartool.run( cmd, viewContext ).stdoutBuffer.toString();

			if( line.endsWith( "\\CHECKEDOUT" ) ) {
				return true;
			} else {
				return false;
			}
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to determine whether " + element + " is checked out or not", e );
		}
	}
	
	public void setOldFile( File oldFile ) {
		this.oldFile = oldFile;
	}
	
	public boolean isMoved() {
		return ( oldFile != null );
	}

	public String stringify() {
		StringBuffer sb = new StringBuffer();

		try {
			if( !this.loaded ) load();

			sb.append( super.stringify() );
			sb.append( super.stringify() + linesep );

			sb.append( "Filename: " + this.fullfile + linesep );
			sb.append( "Revision: " + this.version + linesep );
		} catch( Exception e ) {

		} finally {
			//sb.append( super.stringify() );
			sb.insert( 0, super.stringify() );
		}

		return sb.toString();
	}
	
	public static ChangeSet2 getChangeset( Diffable e1, Diffable e2, boolean merge, File viewContext ) throws CleartoolException, UnableToInitializeEntityException {
		//return context.getChangeset( e1, e2, merge, viewContext );
		String cmd = "diffbl -version " + ( !merge ? "-nmerge " : "" ) + ( e2 == null ? "-pre " : "" ) + " " + e1.getFullyQualifiedName() + ( e2 != null ? e2.getFullyQualifiedName() : "" );

		List<String> lines = null;

		try {
			lines = Cleartool.run( cmd, viewContext ).stdoutList;
		} catch( Exception e ) {
			throw new CleartoolException( "Could not retreive the differences of " + e1 + " and " + e2 + ": " + e.getMessage(), e );
		}

		int length = viewContext.getAbsoluteFile().toString().length();

		// System.out.println(viewContext.getAbsolutePath() + " - " + length);

		net.praqma.clearcase.changeset.ChangeSet2 changeset = new ChangeSet2( viewContext );

		for( int i = 0; i < lines.size(); i++ ) {
			Matcher m = rx_versionName.matcher( lines.get( i ) );
			if( m.find() ) {

				String f = m.group( 2 ).trim();

				logger.debug( "F: " + f );

				Version version = (Version) UCMEntity.getEntity( Version.class, m.group( 2 ).trim() );

				changeset.addVersion( version );
			}
		}

		return changeset;
	}
	
	public static List<Activity> getBaselineDiff( Diffable d1, Diffable d2, boolean merge, File viewContext ) throws CleartoolException, UnableToLoadEntityException, UCMEntityNotFoundException, UnableToInitializeEntityException {
		//return context.getBaselineDiff( d1, d2, merge, viewContext );
		String cmd = "diffbl -version -act " + ( !merge ? "-nmerge " : "" ) + ( d2 == null ? "-pre " : "" ) + d1.getFullyQualifiedName() + ( d2 != null ? " " + d2.getFullyQualifiedName() : "" );

		List<String> lines = null;
		
		try {
			lines = Cleartool.run( cmd, viewContext ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Could not get difference between " + d1.getFullyQualifiedName() + " and " + d2.getFullyQualifiedName() + ": " + e.getMessage(), e );
		}
		
		return Activity.parseActivityStrings( lines, viewContext.getAbsoluteFile().toString().length() );
	}

	@Override
	public int compareTo( Version other ) {
		/* The same file */
		if( this.file.equals( other.getFile() ) ) {
			try {
				return this.version.compareTo( other.getVersion() );
			} catch ( UnableToLoadEntityException e) {
				return -1;
			}
		} else {
			return this.file.compareTo( other.getFile() );
		}
	}


	public static Version get( String name ) throws UnableToInitializeEntityException {
		return (Version) UCMEntity.getEntity( Version.class, name );
	}
}
