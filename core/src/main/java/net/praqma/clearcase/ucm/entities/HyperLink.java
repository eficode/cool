package net.praqma.clearcase.ucm.entities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotInitializedException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;

public class HyperLink extends UCMEntity {
	
	public static final Pattern pattern_hlink_fqname = Pattern.compile( "^hlink:(" + rx_ccdef_allowed + "+)@(\\d+)@(" + rx_ccdef_vob + "+)$" );
	
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	transient private static Logger logger = Logger.getLogger();
	
	/* Hlink specific fields */
	private Integer identifier = null;
	private String type = null;
	private String value = null;

	HyperLink() {
		super( "hlink" );
		tracer.entering(HyperLink.class.getSimpleName(), "HyperLink");
		tracer.exiting(HyperLink.class.getSimpleName(), "HyperLink");
	}

	public static void create( String type, UCMEntity entity, String key, String value ) {
		tracer.entering(HyperLink.class.getSimpleName(), "create", new Object[]{type, entity, key, value});
		
		UnsupportedOperationException exception = new UnsupportedOperationException("Not yet implemented");
		
		tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
		
		throw exception;
	}
	
	public static void createType( String typeName, PVob pvob, String comment ) throws CleartoolException {
		tracer.entering(HyperLink.class.getSimpleName(), "createType", new Object[]{typeName, pvob, comment});
		
		String cmd = "mkhltype -global " + ( comment != null ? "-c \"" + comment + "\"" : "" ) + typeName + "@" + pvob;
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			CleartoolException exception = new CleartoolException( "Could not create the hyperlink type " + typeName, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		
		tracer.exiting(HyperLink.class.getSimpleName(), "createType");
	}

	@Override
	public void initialize() throws UCMEntityNotInitializedException {
		tracer.entering(HyperLink.class.getSimpleName(), "initialize");
		
		Matcher match = pattern_hlink_fqname.matcher( fqname );

		if( match.find() ) {
			this.type = match.group( 1 );
			shortname = match.group( 1 );
			pvob = new PVob( match.group( 3 ) );
			this.identifier = Integer.parseInt( match.group( 2 ) );
		} else {
			UCMEntityNotInitializedException exception = new UCMEntityNotInitializedException( fqname );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
	
		tracer.exiting(HyperLink.class.getSimpleName(), "createType");
	}

	public static HyperLink getHyperLink( String fqname, String value ) throws UnableToInitializeEntityException {
		tracer.entering(HyperLink.class.getSimpleName(), "getHyperLink", new Object[]{fqname, value});
		
		HyperLink hlink = get( fqname );

		hlink.setValue( value );

		hlink.loaded = true;

		tracer.exiting(HyperLink.class.getSimpleName(), "getHyperLink", hlink);
		
		return hlink;
	}

	/* Getters */

	public String getValue() {
		tracer.entering(HyperLink.class.getSimpleName(), "getValue");
		tracer.exiting(HyperLink.class.getSimpleName(), "getValue", this.value);
		return this.value;
	}

	/* Setters */

	public void setValue( String value ) {
		tracer.entering(HyperLink.class.getSimpleName(), "setValue", value);
		
		this.value = value;
		
		tracer.exiting(HyperLink.class.getSimpleName(), "setValue", this.value);
	}
	
	public static HyperLink get( String name ) throws UnableToInitializeEntityException {
		tracer.entering(HyperLink.class.getSimpleName(), "get", name);
		
		if( !name.startsWith( "hlink:" ) ) {
			name = "hlink:" + name;
		}

		HyperLink entity = (HyperLink) UCMEntity.getEntity( HyperLink.class, name );
		
		tracer.exiting(HyperLink.class.getSimpleName(), "setValue", entity);
		
		return entity;
	}
}
