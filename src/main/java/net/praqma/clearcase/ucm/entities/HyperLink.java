package net.praqma.clearcase.ucm.entities;

import java.util.regex.Matcher;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.util.debug.Logger;

public class HyperLink extends UCMEntity {
	transient private static Logger logger = Logger.getLogger();
	
	/* Hlink specific fields */
	private Integer identifier = null;
	private String type = null;
	private String value = null;

	HyperLink() {
		super( "hlink" );
	}

	public void load() {

		this.loaded = true;
	}

	public static void create( String type, UCMEntity entity, String key, String value ) {

	}

	public void postProcess() {
		Matcher match = UCMEntity.pattern_hlink_fqname.matcher( this.fqname );

		if( !match.find() ) {
			logger.warning( "Rather odd, this shouldn've happened...." );
		} else {
			this.type = match.group( 1 );
			this.identifier = Integer.parseInt( match.group( 2 ) );
		}
	}

	public static HyperLink getHyperLink( String fqname, String value ) throws UCMException {
		HyperLink hlink = UCMEntity.getHyperLink( fqname );

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
}
