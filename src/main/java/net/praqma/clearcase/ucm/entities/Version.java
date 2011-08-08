package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.view.SnapshotView;

public class Version extends UCMEntity {
	/* Version specific fields */
	private String kind = null;
	private String date = null;
	private String user = null;
	private String machine = null;
	private boolean checkedout = false;
	private String comment = null;
	private String branch = null;

	private SnapshotView view = null;

	private String file = null;
	private String sfile = null;
	private File version = null;
	private String revision = "0";

	private static String rx_revision = "(\\d+)$";
	private static Pattern p_revision = Pattern.compile( "@@(.*)$" );

	Version() {
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

	void postProcess() {
		logger.trace_function();

		String fqname = this.fqname.matches( "^\\S:\\\\.*" ) ? this.fqname : System.getProperty( "user.dir" ) + filesep + this.fqname;

		this.fqname = fqname;

		/* Find the revision number, make it zero if it is not found */
		Matcher m = p_revision.matcher( this.fqname );
		if( m.find() ) {
			// this.revision = Integer.parseInt( m.group( 1 ) );
			this.revision = m.group( 1 );
		} else {
			this.revision = "0";
		}

		String tmp = this.fqname;
		tmp = tmp.replaceFirst( "(?m)\\@\\@.*$", "" );
		tmp = tmp.replaceFirst( "(?m)^\\s+", "" );
		this.file = tmp;

		this.version = new File( tmp );
	}

	public boolean hijack() {
		if( this.version.canWrite() ) {
			return true;
		}

		return this.version.setWritable( true );
	}

	/* Getters */

	public static Version getUnextendedVersion( File file, File viewroot ) throws UCMException {
		return context.getVersionExtension( file, viewroot );
	}

	public String getUser() {
		if( !loaded ) load();

		return this.user;
	}

	public String blame() {
		return this.getUser();
	}

	public String getFile() {
		if( !loaded ) load();

		return this.file;
	}

	public String getDate() {
		if( !loaded ) load();

		return this.date;
	}

	public String getRevision() {
		if( !loaded ) load();

		return this.revision;
	}

	public void load() {
		logger.trace_function();

		HashMap<String, String> result = context.getVersion( this );

		this.date = result.get( "date" );
		this.user = result.get( "user" );
		this.machine = result.get( "machine" );
		this.comment = result.get( "comment" );
		this.checkedout = result.get( "checkedout" ).length() > 0 ? true : false;
		this.kind = result.get( "kind" );
		this.branch = result.get( "branch" );

		this.loaded = true;
	}
	
	public void create( File file, SnapshotView view ) throws UCMException {
		
		Version version = Version.getUnextendedVersion( file, view.GetViewRoot() );
		version.addToSourceControl( view );
	}
	
	public void addToSourceControl( SnapshotView view ) throws UCMException {
		context.addToSourceControl( this, view );
	}
	
	public void checkIn( SnapshotView view ) throws UCMException {
		context.checkIn( this, view.GetViewRoot() );
	}
	
	public void checkOut() throws UCMException {
		context.checkOut( this, view.GetViewRoot() );
	}

	public void setView( SnapshotView view ) {
		this.view = view;
	}

	public void setSFile( String sfile ) {
		this.sfile = sfile;
	}

	public String getSFile() {
		return sfile;
	}
	
	public File getVersion() {
		return version;
	}

	public String stringify() throws UCMException {
		StringBuffer sb = new StringBuffer();
		sb.append( super.stringify() + linesep );

		sb.append( "Filename: " + this.file + linesep );
		sb.append( "Revision: " + this.revision + linesep );

		return sb.toString();
	}
}
