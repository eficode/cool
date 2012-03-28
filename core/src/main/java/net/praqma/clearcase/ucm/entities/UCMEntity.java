package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.TagException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UCMEntityNotInitializedException;
import net.praqma.clearcase.exceptions.HyperlinkException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToListAttributesException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.UnableToSetAttributeException;
import net.praqma.clearcase.exceptions.UnknownAttributeException;
import net.praqma.clearcase.exceptions.UnknownEntityException;
import net.praqma.clearcase.exceptions.UnknownEntityTypeException;
import net.praqma.clearcase.exceptions.UnknownUserException;
import net.praqma.clearcase.exceptions.UnknownVobException;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;
import net.praqma.util.structure.Tuple;

/**
 * 
 * @author wolfgang
 * 
 */
public abstract class UCMEntity extends UCM implements Serializable {

	transient private static Logger logger = Logger.getLogger();

	private static final long serialVersionUID = 1123123123L;

	protected static final String rx_ccdef_allowed = "[\\w\\.-]";
	protected static final String rx_ccdef_vob = "[\\\\\\w\\.-/]";
	protected static final Pattern pattern_std_fqname = Pattern.compile( "^(\\w+):(" + rx_ccdef_allowed + "+)@(" + rx_ccdef_vob + "+)$" );
	/* TODO Make a better character class definition for files(Version) */
	//private static final Pattern pattern_version_fqname = Pattern.compile( "^([\\S\\s\\\\\\/.^@]+)@@(" + rx_ccdef_vob + "+)$" );
	protected static final Pattern pattern_version_fqname = Pattern.compile( "^(.+)@@(.+)$" );
	protected static final String rx_ccdef_filename = "[\\S\\s\\\\\\/.^@]";
	//private static final Pattern pattern_version_fqname = Pattern.compile( "^(" + rx_ccdef_filename + "+)@@(?:(" + rx_ccdef_filename + ")@@)?(" + rx_ccdef_vob + "+)$" );
	
	protected static final Pattern pattern_tag_fqname = Pattern.compile( "^tag@(\\w+)@(" + rx_ccdef_vob + "+)$" );
	private static final Pattern pattern_hlink = Pattern.compile( "^\\s*(" + rx_ccdef_allowed + "+@\\d+@" + rx_ccdef_allowed + "+)\\s*->\\s*\"*(.*?)\"*\\s*$" );
	private static final Pattern pattern_hlink_type_missing = Pattern.compile( ".*Error: hyperlink type \"(.*?)\" not found in VOB \"(\\S+)\" .*" );
	private static final String rx_entityNotFound = "cleartool: Error: \\w+ not found: \"\\S+\"\\.";

	protected static final String rx_ccdef_cc_name = "[\\w\\.][\\w\\.-]*";
	
	private static final String rx_attr_find = "^\\s*\\S+\\s*=\\s*\\S*\\s*$";

	transient private static ClassLoader classloader = UCMEntity.class.getClassLoader();
	
	private boolean created = false;

	public enum LabelStatus {
		UNKNOWN,
		FULL,
		INCREMENTAL,
		UNLABLED
	}
	
	public enum Kind {
		UNKNOWN,
		DIRECTORY_ELEMENT,
		FILE_ELEMENT,
	}
	
	protected Kind kind = Kind.UNKNOWN;
	
	protected LabelStatus labelStatus = LabelStatus.UNKNOWN;


	protected Map<String, String> attributes = new HashMap<String, String>();
	
	protected Date date;
	
	protected static transient DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd.HHmmss"); // 20060810.225810
	
	transient private String comment;

	/* Fields that need not to be loaded */
	protected String fqname = "";
	protected String shortname = "";
	protected PVob pvob;
	protected Vob vob;

	protected String mastership = null;

	/* Loadable standard fields */
	protected String user = "";

	protected boolean loaded = false;
	private String entitySelector;
	
	public UCMEntity() {
		
	}

	protected UCMEntity( String entitySelector ) {
		this.entitySelector = entitySelector;
	}

