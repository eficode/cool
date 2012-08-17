/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.clearcase.ucm.entities;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;

/**
 * 
 * @author Praqma
 */
public class Folder extends UCMEntity {
	transient static private Logger logger = Logger.getLogger();
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	
	Folder() {
		super( "folder" );
		tracer.entering(Folder.class.getSimpleName(), "Folder");
		tracer.exiting(Folder.class.getSimpleName(), "Folder");
	}
	
	public static Folder create( String name, PVob pvob, String in, String comment ) throws UnableToCreateEntityException {
		tracer.entering(Folder.class.getSimpleName(), "create", new Object[]{name, pvob, in, comment});
		
		String cmd = "mkfolder " + getargComment( comment ) + " " + getargIn( in ) + " " + name + "@" + pvob;
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			UnableToCreateEntityException exception = new UnableToCreateEntityException( Folder.class, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			throw exception;
		}
		
		Folder output = null;
		
		tracer.exiting(Folder.class.getSimpleName(), "create", output);
		
		return output;
	}

	public static Folder get( String name ) throws UnableToInitializeEntityException {
		tracer.entering(Folder.class.getSimpleName(), "get", name);
		
		Folder output = null;
		if( !name.startsWith( "folder:" ) ) {
			output = (Folder) UCMEntity.getEntity( Folder.class, "folder:" + name );
			
			tracer.exiting(Folder.class.getSimpleName(), "get", output);
			
			return output;
		}
		
		output = (Folder) UCMEntity.getEntity( Folder.class, name );
		
		tracer.exiting(Folder.class.getSimpleName(), "get", output);
		
		return output;
	}

	public static Folder get( String fqname, PVob vob ) throws UnableToInitializeEntityException {
		tracer.entering(Folder.class.getSimpleName(), "get", new Object[]{fqname, vob});
		
		Folder output = null;
		if( !fqname.startsWith( "folder:" ) ) {
			output = (Folder) UCMEntity.getEntity( Folder.class, "folder:" + fqname + "@" + vob );
			
			tracer.exiting(Folder.class.getSimpleName(), "get", output);
			
			return output;
		}
		output = (Folder) UCMEntity.getEntity( Folder.class, fqname + "@" + vob );
		
		tracer.exiting(Folder.class.getSimpleName(), "get", output);
		
		return output;
	}

}
