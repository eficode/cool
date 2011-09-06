package net.praqma.clearcase.ucm.entities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.utils.TagQuery;
import net.praqma.util.structure.Tuple;

public class Tag extends UCMEntity {
	/* Tag specific fields */
	private String tagType = "";
	private String tagID = "";
	private String OID = "";

	/* Indicates that the tag is newly created */
	private boolean created = false;

	private UCMEntity entity = null;

	// private static final Pattern pattern_cgi = Pattern.compile( "" );

	private Map<String, String> keyval = new HashMap<String, String>();

	Tag() {
		super( "tag" );
	}

	/**
	 * This method is only available to the package, because only
	 * ClearcaseEntity should be allowed to call it.
	 * 
	 * @return A new Tag Entity
	 */
	static Tag getEntity() {
		return new Tag();
	}

	public void postProcess() {
		this.OID = this.shortname;
	}

	public static Map<String, String> CGIToHash( String cgi ) {
		HashMap<String, String> hash = new HashMap<String, String>();
		if( UCM.isVerbose() ) {
			logger.debug( "cgi=" + cgi );
		}
		String[] entries = cgi.split( "&" );
		for( String e : entries ) {
			/* Check if the entry is a key value pair */
			if( !e.contains( "=" ) ) continue;

			String[] entry = e.split( "=" );
			hash.put( entry[0].trim(), entry[1].trim() );
		}

		return hash;
	}

	public boolean queryTag( TagQuery query ) {
		for( Tuple<String, String> t : query ) {
			if( this.keyval.containsKey( t.t1 ) ) {
				logger.debug( "(" + t.t1 + ", " + t.t2 + ") = " + this.keyval.get( t.t1 ) );

				Pattern pattern = Pattern.compile( t.t2 );
				Matcher match = pattern.matcher( this.keyval.get( t.t1 ) );

				// if( !this.keyval.get( t.t1 ).matches( t.t2 ) )
				if( !match.find() ) {
					return false;
				}
			} else {
				logger.debug( "(" + t.t1 + ", " + t.t2 + ") = " );

				/*
				 * Handling non-existing keys, if the condition can't fall back
				 * to an empty string, return false
				 */
				if( !"".matches( t.t2 ) ) {
					return false;
				}
			}
		}

		return true;
	}

	public static String mapToCGI( Map<String, String> keyval ) {
		return mapToCGI( keyval, false );
	}

	public static String mapToCGI( Map<String, String> keyval, boolean skiptaginfo ) {
		StringBuffer sb = new StringBuffer();
		Iterator<Entry<String, String>> it = keyval.entrySet().iterator();
		int c = 0;
		while( it.hasNext() ) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
			if( skiptaginfo && ( entry.getKey().equalsIgnoreCase( "tagid" ) || entry.getKey().equalsIgnoreCase( "tagtype" ) ) ) continue;
			sb.append( entry.getKey() + "=" + entry.getValue() + "&" );
			c++;
		}

		if( c > 0 ) {
			sb.deleteCharAt( sb.length() - 1 );
		}

		return sb.toString();
	}

	/**
	 * Is used by Stringify
	 */
	public void load() throws UCMException {
		Tuple<String, String> t = context.getTag( this );
		// this.OID = t.t1;

		// System.out.println( "CGI=" + t.t2 );

		keyval = Tag.CGIToHash( t.t2 );
		this.tagType = keyval.get( "tagtype" );
		this.tagID = keyval.get( "tagid" );

		this.loaded = true;
	}

	public void setKeyValue( String cgi ) {
		keyval = Tag.CGIToHash( cgi );
		this.tagType = ( keyval.containsKey( "tagtype" ) ? keyval.get( "tagtype" ) : "" );
		this.tagID = ( keyval.containsKey( "tagtype" ) ? keyval.get( "tagid" ) : "" );

		this.loaded = true;
	}

	public void setEntry( String key, String value ) {
		logger.log( "Setting " + key + " = " + value );
		keyval.put( key, value );
	}

	public boolean removeEntry( String key ) {
		if( keyval.containsKey( key ) ) {
			logger.log( "Removing " + key );
			keyval.remove( key );
			return true;
		} else {
			logger.log( "Could not remove " + key );
			return false;
		}
	}

	public String getEntry( String key ) {
		if( keyval.containsKey( key ) ) {
			return keyval.get( key );
		}

		return null;
	}

	public Map<String, String> GetEntries() {
		return this.keyval;
	}

	public void setTagEntity( UCMEntity entity ) {
		this.entity = entity;
	}

	public UCMEntity getTagEntity() {
		return this.entity;
	}

	/**
	 * Persist the Tag. Returns the new Tag from ClearCase.
	 * 
	 * @return The new Tag.
	 */
	public Tag persist() throws UCMException {
		if( UCM.isVerbose() ) {
			System.out.println( "Persisting tags." );
			// Printer.mapPrinter( this.GetEntries() );
		}

		return context.storeTag( this );
	}

	public void setCreated( boolean created ) {
		this.created = created;
	}

	public boolean isCreated() {
		return this.created;
	}

	public String getTagType() {
		return this.tagType;
	}

	public String getTagID() throws UCMException {
		return this.tagID;
	}

	public String stringify() throws UCMException {
		if( !this.loaded ) load();

		StringBuffer sb = new StringBuffer();

		sb.append( super.stringify() );

		sb.append( "Tag ID   : " + this.tagID + linesep );
		sb.append( "Tag Type : " + this.tagType + linesep );
		sb.append( "Tag OID  : " + this.OID + linesep );

		sb.append( "From obj : " + this.getTagEntity().toString() + linesep );

		sb.append( "Entries  : " + keyval.size() + " " );
		Iterator<Entry<String, String>> it = keyval.entrySet().iterator();
		while( it.hasNext() ) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
			sb.append( "(" + entry.getKey() + " = " + entry.getValue() + ")" );
			if( it.hasNext() ) sb.append( ", " );
		}

		return sb.toString();
	}
}
