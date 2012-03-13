package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UCMEntityNotInitializedException;
import net.praqma.clearcase.exceptions.UCMException;
import net.praqma.clearcase.exceptions.UCMException.UCMType;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.UnknownEntityTypeException;
import net.praqma.util.debug.Logger;

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

	protected static final String rx_ccdef_cc_name = "[\\w\\.][\\w\\.-]*";

	transient private static ClassLoader classloader = UCMEntity.class.getClassLoader();

	transient protected static TagPool tp = TagPool.GetInstance();
	
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
	
	DateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd.HHmmss"); // 20060810.225810
	
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
	 * 
	 * @param fqname
	 *            The fully qualified name
	 * @param trusted
	 *            If not trusted, the entity's content is loaded from clear
	 *            case.
	 * @param cachable
	 *            If cachable, the entity is stored and can later be retrieved
	 *            from the cache without contacting clear case.
	 * @return A new entity of the type given by the fully qualified name.
	 */
	protected static UCMEntity getEntity( Class<? extends UCMEntity> clazz, String fqname, boolean trusted ) {
		
		/* Is this needed? */
		fqname = fqname.trim();

		UCMEntity entity = null;

		String shortname = "";
		String pvob = "";

		/* Test standard fully qualified names */
		Matcher match = pattern_std_fqname.matcher( fqname );
		if( match.find() ) {
			ClearcaseEntityType etype = ClearcaseEntityType.GetFromString( match.group( 1 ) );

			/* Set the Entity variables */
			shortname = match.group( 2 );
			pvob = match.group( 3 );
			type = etype;
		} else {

			/* Not a standard entity, lets try Version */
			match = pattern_version_fqname.matcher( fqname );
			if( match.find() ) {
				/* Set the Entity variables */
				shortname = match.group( 1 );
				pvob = match.group( 2 );
				type = ClearcaseEntityType.Version;
			} else {

				/* Not a Version entity, lets try Tag */
				match = pattern_tag_fqname.matcher( fqname );
				if( match.find() ) {
					/* Set the Entity variables */
					shortname = match.group( 1 ); // This is also the eid
					pvob = match.group( 2 );
					type = ClearcaseEntityType.Tag;
				} else {
					/* Not a Tag entity, lets try HLink(which really is a Tag) */
					match = pattern_hlink_fqname.matcher( fqname );
					if( match.find() ) {
						/* Set the Entity variables */
						shortname = match.group( 1 );
						pvob = match.group( 3 );
						type = ClearcaseEntityType.HyperLink;
					}
				}
			}
		}

		/* Try to instantiate the Entity object */
		entity = clazz.newInstance();

		/* Storing the variables in the object */
		/*
		entity.fqname = fqname;
		entity.shortname = shortname;
		entity.type = type;
		entity.pvob = pvob;
		*/

		// logger.debug( "Created entity of type " + entity.type );

		entity.initialize();

		/* If not trusted, load the entity from the context */
		if( !trusted ) {
			entity.load();
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
	 */
	public UCMEntity load() throws UnableToLoadEntityException, UCMEntityNotFoundException {
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

	/* Syntactic static helper methods for retrieving entity objects */











	
	

	/* Tag stuff */

	public Tag getTag( String tagType, String tagID ) throws UCMException {
		if( UCM.isVerbose() ) {
			System.out.println( "Retrieving tags for " + tagType + ", " + tagID );
		}

		return tp.getTag( tagType, tagID, this );
	}

	/**
	 * Retrieve the attributes for an entity, executed from the current working
	 * directory
	 * 
	 * @return A Map of key, value pairs of the attributes
	 * @throws UCMException
	 */
	public Map<String, String> getAttributes() throws UCMException {
		return context.getAttributes( this );
	}

	public String getAttribute( String key ) throws UCMException {
		Map<String, String> atts = this.getAttributes();
		if( atts.containsKey( key ) ) {
			return atts.get( key );
		} else {
			return null;
		}
	}

	/**
	 * Retrieve the attributes for an entity
	 * 
	 * @param dir
	 *            A File object of the directory where the command should be
	 *            executed
	 * @return A Map of key, value pairs of the attributes
	 * @throws UCMException
	 */
	public Map<String, String> getAttributes( File dir ) throws UCMException {
		return context.getAttributes( this, dir );
	}

	public void setAttribute( String attribute, String value ) throws UCMException {
		context.setAttribute( this, attribute, value );
	}

	public List<HyperLink> getHlinks( String hlinkType, File dir ) throws UCMException {
		logger.debug( "THIS=" + this.getFullyQualifiedName() );
		return context.getHlinks( this, hlinkType, dir );
	}

	/* Getters */

	public String getUser() throws UCMException {
		if( !loaded ) {
			load();
		}
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

	public String getPvobString() {
		return this.pvob;
	}

	public PVob getPVob() {
		return vob;
	}

	public String getMastership() throws UCMException {
		if( this.mastership == null ) {
			this.mastership = context.getMastership( this );
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

	public static String getNamePart( String fqname ) throws UCMException {
		Matcher m = pattern_name_part.matcher( fqname );

		if( m.find() ) {
			String name = m.group( 1 );
			if( name.matches( rx_ccdef_cc_name ) ) {
				return name;
			}
		}

		throw new UCMException( "Not a valid UCM name.", UCMType.ENTITY_NAME_ERROR );
	}

	/**
	 * @return A shorthand representation of the object.
	 */
	public String toString() {
		return this.getFullyQualifiedName();
	}

	public String getXML() {
		return context.getXML();
	}

	public static void saveState() {
		context.saveState();
	}
	
	public Date getDate() {
		if(!loaded) try {
			this.load();
		} catch (UCMException e) {
			logger.error( "UNable to load entity" );
		}
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
	
	public void changeOwnership( String username, File viewContext ) throws UCMException {
		context.changeOwnership( this, username, viewContext );
	}
	
	public static void changeOwnership( String fqname, String username, File viewContext ) throws UCMException {
		context.changeOwnership( fqname, username, viewContext );
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
}