	/**
	 * Generates a UCM entity given its fully qualified name.
	 * @return A new entity of the type given by the fully qualified name.
	 * @throws UnableToCreateEntityException 
	 * @throws UnableToInitializeEntityException 
	 */
	protected static UCMEntity getEntity( Class<? extends UCMEntity> clazz, String fqname ) throws UnableToInitializeEntityException {
		
		/* Is this needed? */
		fqname = fqname.trim();

		UCMEntity entity = null;
		String pvob = "";

		/* Try to instantiate the Entity object */
		try {
			entity = clazz.newInstance();
			entity.fqname = fqname;
			entity.initialize();
		} catch( Exception e ) {
			throw new UnableToInitializeEntityException( clazz, e );
		}

		/* Create the vob object */
		entity.vob = new PVob( pvob );

		return entity;
	}

	/**
	 * Initialize the UCM entity. This is a base implementation, storing the short name and pvob.
	 * @throws UCMEntityNotInitializedException 
	 */
	protected void initialize() throws UCMEntityNotInitializedException {
		Matcher match = pattern_std_fqname.matcher( fqname );
		if( match.find() ) {
			shortname = match.group( 2 );
			pvob = new PVob( match.group( 3 ) );
		} else {
			throw new UCMEntityNotInitializedException( fqname );
		}
	}

	/**
	 * Default load functionality for the entity. Every UCM entity should
	 * implement this method itself.
	 * @return 
	 * 
	 * @throws UnableToLoadEntityException 
	 * @throws UCMEntityNotFoundException 
	 * @throws UnableToCreateEntityException 
	 * @throws UnableToInitializeEntityException 
	 * @throws UnableToGetEntityException 
	 */
	public UCMEntity load() throws UnableToLoadEntityException, UCMEntityNotFoundException, UnableToInitializeEntityException {
		logger.debug( "Load method is not implemented for this Entity(" + this.fqname + ")" );
		this.loaded = true;
		
		return this;
	}
	
	public LabelStatus getLabelStatusFromString( String ls ) {
		if( ls.equalsIgnoreCase( "not labeled" ) ) {
			return LabelStatus.UNLABLED;
		} else if( ls.equalsIgnoreCase( "fully labeled" ) ) {
			return LabelStatus.FULL;
		} else if( ls.equalsIgnoreCase( "incrementally labeled" ) ) {
			return LabelStatus.INCREMENTAL;
		} else {
			return LabelStatus.UNKNOWN;
		}
	}
	
	public LabelStatus getLabelStatus() {
		return labelStatus;
	}
	
	public static UCMEntity getEntity( String fqname ) throws UnableToInitializeEntityException, UnknownEntityException {
		if( fqname.startsWith( "baseline:" ) ) {
			return Baseline.get( fqname );
		} else if( fqname.startsWith( "project:" ) ) {
			return Project.get( fqname );
		} else if( fqname.startsWith( "stream:" ) ) {
			return Stream.get( fqname );
		} else if( fqname.startsWith( "activity:" ) ) {
			return Activity.get( fqname );
		} else if( fqname.startsWith( "component:" ) ) {
			return Component.get( fqname );
		} else if ( fqname.startsWith( "folder:" ) ) {
			return Folder.get( fqname );
		}		
		
		throw new UnknownEntityException( fqname );
	}

	/* Syntactic static helper methods for retrieving entity objects */


	/* Tag stuff */

