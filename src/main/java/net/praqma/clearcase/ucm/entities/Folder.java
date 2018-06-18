/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.clearcase.ucm.entities;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;

import java.util.logging.Logger;

/**
 * 
 * @author Praqma
 */
@SuppressFBWarnings("")
public class Folder extends UCMEntity {
	transient static private Logger logger = Logger.getLogger( Folder.class.getName() );

	Folder() {
		super( "folder" );
	}
	
	public static Folder create( String name, PVob pvob, String in, String comment ) throws UnableToCreateEntityException {
		
		String cmd = "mkfolder " + getargComment( comment ) + " " + getargIn( in ) + " " + name + "@" + pvob;
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new UnableToCreateEntityException( Folder.class, e );
		}
		
		return null;
	}

	public static Folder get( String name ) throws UnableToInitializeEntityException {
		if( !name.startsWith( "folder:" ) ) {
			return (Folder) UCMEntity.getEntity( Folder.class, "folder:" + name );
		}

		return (Folder) UCMEntity.getEntity( Folder.class, name );
	}

	public static Folder get( String fqname, PVob vob ) throws UnableToInitializeEntityException {
		if( !fqname.startsWith( "folder:" ) ) {
			return (Folder) UCMEntity.getEntity( Folder.class, "folder:" + fqname + "@" + vob );
		}
		return (Folder) UCMEntity.getEntity( Folder.class, fqname + "@" + vob );
	}

}
