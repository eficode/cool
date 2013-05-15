package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.TagException;
import net.praqma.clearcase.exceptions.TagException.Type;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.utils.TagQuery;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.structure.Tuple;

public class Tag extends UCMEntity {

    /**
     * The tag name in ClearCase
     */
	public static final String __TAG_NAME = "tag";

	private static final Pattern pattern_hlink_type_missing = Pattern.compile( ".*Error: hyperlink type \"(.*?)\" not found in VOB \"(\\S+)\" .*" );
	private static final Pattern pattern_remove_verbose_tag = Pattern.compile( "^.*?\"(.*)\".*?$" );

	private static final Pattern pattern_tags = Pattern.compile( "^\\s*(.*?)\\s*->\\s*\"(.*?)\"\\s*$" );

	transient private static Logger logger = Logger.getLogger( Tag.class.getName() );

    /**
     * The tag type
     */
	private String tagType = "";

    /**
     * The tag id
     */
	private String tagID = "";

    /**
     * The object id
     */
	private String OID = "";

	/**
     *  Indicates that the tag is newly created
     */
	private boolean created = false;

	private UCMEntity entity = null;

	// private static final Pattern pattern_cgi = Pattern.compile( "" );

	private Map<String, String> keyval = new HashMap<String, String>();

	Tag() {
		super( "tag" );
	}

	@Override
	public void initialize() {
		Matcher match = pattern_tag_fqname.matcher( fqname );
		if( match.find() ) {
			shortname = match.group( 1 ); // This is also the eid
			pvob = new PVob( match.group( 2 ) );
		}
		
		this.OID = this.shortname;
	}

