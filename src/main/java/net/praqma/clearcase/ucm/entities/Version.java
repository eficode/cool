package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.changeset.ChangeSet;
import net.praqma.clearcase.changeset.ChangeSet2;
import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.util.debug.Logger;

public class Version extends UCMEntity implements Comparable<Version> {
	transient private static Logger logger = Logger.getLogger();
	
	//private String date = null;
	private String user = null;
	private String machine = null;
	private boolean checkedout = false;
	private String comment = null;
	private String branch = null;
	
	private boolean oldVersion = false;
	private File oldFile;

	private SnapshotView view = null;

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

	/**
	 * This method is only available to the package, because only
	 * ClearcaseEntity should be allowed to call it.
	 * 
	 * @return A new Version Entity
	 */
	static Version getEntity() {
		return new Version();
	}
	
	private static final Pattern rx_findAddedElements = Pattern.compile( qfs + ".*?" + qfs + "(\\d+)" + qfs + "(.*?)" + qfs );
	private static final Pattern rx_findRevision = Pattern.compile( qfs + "(\\d+)$" );

	void postProcess() {
		String fqname = this.fqname.matches( "^\\S:\\\\.*" ) ? this.fqname : System.getProperty( "user.dir" ) + filesep + this.fqname;

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

	public static Version getUnextendedVersion( File file, File viewroot ) throws UCMException {
		return context.getVersionExtension( file, viewroot );
	}

	public String blame() throws UCMException {
		return this.getUser();
	}

	@Deprecated
	public String getFileAsString() throws UCMException {
		if( !loaded ) load();

		return this.fullfile;
	}


	public String getVersion() throws UCMException {
		if( !loaded ) load();

		return this.version;
	}

	public void load2() {
		HashMap<String, String> result = context.getVersion( this );

		//this.date = result.get( "date" );
        try {
			this.date = dateFormatter.parse( result.get( "date" ) );
		} catch (ParseException e) {
			this.date = null;
		}
		this.user = result.get( "user" );
		this.machine = result.get( "machine" );
		this.comment = result.get( "comment" );
		this.checkedout = result.get( "checkedout" ).length() > 0 ? true : false;
		//this.kind = result.get( "kind" );
		this.branch = result.get( "branch" );

		this.loaded = true;
	}
	
	public void load() throws UCMException {
		context.loadVersion( this );
	}
	
	public static Version create( File file, boolean mkdir, SnapshotView view ) throws UCMException {

		Version.addToSourceControl( file, mkdir, view.getViewRoot() );
		
		Version version = Version.getUnextendedVersion( file, view.getViewRoot() );
		version.setView( view );
		
		return version;
	}
	
	public static void addToSourceControl( File file, boolean mkdir, File view ) throws UCMException {
		context.addToSourceControl( file, mkdir, view );
	}
	
	public void checkIn() throws UCMException {
		context.checkIn( this, false, view.getViewRoot() );
	}
	
	public void checkIn( boolean identical ) throws UCMException {
		context.checkIn( this, identical, view.getViewRoot() );
	}
	
	public void checkInIdentical() throws UCMException {
		context.checkIn( this, true, view.getViewRoot() );
	}
	
	public void checkOut() throws UCMException {
		context.checkOut( this, view.getViewRoot() );
	}
	
	public void removeVersion() throws UCMException {
		context.removeVersion( this.file, view.getViewRoot() );
	}
	
	public static void removeVersion( File file, File viewContext ) throws UCMException {
		context.removeVersion( file, viewContext );
	}
	
	public void removeName( ) throws UCMException {
		context.removeName( this.file, view.getViewRoot() );
	}
	
	public static void removeName( File file, File viewContext ) throws UCMException {
		context.removeName( file, viewContext );
	}
	
	public static void moveFile( File file, File destination, SnapshotView view ) throws UCMException {
		context.moveFile( file, destination, view.getViewRoot() );
	}
	
	public void moveFile( File destination ) throws UCMException {
		context.moveFile( file, destination, view.getViewRoot() );
	}
	
	public static void checkIn( File file, boolean identical, File view ) throws UCMException {
		context.checkIn( file, identical, view );
	}
	
	public static void checkOut( File file, File view ) throws UCMException {
		context.checkOut( file, view );
	}
	
	public static void uncheckout( File file, boolean keep, File viewContext ) throws UCMException {
		context.uncheckout( file, keep, viewContext );
	}
	
	public void uncheckout() throws UCMException {
		context.uncheckout( this.getFile(), true, view.getViewRoot() );
	}
	
	public void uncheckout( boolean keep ) throws UCMException {
		context.uncheckout( this.getFile(), keep, view.getViewRoot() );
	}

	public void setView( SnapshotView view ) {
		this.view = view;
	}
	
	public SnapshotView getView() {
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
	
	public void setRevision( int revison ) {
		this.revision = revision;
	}
	
	public Integer getRevision() {
		return this.revision;
	}
	
	public static List<File> getUncheckedIn( File viewContext ) throws UCMException {
		return context.getUnchecedInFiles( viewContext );
	}
	
	public boolean isDirectory() throws UCMException {
		if( !loaded ) load();
		return kind.equals( Kind.DIRECTORY_ELEMENT );
	}
	
	public boolean isFile() throws UCMException {
		if( !loaded ) load();
		return kind.equals( Kind.FILE_ELEMENT );
	}
	
	@Deprecated
	public void setOldVersion( boolean old ) {
		this.oldVersion = old;
	}
	
	@Deprecated
	public boolean isOldVersion() {
		return oldVersion;
	}
	
	/**
	 * An exception safe way to determine whether the file is under
	 * source control
	 * @param element The File to be checked
	 * @param viewContext The view context as a File path
	 * @return True if the File element is under source control
	 */
	public static boolean isUnderSourceControl( File element, File viewContext ) {
		try {
			return context.isUnderSourceControl( element, viewContext );
		} catch (UCMException e) {
			return false;
		}
	}
	
	/**
	 * An exception safe way to determine whether the file is checked out
	 * @param element The File to be checked
	 * @param viewContext The view context as a File path
	 * @return True if the File element is checked out
	 */
	public static boolean isCheckedout( File element, File viewContext ) {
		try {
			return context.isCheckedout( element, viewContext );
		} catch (UCMException e) {
			return false;
		}
	}
	
	public void setOldFile( File oldFile ) {
		this.oldFile = oldFile;
	}
	
	public boolean isMoved() {
		return ( oldFile != null );
	}

	public String stringify() throws UCMException {
		StringBuffer sb = new StringBuffer();
		sb.append( super.stringify() + linesep );

		sb.append( "Filename: " + this.fullfile + linesep );
		sb.append( "Revision: " + this.version + linesep );

		return sb.toString();
	}
	
	public static ChangeSet2 getChangeset( Diffable e1, Diffable e2, boolean merge, File viewContext ) throws UCMException {
		return context.getChangeset( e1, e2, merge, viewContext );
	}
	
	public static List<Activity> getBaselineDiff( Diffable d1, Diffable d2, boolean merge, File viewContext ) throws UCMException {
		return context.getBaselineDiff( d1, d2, merge, viewContext );
	}

	@Override
	public int compareTo( Version other ) {
		/* The same file */
		if( this.file.equals( other.getFile() ) ) {
			try {
				return this.version.compareTo( other.getVersion() );
			} catch (UCMException e) {
				return -1;
			}
		} else {
			return this.file.compareTo( other.getFile() );
		}
	}
}
