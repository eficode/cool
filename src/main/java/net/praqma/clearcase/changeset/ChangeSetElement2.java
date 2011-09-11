package net.praqma.clearcase.changeset;

import java.io.File;

import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.entities.Version.Status;

public class ChangeSetElement2 {
	private File file;	
	private File oldFile;
	private Version origin;
	
	private Status status = Status.UNCHANGED;
	
	public ChangeSetElement2( File file, Status status ) {
		this.file = file;
		this.status = status;
	}
	
	public ChangeSetElement2( File file, Status status, Version origin ) {
		this.file = file;
		this.status = status;
		this.origin = origin;
	}
	
	public Version getOrigin() {
		return origin;
	}

	public File getFile() {
		return file;
	}

	public void setFile( File file ) {
		this.file = file;
	}

	public File getOldFile() {
		return oldFile;
	}

	public void setOldFile( File oldFile ) {
		this.oldFile = oldFile;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus( Status status ) {
		this.status = status;
	}
	

}