	public static Map<String, String> CGIToHash( String cgi ) {
		HashMap<String, String> hash = new HashMap<String, String>();
		logger.fine( "cgi=" + cgi );

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
				logger.fine( "(" + t.t1 + ", " + t.t2 + ") = " + this.keyval.get( t.t1 ) );

				Pattern pattern = Pattern.compile( t.t2 );
				Matcher match = pattern.matcher( this.keyval.get( t.t1 ) );

				// if( !this.keyval.get( t.t1 ).matches( t.t2 ) )
				if( !match.find() ) {
					return false;
				}
			} else {
				logger.fine( "(" + t.t1 + ", " + t.t2 + ") = " );

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
	 * 
	 * @return
	 */
	public Tag load() {
		// TODO This method was not implemented????
		//Tuple<String, String> t = context.getTag( this );
		// this.OID = t.t1;

		// System.out.println( "CGI=" + t.t2 );

		//keyval = Tag.CGIToHash( t.t2 );
		this.tagType = keyval.get( "tagtype" );
		this.tagID = keyval.get( "tagid" );

		this.loaded = true;

		return this;
	}

	public void setKeyValue( String cgi ) {
		keyval = Tag.CGIToHash( cgi );
		this.tagType = ( keyval.containsKey( "tagtype" ) ? keyval.get( "tagtype" ) : "" );
		this.tagID = ( keyval.containsKey( "tagid" ) ? keyval.get( "tagid" ) : "" );

		this.loaded = true;
	}

	public void setEntry( String key, String value ) {
		logger.fine( "Setting " + key + " = " + value );
		keyval.put( key, value );
	}

	public boolean removeEntry( String key ) {
		if( keyval.containsKey( key ) ) {
			logger.fine( "Removing " + key );
			keyval.remove( key );
			return true;
		} else {
			logger.fine( "Could not remove " + key );
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
	 * @throws UnableToInitializeEntityException 
	 * @throws TagException
	 * @throws UCMEntityNotFoundException 
	 * @throws UnableToLoadEntityException 
	 * @throws UnableToCreateEntityException 
	 * @throws UnableToGetEntityException 
	 */
	public Tag persist() throws TagException, UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException, UnableToGetEntityException, UnableToInitializeEntityException {
		return persist( this );
	}

	public static Tag persist( Tag tag ) throws TagException, UnableToCreateEntityException, UnableToLoadEntityException, UCMEntityNotFoundException, UnableToGetEntityException, UnableToInitializeEntityException {
		/* Make the new tag */
		Tag newtag = Tag.newTag( tag.getTagType(), tag.getTagID(), tag.getTagEntity(), Tag.mapToCGI( tag.GetEntries(), true ) );

		/* Delete the old tag */
		delete( tag );

		return newtag;
	}

	private static void delete( Tag tag ) {

	}

	public static List<Tag> getTags( UCMEntity entity ) throws TagException, UnableToInitializeEntityException {
		logger.fine( entity.toString() );

		String cmd = "describe -ahlink " + __TAG_NAME + " -l " + entity;
		CmdResult res = null;
		try {
			res = Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			Matcher match = pattern_hlink_type_missing.matcher( e.getMessage() );
			logger.warning( "Unable to get tags: " + e.getMessage() );
			if( match.find() ) {
				logger.fine( "Tag type not found" );
				//UCM.addMessage( "The Hyperlink type \"" + match.group( 1 ) + "\" was not found.\nInstallation: \"cleartool mkhltype " + __TAG_NAME + " -c \"Hyperlink type for tagging entities\"\"" );
				//throw new UCMException( "ClearCase hyperlink type \"" + match.group( 1 ) + "\" was not found. ", e.getMessage(), UCMType.UNKNOWN_HLINK_TYPE );
				//UCMException ucme = new UCMException( "ClearCase hyperlink type \"" + match.group( 1 ) + "\" was not found. ", e, UCMType.UNKNOWN_HLINK_TYPE );
				//ucme.addInformation(  "The Hyperlink type \"" + match.group( 1 ) + "\" was not found.\nInstallation: \"cleartool mkhltype -global -c \"Hyperlink type for tagging entities\" " + __TAG_NAME + "@" + match.group( 2 ) );
				TagException te = new TagException( entity, "", __TAG_NAME, Type.NO_SUCH_HYPERLINK, e );
				te.addInformation( "The Hyperlink type \"" + match.group( 1 ) + "\" was not found.\nInstallation: \"cleartool mkhltype -global -c \"Hyperlink type for tagging entities\" " + __TAG_NAME + "@" + match.group( 2 ) );
				throw te;
			} else {
				logger.fine( "Something else" );
				throw new TagException( entity, "", __TAG_NAME, Type.CREATION_FAILED, e );
			}
		}

		List<String> list = res.stdoutList;

		// List<Tuple<String, String>> tags = new ArrayList<Tuple<String,
		// String>>();
		//List<String[]> tags = new ArrayList<String[]>();
		
		ArrayList<Tag> tags = new ArrayList<Tag>();

		/* There are tags */
		if( list.size() > 2 ) {
			for( int i = 2; i < list.size(); i++ ) {
				logger.fine( "[" + i + "]" + list.get( i ) );
				
				Matcher match = pattern_tags.matcher( list.get( i ) );
				if( match.find() ) {
					// tags.add( new Tuple<String, String>( match.group( 1 ),
					// match.group( 2 ) ) );
					//tags.add( new String[] { match.group( 1 ), match.group( 2 ) } );
					
					Tag tag = (Tag) UCMEntity.getEntity( Tag.class, match.group( 1 ).trim() );
					tag.setKeyValue( match.group( 2 ) );
					tags.add( tag );
				}
			}
		}

		return tags;
	}

	private static void deleteTagsWithID( String tagType, String tagID, UCMEntity entity ) throws TagException, UnableToInitializeEntityException {
		logger.fine( tagType + tagID );

		List<Tag> list = getTags( entity );
		logger.fine( list.size() + " Tags!" );

		for( Tag t : list ) {
			logger.fine( "Testing " + t.getTagType() + " > " + t.getTagID() );

			if( t.getTagID().matches( "^.*tagtype=" + tagType + ".*$" ) && t.getTagID().matches( "^.*tagid=" + tagID + ".*$" ) ) {
				String cmd = "rmhlink " + t.getTagType();
				try {
					Cleartool.run( cmd );
				} catch( AbnormalProcessTerminationException e ) {
					//throw new UCMException( "Unable to delete tag with " + tagID + ": " + e.getMessage() );
					throw new TagException( entity, "", __TAG_NAME, Type.DELETION_FAILED, e );
				}
			}
		}

	}
	
	public static Tag getTag( UCMEntity entity, String tagType, String tagID, boolean create ) throws TagException, UnableToInitializeEntityException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		logger.fine( entity.toString() );
		List<Tag> tags = getTags( entity );

		for( Tag t : tags ) {
			logger.fine( "Current: " + t );
			/* Is it the correct tag? Return it! */
			if( t.getTagType().equals( tagType ) && t.getTagID().equals( tagID ) ) {
				logger.fine( "This is it!" );
				t.setTagEntity( entity );
				return t;
			}
		}

		logger.fine( "Could not find the Tag with ID " + tagType + tagID + ". Creating new." );

		if( create ) {
			return newTag( entity, tagType, tagID );
		} else {
			return null;
		}
	}
	
	private static Tag newTag( UCMEntity entity, String tagType, String tagID ) throws UnableToInitializeEntityException {
		Tag tag = (Tag) UCMEntity.getEntity( Tag.class, "tag@0@" + entity.getPVob().getName() );
		// tag.SetEntry( "tagtype", tagType );
		// tag.SetEntry( "tagid", tagID );
		String cgi = "tagtype=" + tagType + "&tagid=" + tagID;
		tag.setKeyValue( cgi );
		tag.setTagEntity( entity );

		tag.setCreated( true );

		return tag;
	}

	private static Tag newTag( String tagType, String tagID, UCMEntity entity, String cgi ) throws TagException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException, UnableToInitializeEntityException {
		logger.fine( "ENTITY=" + entity.toString() );
		logger.fine( "CGI FOR NEW = " + cgi );
		// System.out.println( "CGI==="+cgi );

		/* Delete any existing Tags with the unique ID */
		logger.fine( "Deleting Tags with ID: " + tagType + tagID + " for entity " + entity.getFullyQualifiedName() );
		deleteTagsWithID( tagType, tagID, entity );

		cgi = "tagtype=" + tagType + "&tagid=" + tagID + ( cgi.length() > 0 ? "&" + cgi : "" );
		String fqname = storeTag( entity, cgi );

		Tag tag = (Tag) UCMEntity.getEntity( Tag.class, fqname );
		// tag.SetEntry( "tagtype", tagType );
		// tag.SetEntry( "tagid", tagID );
		tag.setKeyValue( cgi );
		tag.setTagEntity( entity );

		return tag;
	}

	private static String storeTag( UCMEntity entity, String cgi ) throws TagException {
		logger.fine( entity.getFullyQualifiedName() );

		String cmd = "mkhlink -ttext \"" + cgi + "\" " + __TAG_NAME + " " + entity;

		CmdResult res = null;
		try {
			res = Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			logger.warning( "Unable add tag: " + e.getMessage() );
			Matcher match = pattern_hlink_type_missing.matcher( e.getMessage() );
			if( match.find() ) {
				logger.fine( "Found match" );
				//UCM.addMessage( "The Hyperlink type \"" + match.group( 1 ) + "\" was not found.\nInstallation: \"cleartool mkhltype " + __TAG_NAME + " -c \"Hyperlink type for tagging entities\"\"" );
				//throw new UCMException( "ClearCase hyperlink type \"" + match.group( 1 ) + "\" was not found.", e.getMessage(), UCMType.UNKNOWN_HLINK_TYPE );

				//UCMException ucme = new UCMException( "ClearCase hyperlink type \"" + match.group( 1 ) + "\" was not found. ", e, UCMType.UNKNOWN_HLINK_TYPE );
				//ucme.addInformation(  "The Hyperlink type \"" + match.group( 1 ) + "\" was not found.\nInstallation: \"cleartool mkhltype -global -c \"Hyperlink type for tagging entities\" " + __TAG_NAME + "@" + match.group( 2 ) );
				TagException te = new TagException( entity, cgi, __TAG_NAME, Type.NO_SUCH_HYPERLINK, e );
				te.addInformation( "The Hyperlink type \"" + match.group( 1 ) + "\" was not found.\nInstallation: \"cleartool mkhltype -global -c \"Hyperlink type for tagging entities\" " + __TAG_NAME + "@" + match.group( 2 ) );
				throw te;
			} else {
				//throw new UCMException( e );
				throw new TagException( entity, cgi, __TAG_NAME, Type.CREATION_FAILED, e );
			}
		}

		String tag = res.stdoutBuffer.toString();

		Matcher match = pattern_remove_verbose_tag.matcher( tag );
		if( !match.find() ) {
			//throw new UCMException( "Could not create tag", UCMType.TAG_CREATION_FAILED );
			throw new TagException( entity, cgi, __TAG_NAME, Type.CREATION_FAILED );
		}

		return match.group( 1 );
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

	public String getTagID() {
		return this.tagID;
	}

	public String stringify() {
		StringBuffer sb = new StringBuffer();

		try {
			if( !this.loaded ) load();

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
		} catch( Exception e ) {

		} finally {
			//sb.append( super.stringify() );
			sb.insert( 0, super.stringify() );
		}

		return sb.toString();
	}
	
	public String toString() {
		return tagType + "::" + tagID;
	}
}
