package net.praqma.clearcase;

import java.io.File;

import net.praqma.clearcase.ucm.UCMException;

public class PVob extends Vob {

	private String localPath;
	private String globalPath;

	public PVob(String name) {
		super(name);
	}

	public static PVob create( String name, File path, String comment ) throws UCMException {
		context.createVob(name, true, path, comment);
		
		PVob pvob = new PVob(name);
		pvob.storageLocation = path;
		
		return pvob;
	}

}
