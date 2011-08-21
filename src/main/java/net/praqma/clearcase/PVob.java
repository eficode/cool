package net.praqma.clearcase;

import net.praqma.clearcase.ucm.UCMException;

public class PVob extends Vob {

	private String localPath;
	private String globalPath;

	public PVob(String name) {
		super(name);
		
		this.projectVob = true;
	}

	public static PVob create( String name, String path, String comment ) throws UCMException {
		context.createVob(name, true, path, comment);
		PVob pvob = new PVob(name);
		pvob.storageLocation = path;
		
		return pvob;
	}
	
	public static PVob get( String pvobname ) {
		try {
			PVob pvob = new PVob(pvobname);
			pvob.load();
			return pvob;
		} catch( Exception e ) {
			return null;
		}
	}

}
