package net.praqma.clearcase.ucm.entities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UCMEntityNotInitializedException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.util.debug.Logger;

public class HyperLink extends UCMEntity {
	
	public static final Pattern pattern_hlink_fqname = Pattern.compile( "^hlink:(" + rx_ccdef_allowed + "+)@(\\d+)@(" + rx_ccdef_vob + "+)$" );
	
	transient private static Logger logger = Logger.getLogger();
	
	/* Hlink specific fields */
	private Integer identifier = null;
	private String type = null;
	private String value = null;

	HyperLink() {
		super( "hlink" );
	}

	public static void create( String type, UCMEntity entity, String key, String value ) {

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

	public static HyperLink getHyperLink( String fqname, String value ) throws UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException {
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
	
	
	public static HyperLink get( String name ) throws UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException {
		return get( name, true );
	}

	public static HyperLink get( String name, boolean trusted ) throws UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException {
		if( !name.startsWith( "hlink:" ) ) {
			name = "hlink:" + name;
		}

		HyperLink entity = (HyperLink) UCMEntity.getEntity( HyperLink.class, name, trusted );
		return entity;
	}
}
