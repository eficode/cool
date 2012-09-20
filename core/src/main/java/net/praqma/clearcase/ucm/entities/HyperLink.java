package net.praqma.clearcase.ucm.entities;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotInitializedException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;

public class HyperLink extends UCMEntity {
	
	public static final Pattern pattern_hlink_fqname = Pattern.compile( "^hlink:(" + rx_ccdef_allowed + "+)@(\\d+)@(" + rx_ccdef_vob + "+)$" );
	
	transient private static Logger logger = Logger.getLogger( HyperLink.class.getName() );
	
	/* Hlink specific fields */
	private Integer identifier = null;
	private String type = null;
	private String value = null;

	HyperLink() {
		super( "hlink" );
	}

	public static void create( String type, UCMEntity entity, String key, String value ) {
	}
	
	public static void createType( String typeName, PVob pvob, String comment ) throws CleartoolException {
		String cmd = "mkhltype -global " + ( comment != null ? "-c \"" + comment + "\"" : "" ) + typeName + "@" + pvob;
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new CleartoolException( "Could not create the hyperlink type " + typeName, e );
		}
	}

	@Override
	public void initialize() throws UCMEntityNotInitializedException {
		
		Matcher match = pattern_hlink_fqname.matcher( fqname );

		if( match.find() ) {
			this.type = match.group( 1 );
			shortname = match.group( 1 );
			pvob = new PVob( match.group( 3 ) );
			this.identifier = Integer.parseInt( match.group( 2 ) );
		} else {
			throw new UCMEntityNotInitializedException( fqname );
		}
	}

	public static HyperLink getHyperLink( String fqname, String value ) throws UnableToInitializeEntityException {
		HyperLink hlink = get( fqname );

		hlink.setValue( value );

		hlink.loaded = true;

		return hlink;
	}

	/* Getters */

	public String getValue() {
		return this.value;
	}

	/* Setters */

	public void setValue( String value ) {
		this.value = value;
	}
	
	public static HyperLink get( String name ) throws UnableToInitializeEntityException {
		if( !name.startsWith( "hlink:" ) ) {
			name = "hlink:" + name;
		}

		HyperLink entity = (HyperLink) UCMEntity.getEntity( HyperLink.class, name );
		return entity;
	}
}
