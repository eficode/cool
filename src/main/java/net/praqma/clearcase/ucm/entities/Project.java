package net.praqma.clearcase.ucm.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.UCMException;

public class Project extends UCMEntity {
    /* Project specific fields */
    private Stream stream = null;
    
    /**
     * Enables inter-project deliveries
     */
    public static final int POLICY_INTERPROJECT_DELIVER   = 1;
    public static final int POLICY_CHSTREAM_UNRESTRICTED  = 2;
    public static final int POLICY_DELIVER_REQUIRE_REBASE = 4;
    public static final int POLICY_DELIVER_NCO_DEVSTR     = 8;

    Project() {
    	super( "project" );
    }

    /**
     * This method is only available to the package, because only UCMEntity
     * should be allowed to call it.
     * 
     * @return A new Project Entity
     */
    static Project getEntity() {
        return new Project();
    }

    /* For now, the project implements the Plevel functionality */
    public enum PromotionLevel implements Serializable {
        INITIAL, BUILT, TESTED, RELEASED, REJECTED;
    }

    /**
     * Given a String, return the corresponding Promotion Level.
     * 
     * @param str
     *            , if not a valid Promotion Level INITAL is returned.
     * @return A Promotion Level
     */
    public static PromotionLevel getPlevelFromString( String str ) {
        PromotionLevel plevel = PromotionLevel.INITIAL;

        try {
            plevel = PromotionLevel.valueOf( str );
        } catch( Exception e ) {
            /* Do nothing... */
        }

        return plevel;
    }

    public static PromotionLevel promoteFrom( PromotionLevel plevel ) {
        switch( plevel ) {
        case INITIAL:
            plevel = PromotionLevel.BUILT;
            break;
        case BUILT:
            plevel = PromotionLevel.TESTED;
            break;
        case TESTED:
            plevel = PromotionLevel.RELEASED;
            break;
        case RELEASED:
            plevel = PromotionLevel.RELEASED;
            break;
        }

        return plevel;
    }
    
    public static String getPolicy( int policy ) {
    	String p = "";
    	if( ( policy & POLICY_INTERPROJECT_DELIVER ) > 0 ) {
    		p += "POLICY_INTERPROJECT_DELIVER,";
    	}
    	
    	if( ( policy & POLICY_CHSTREAM_UNRESTRICTED ) > 0 ) {
    		p += "POLICY_CHSTREAM_UNRESTRICTED,";
    	}
    	
    	if( ( policy & POLICY_DELIVER_REQUIRE_REBASE ) > 0 ) {
    		p += "POLICY_DELIVER_REQUIRE_REBASE,";
    	}
    	
    	if( ( policy & POLICY_DELIVER_NCO_DEVSTR ) > 0 ) {
    		p += "POLICY_DELIVER_NCO_DEVSTR,";
    	}
    	
    	if( p.length() > 0 ) {
    		p = p.substring( 0, ( p.length() - 1 ) );
    	}
    	
    	return p;
    }
    
    public static Project create( String name, String root, PVob pvob, int policy, String comment, Component ... mcomps ) throws UCMException {
    	context.createProject( name, root, pvob, policy, comment, mcomps );
    	
    	Project p = UCMEntity.getProject( name, pvob, true );
    	
    	return p;
    }

    public void load() throws UCMException {
        context.loadProject( this );
    }

    public void setStream( Stream stream ) {
        this.stream = stream;
    }

    public Stream getIntegrationStream() throws UCMException {
        if( !this.loaded )
            load();
        return stream;
    }

    public static List<String> getPromotionLevels() {
        List<String> retval = new ArrayList<String>();
        for( Object o : PromotionLevel.values() ) {
            retval.add( o.toString() );
        }
        return retval;
    }
    
    public static List<Project> getProjects( PVob vob ) throws UCMException {
    	return context.getProjects( vob );
    }
    
    public static void create() {
    	
    }
}
