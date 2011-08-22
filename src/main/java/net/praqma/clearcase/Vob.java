package net.praqma.clearcase;

import net.praqma.clearcase.ucm.UCMException;

public class Vob extends Cool {

	protected String name;
	
	protected boolean projectVob = false;
	
	protected String storageLocation = null;

	public Vob(String name) {
		this.name = name;
	}
	
	public void load() throws UCMException {
		context.loadVob(this);
	}
	
	public void mount() throws UCMException {
		context.mountVob(this);
	}
	
	public void unmount() throws UCMException {
		context.unmountVob(this);
	}
	
	public void setStorageLocation( String storageLocation ) {
		this.storageLocation = storageLocation;
	}
	
	public String getStorageLocation() throws UCMException {
		if( storageLocation == null ) {
			load();
		}
		
		return this.storageLocation;
	}
	
	public void setIsProjectVob( boolean pvob ) {
		this.projectVob = pvob;
	}
	
	public boolean isProjectVob() throws UCMException {
		if( storageLocation == null ) {
			load();
		}
		
		return this.projectVob;
	}
	
	public String toString() {
		return name;
	}
	
	public String getName() {
		if( name.startsWith( "\\" )) {
			return name.substring( 1 );
		} else {
			return name;
		}
	}
	
	public static Vob create( String name, String path, String comment ) throws UCMException {
		context.createVob(name, false, path, comment);
		
		Vob vob = new Vob(name);
		vob.storageLocation = path;
		
		return vob;
	}
	
	public void remove() throws UCMException {
		context.removeVob( this );
	}
	
	public static Vob get( String vobname ) {
		try {
			Vob vob = new Vob(vobname);
			vob.load();
			return vob;
		} catch( Exception e ) {
			return null;
		}
	}

}