	public Tag getTag( String tagType, String tagID ) throws TagException, UnableToInitializeEntityException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		logger.debug( "Retrieving tags for " + tagType + ", " + tagID );
		return Tag.getTag( this, tagType, tagID, true );
	}

	/**
	 * Retrieve the attributes for an entity, executed from the current working
	 * directory
	 * 
	 * @return A Map of key, value pairs of the attributes
	 * @throws UnableToListAttributesException
	 */
	public static Map<String, String> getAttributes( UCMEntity entity, File context ) throws UnableToListAttributesException {
		String cmd = "describe -aattr -all " + entity;

		CmdResult res = null;
		try {
			res = Cleartool.run( cmd, context );
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Could not find attributes on " + fqname + ". Recieved: " + e.getMessage(), e.getMessage() );
			throw new UnableToListAttributesException( entity, context, e );
		}

		Map<String, String> atts = new HashMap<String, String>();

		for( String s : res.stdoutList ) {
			/* A valid attribute */
			if( s.matches( rx_attr_find ) ) {
				String[] data = s.split( "=" );
				atts.put( data[0].trim(), data[1].trim() );
			}
		}

		return atts;
	}
	
	public Map<String, String> getAttributes() throws UnableToListAttributesException {
		return UCMEntity.getAttributes( this, null );
	}
	
	public Map<String, String> getAttributes( File context ) throws UnableToListAttributesException {
		return UCMEntity.getAttributes( this, context );
	}
	
	public String getAttribute( String key ) throws UnableToListAttributesException {
		Map<String, String> atts = getAttributes( this, null );
		if( atts.containsKey( key ) ) {
			return atts.get( key );
		} else {
			return null;
		}
	}

	public void setAttribute( String attribute, String value ) throws UnableToSetAttributeException {
		setAttribute( attribute, value, null );
	}

	public void setAttribute( String attribute, String value, File context ) throws UnableToSetAttributeException {
		//context.setAttribute( this, attribute, value );
		logger.debug( "Setting attribute " + attribute + "=" + value + " for " + this );

		String cmd = "mkattr -replace " + attribute + " " + value + " " + this;
		try {
			Cleartool.run( cmd, context );
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Could not create the attribute " + attribute, e.getMessage() );
			throw new UnableToSetAttributeException( this, attribute, value, context, e );
		}
	}
	
	public List<HyperLink> getHyperlinks( String hyperlinkType, File context ) throws HyperlinkException, UnableToInitializeEntityException {
		String cmd = "describe -ahlink " + hyperlinkType + " -l " + this;

		CmdResult res = null;
		try {
			res = Cleartool.run( cmd, context );
		} catch( AbnormalProcessTerminationException e ) {
			Matcher match = pattern_hlink_type_missing.matcher( e.getMessage() );
			if( match.find() ) {
				//UCMException ucme = new UCMException( "ClearCase hyperlink type \"" + match.group( 1 ) + "\" was not found. ", e, UCMType.UNKNOWN_HLINK_TYPE );
				//ucme.addInformation(  "The Hyperlink type \"" + match.group( 1 ) + "\" was not found.\nInstallation: \"cleartool mkhltype -global -nc " + match.group( 1 ) + "@" + match.group( 2 ) );
				HyperlinkException ex = new HyperlinkException( this, context, match.group( 1 ), e );
				ex.addInformation(  "The Hyperlink type \"" + match.group( 1 ) + "\" was not found.\nInstallation: \"cleartool mkhltype -global -nc " + match.group( 1 ) + "@" + match.group( 2 ) );
				throw ex;
			} else {
				HyperlinkException ex = new HyperlinkException( this, context, hyperlinkType, e );
			}
		}

		List<String> list = res.stdoutList;

		//List<Tuple<String, String>> hlinks = new ArrayList<Tuple<String, String>>();
		List<HyperLink> hlinks = new ArrayList<HyperLink>();

		/* There are elements */
		if( list.size() > 2 ) {
			for( int i = 2; i < list.size(); i++ ) {
				logger.debug( "[" + i + "]" + list.get( i ) );
				Matcher match = pattern_hlink.matcher( list.get( i ) );
				if( match.find() ) {
					//hlinks.add( new Tuple<String, String>( match.group( 1 ).trim(), match.group( 2 ).trim() ) );
					
					HyperLink h = HyperLink.getHyperLink(  match.group( 1 ).trim(), match.group( 2 ).trim() );

					hlinks.add( h );
				}
			}
		}

		return hlinks;
	}

	/* Getters */

	public String getUser() {
		return this.user;
	}

	public void setUser( String user ) {
		this.user = user;
	}

	public String getFullyQualifiedName() {
		return this.fqname;
	}

	public String getShortname() {
		return this.shortname;
	}


	public PVob getPVob() {
		return pvob;
	}

	public String getMastership() throws CleartoolException {
		if( this.mastership == null ) {
			//this.mastership = context.getMastership( this );
			String cmd = "describe -fmt %[master]p " + fqname;

			CmdResult ms = null;

			try {
				ms = Cleartool.run( cmd );
			} catch( AbnormalProcessTerminationException e ) {
				throw new CleartoolException( "The mastership was undefined. ", e );
			}

			this.mastership = ms.stdoutBuffer.toString();
		}

		return this.mastership;
	}

	/**
	 * Returns a string representation of the entity object
	 * 
	 * @return A String
	 * @throws UCMException
	 * @throws UnableToLoadEntityException 
	 */
	public String stringify() {
		StringBuffer sb = new StringBuffer();

		sb.append( this.fqname + ":" + linesep );
		sb.append( " * Shortname: " + this.shortname + linesep );

		return sb.toString();
	}

	private static final Pattern pattern_name_part = Pattern.compile( "^(?:\\w+:)*(.*?)@" );

	public static String getNamePart( String fqname ) throws CleartoolException {
		Matcher m = pattern_name_part.matcher( fqname );

		if( m.find() ) {
			String name = m.group( 1 );
			if( name.matches( rx_ccdef_cc_name ) ) {
				return name;
			}
		}

		throw new CleartoolException( "Not a valid UCM name." );
	}

	/**
	 * @return A shorthand representation of the object.
	 */
	public String toString() {
		return this.getFullyQualifiedName();
	}

	public Date getDate() {
		return date;
	}
	
	public void setComment( String comment ) {
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}
	
	public Kind getKind() {
		return kind;
	}

	public void setKind( Kind kind ) {
		this.kind = kind;
	}
	
	public boolean equals( UCMEntity entity ) {
		return entity.getFullyQualifiedName().equals( this.getFullyQualifiedName() );
	}

	public String getEntitySelector() {
		return this.entitySelector;
	}
	
	public void changeOwnership( String username, File viewContext ) throws UnknownVobException, UnknownUserException, UCMEntityNotFoundException, CleartoolException {
		UCMEntity.changeOwnership( this, username, viewContext );
	}
	
	public static void changeOwnership( UCMEntity entity, String username, File viewContext ) throws UnknownVobException, UnknownUserException, UCMEntityNotFoundException, CleartoolException {
		String cmd = "protect -chown " + username + " \"" + entity + "\"";

		try {
			Cleartool.run( cmd, viewContext );
		} catch( AbnormalProcessTerminationException e ) {
			if( e.getMessage().contains( "Unable to determine VOB for pathname" ) ) {
				throw new UnknownVobException( e );
			}

			if( e.getMessage().contains( "Unknown user name" ) ) {
				throw new UnknownUserException( username, e );
			}

			if( e.getMessage().matches( rx_entityNotFound ) ) {
				throw new UCMEntityNotFoundException( entity, e );
			}

			if( e.getMessage().contains( " ClearCase object not found" ) ) {
				throw new UCMEntityNotFoundException( entity, e );
			}

			throw new CleartoolException( "Unable to change ownership of " + entity + " to " + username, e );
		}
	}
	
	public boolean isLoaded() {
		return this.loaded;
	}

	public boolean isCreated() {
		return created;
	}

	public void setCreated( boolean created ) {
		this.created = created;
	}
	
	public String getNormalizedName() {
		int idx = fqname.indexOf( ':' );
		if( idx < 0 ) {
			return fqname;
		} else {
			return fqname.substring( idx + 1 );
		}
	}
	
	public static String getargComment( String comment ) {
		return ( comment == null || comment.length() == 0 ? "-nc " : "-comment \"" + comment + "\"" );
	}
	
	public static String getargIn( String in ) {
		return "-in " + ( in == null ? "RootFolder" : in );
	}
}
