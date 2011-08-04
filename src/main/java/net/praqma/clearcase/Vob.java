package net.praqma.clearcase;

import java.io.File;

import net.praqma.clearcase.ucm.UCMException;

public class Vob extends Cool {

	protected String name;
	
	protected File storageLocation = null;

	public Vob(String name) {
		this.name = name;
	}
	
	public void load() {
		
	}
	
	public void mount() {
		
	}
	
	public String toString() {
		return name;
	}
	
	public File getStorageLocation() {
		return this.storageLocation;
	}
	
	public static Vob create( String name, File path, String comment ) throws UCMException {
		context.createVob(name, true, path, comment);
		
		Vob vob = new Vob(name);
		vob.storageLocation = path;
		
		return vob;
	}

}
