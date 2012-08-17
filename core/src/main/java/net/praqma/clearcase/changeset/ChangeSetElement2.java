package net.praqma.clearcase.changeset;

import java.io.File;

import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.entities.Version.Status;
import net.praqma.logging.Config;

public class ChangeSetElement2 {
	private File file;	
	private File oldFile;
	private Version origin;

	private Status status = Status.UNCHANGED;

	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	public ChangeSetElement2( File file, Status status ) {
		tracer.entering(ChangeSetElement2.class.getSimpleName(), "ChangeSetElement2", new Object[]{file, status});

		this.file = file;
		this.status = status;

		tracer.exiting(ChangeSetElement2.class.getSimpleName(), "ChangeSetElement2", new Object[]{file, status});
	}

	public ChangeSetElement2( File file, Status status, Version origin ) {
		tracer.entering(ChangeSetElement2.class.getSimpleName(), "ChangeSetElement2", new Object[]{file, status});

		this.file = file;
		this.status = status;
		this.origin = origin;

		tracer.exiting(ChangeSetElement2.class.getSimpleName(), "ChangeSetElement2", new Object[]{file, status});
	}

	public Version getOrigin() {
		tracer.entering(ChangeSetElement2.class.getSimpleName(), "getOrigin");
		tracer.finest(String.format("Returning value: %s", origin));

		return origin;
	}

	public File getFile() {
		tracer.entering(ChangeSetElement2.class.getSimpleName(), "getFile");
		tracer.finest(String.format("Returning value: %s", file));
		return file;
	}

	public void setFile( File file ) {
		tracer.entering(ChangeSetElement2.class.getSimpleName(), "setFile");

		this.file = file;

		tracer.exiting(ChangeSetElement2.class.getSimpleName(), "setFile");
	}

	public File getOldFile() {
		tracer.entering(ChangeSetElement2.class.getSimpleName(), "getOldFile");
		tracer.finest(String.format("Returning value: %s", oldFile));

		return oldFile;
	}

	public void setOldFile( File oldFile ) {
		tracer.entering(ChangeSetElement2.class.getSimpleName(), "setOldFile");

		this.oldFile = oldFile;

		tracer.exiting(ChangeSetElement2.class.getSimpleName(), "setOldFile");
	}

	public Status getStatus() {
		tracer.entering(ChangeSetElement2.class.getSimpleName(), "setOldFile");
		tracer.finest(String.format("Returning value: %s", status));
		
		return status;
	}

	public void setStatus( Status status ) {
		tracer.entering(ChangeSetElement2.class.getSimpleName(), "setStatus");
		
		this.status = status;
		
		tracer.exiting(ChangeSetElement2.class.getSimpleName(), "setStatus");
	}


}
