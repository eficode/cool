package net.praqma.clearcase.changeset;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.exceptions.UCMException;
import net.praqma.util.structure.Tuple;

public class ChangeSetElement {
	private File file;
	private int revision;
	private String version;
	private String fullversion;
	
	private File oldFile;
	
	private Status status = Status.UNCHANGED;
	
	public enum Status {
		UNCHANGED,
		CHANGED,
		ADDED,
		DELETED
	}
	
	public ChangeSetElement( File file, String version ) throws UCMException {
		this.file = file;
		this.fullversion = version;
		
		Tuple<String, Integer> info = parseVersion( version );
		this.version = info.t1;
		this.revision = info.t2;
	}
	
	public ChangeSetElement( File file, String version, Status status ) throws UCMException {
		this.file = file;
		this.fullversion = version;
		this.status = status;
		
		Tuple<String, Integer> info = parseVersion( version );
		this.version = info.t1;
		this.revision = info.t2;
	}
	
	private static Pattern rx_versionVersion = Pattern.compile( "^(.*?)\\\\(\\d+)\\\\.*?$" );
	private static Pattern rx_versionVersionSimple = Pattern.compile( "^(.*?)\\\\(\\d+)$" );
	
	public static Tuple<String, Integer> parseVersion( String version ) throws UCMException {
		Matcher simple = rx_versionVersionSimple.matcher( version );
		
		if( simple.find() ) {
			return new Tuple<String, Integer>( simple.group(1), Integer.parseInt( simple.group(2) ) );
		} else {
			Matcher advanced = rx_versionVersion.matcher( version );
			
			if( advanced.find() ) {
				return new Tuple<String, Integer>( advanced.group(1), Integer.parseInt( advanced.group(2) ) );
			} else {
				throw new UCMException( "Could not find any version information on " + version );
			}
		}
		
	}

	public File getFile() {
		return file;
	}

	public void setFile( File file ) {
		this.file = file;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion( String version ) {
		this.version = version;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus( Status status ) {
		this.status = status;
	}

	public int getRevision() {
		return revision;
	}

	public void setRevision( int revision ) {
		this.revision = revision;
	}

	public String getFullversion() {
		return fullversion;
	}

	public void setFullversion( String fullversion ) {
		this.fullversion = fullversion;
	}
	
	public File getOldFile() {
		return oldFile;
	}

	public void setOldFile( File oldFile ) {
		this.oldFile = oldFile;
	}
	
	public boolean isMoved() {
		return ( oldFile != null );
	}

	public String toString() {
		return this.file + "[" + this.version + "(" + this.revision + ")]";
	}

}
