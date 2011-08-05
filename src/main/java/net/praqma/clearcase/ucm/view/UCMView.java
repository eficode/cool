package net.praqma.clearcase.ucm.view;

import java.io.File;
import java.util.Map;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.UCMException.UCMType;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.UCM;

public class UCMView extends UCM {

	protected String path;
	protected String viewtag = "";
	protected Stream stream = null;
	
	protected String storageLocation = null;
	
	protected boolean dynamic = false;

	public UCMView() {
	}

	public UCMView(String path) {
		this.path = path;
	}
	
	public UCMView(String path, String viewtag) {
		this.path = path;
		this.viewtag = viewtag;
	}
	
	public UCMView(String path, String viewtag, Stream stream) {
		this.path = path;
		this.viewtag = viewtag;
		this.stream = stream;
	}

	public static SnapshotView GetSnapshotView(File viewroot) throws UCMException {
		return new SnapshotView(viewroot);
	}

	public static boolean ViewExists(String viewtag) {
		boolean b = context.viewExists(viewtag);
		logger.debug("The view " + viewtag + " exists: " + b);
		return b;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPath() {
		return path;
	}
	
	public String GetViewtag() {
		return this.viewtag;
	}
	
	public void removeView() throws UCMException {
		context.removeView( this );
	}
	
	public boolean isDynamicView() {
		return this.dynamic;
	}
	
	public void load() throws UCMException {
		Map<String, String> options = context.loadView( this );
		
		try {
			this.storageLocation = options.get("pathname");
		} catch( NullPointerException e ) {
			throw new UCMException( "Could not load " + this.toString() + " correctly: " + e.getMessage(), UCMType.LOAD_FAILED );
		}
	}
	
	public String getStorageLocation() throws UCMException {
		if( this.storageLocation == null ) {
			this.load();
		}
		
		return this.storageLocation;
	}
	
	public Stream getStream() throws UCMException {
		return stream;
	}
	
	public String toString() {
		return viewtag;
	}

}