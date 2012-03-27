/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.praqma.clearcase.ucm.entities;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.util.debug.Logger;

/**
 *
 * @author Praqma
 */
public class Folder extends UCMEntity {
    transient static private Logger logger = Logger.getLogger();
   
    Folder () {
        super("folder");
    }
    
    public static Folder get( String name ) throws UnableToCreateEntityException {
        if(!name.startsWith( "folder:" )) {
            return ( Folder )UCMEntity.getEntity( Folder.class, "folder:"+name );
        }
        
        return ( Folder )UCMEntity.getEntity( Folder.class, name );
    }
    
    public static Folder get( String fqname, PVob vob ) throws UnableToCreateEntityException {
        if(!fqname.startsWith( "folder:" )) {
            return ( Folder )UCMEntity.getEntity( Folder.class, "folder:"+fqname+"@"+vob );
        }
        return ( Folder )UCMEntity.getEntity( Folder.class, fqname+"@"+vob.getName() );
    }
    
    
}
