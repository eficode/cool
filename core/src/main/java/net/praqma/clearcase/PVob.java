package net.praqma.clearcase;

import net.praqma.clearcase.exceptions.CleartoolException;

public class PVob extends Vob {

	private String localPath;
	private String globalPath;
	
	public static final String rx_format = "\\S+";

	public PVob( String name ) {
		super( name );

		this.projectVob = true;
	}

	public static PVob create( String name, String path, String comment ) throws CleartoolException {
		Vob.create( name, true, path, comment );
		PVob pvob = new PVob( name );
		pvob.storageLocation = path;
		
		if( path == null ) {
			pvob.load();
		}

		return pvob;
	}

	public static PVob get( String pvobname ) {
		try {
			PVob pvob = new PVob( pvobname );
			pvob.load();
			return pvob;
		} catch( Exception e ) {
			return null;
		}
	}

}
