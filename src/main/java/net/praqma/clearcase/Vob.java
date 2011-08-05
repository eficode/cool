package net.praqma.clearcase;

import java.util.Map;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.UCMException.UCMType;

public class Vob extends Cool {

	protected String name;
	
	protected String storageLocation = null;

	public Vob(String name) {
		this.name = name;
	}
	
	public void load() throws UCMException {
		Map<String, String> options = context.loadVob(this);
		
		try {
			this.storageLocation = options.get("pathname");
		} catch( NullPointerException e ) {
			throw new UCMException( "Could not load " + this.toString() + " correctly: " + e.getMessage(), UCMType.LOAD_FAILED );
		}
	}
	
	public void mount() throws UCMException {
		context.mountVob(this);
	}
	
	public String toString() {
		return name;
	}
	
	public String getStorageLocation() throws UCMException {
		if( storageLocation == null ) {
			load();
		}
		
		return this.storageLocation;
	}
	
	public static Vob create( String name, String path, String comment ) throws UCMException {
		context.createVob(name, true, path, comment);
		
		Vob vob = new Vob(name);
		vob.storageLocation = path;
		
		return vob;
	}
	
	public void remove() throws UCMException {
		context.removeVob( this );
	}
	
	public static boolean exists( String vobname ) {
		try {
			Vob vob = new Vob(vobname);
			vob.load();
			return true;
		} catch( Exception e ) {
			return false;
		}
	}

}
