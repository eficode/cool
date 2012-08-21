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

import net.praqma.clearcase.ClearCase;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.EntityNotLoadedException;
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
public abstract class UCMEntity extends ClearCase implements Serializable {

	transient private static Logger logger = Logger.getLogger();

	private static final long serialVersionUID = 1123123123L;

	protected static final String rx_ccdef_allowed = "[\\w\\.-]";
	protected static final String rx_ccdef_vob = "[\\\\\\w\\./-]";
	protected static final Pattern pattern_std_fqname = Pattern.compile( "^(\\w+):(" + rx_ccdef_allowed + "+)@(" + rx_ccdef_vob + "+)$" );
	/* TODO Make a better character class definition for files(Version) */
private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	//private static final Pattern pattern_version_fqname = Pattern.compile( "^([\\S\\s\\\\\\/.^@]+)@@(" + rx_ccdef_vob + "+)$" );
	protected static final Pattern pattern_version_fqname = Pattern.compile( "^(.+)@@(.+)$" );
	protected static final String rx_ccdef_filename = "[\\S\\s\\\\\\/.^@]";
	//private static final Pattern pattern_version_fqname = Pattern.compile( "^(" + rx_ccdef_filename + "+)@@(?:(" + rx_ccdef_filename + ")@@)?(" + rx_ccdef_vob + "+)$" );

	protected static final Pattern pattern_tag_fqname = Pattern.compile( "^tag@(\\w+)@(" + rx_ccdef_vob + "+)$" );
	private static final Pattern pattern_hlink = Pattern.compile( "^\\s*(" + rx_ccdef_allowed + "+@\\d+@" + rx_ccdef_allowed + "+)\\s*->\\s*\"*(.*?)\"*\\s*$" );
	private static final Pattern pattern_hlink_type_missing = Pattern.compile( ".*Error: hyperlink type \"(.*?)\" not found in VOB \"(\\S+)\" .*" );
	private static final String rx_entityNotFound = "cleartool: Error: \\w+ not found: \"\\S+\"\\.";

	protected static final String rx_ccdef_cc_name = "[\\w\\.][\\w\\.-]*";

	transient private static ClassLoader classloader = UCMEntity.class.getClassLoader();

	private boolean created = false;

	public enum LabelStatus {
		UNKNOWN, FULL, INCREMENTAL, UNLABLED
	}

	public enum Kind {
		UNKNOWN, DIRECTORY_ELEMENT, FILE_ELEMENT, BRANCH, VERSION, STREAM, DERIVED_OBJECT, BRANCH_TYPE, LABEL_TYPE
	}

	protected Kind kind = Kind.UNKNOWN;

	protected LabelStatus labelStatus = LabelStatus.UNKNOWN;

	protected Date date;

	public static final transient DateFormat dateFormatter = new SimpleDateFormat( "yyyyMMdd.HHmmss" ); // 20060810.225810

	transient private String comment;

	/* Fields that need not to be loaded */
tracer.exiting(definition.class.getSimpleName(), "SimpleDateFormat");
tracer.entering(definition.class.getSimpleName(), "SimpleDateFormat");
	protected String fqname = "";
	protected String shortname = "";
	protected PVob pvob;
	protected Vob vob;

	protected String mastership = null;

	/* Loadable standard fields */
	protected String user = "";

	protected boolean loaded = false;
	private String entitySelector;

	private UCMEntity() {
tracer.entering(definition.class.getSimpleName(), "UCMEntity");

tracer.exiting(definition.class.getSimpleName(), "UCMEntity");
	}

	protected UCMEntity( String entitySelector ) {
tracer.entering(definition.class.getSimpleName(), "UCMEntity", new Object[]{entitySelector});
		this.entitySelector = entitySelector;
tracer.exiting(definition.class.getSimpleName(), "UCMEntity");
	}

	/**
	 * Generates a UCM entity given its fully qualified name.
	 * 
	 * @return A new entity of the type given by the fully qualified name.
	 * @throws UnableToCreateEntityException
	 * @throws UnableToInitializeEntityException
	 */
	protected static UCMEntity getEntity( Class<? extends UCMEntity> clazz, String fqname ) throws UnableToInitializeEntityException {
tracer.entering(definition.class.getSimpleName(), "getEntity", new Object[]{extends, clazz, fqname});

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

tracer.exiting(definition.class.getSimpleName(), "getEntity", entity);
		return entity;
	}

	/**
	 * Initialize the UCM entity. This is a base implementation, storing the
	 * short name and pvob.
	 * 
	 * @throws UCMEntityNotInitializedException
	 */
	protected void initialize() throws UCMEntityNotInitializedException {
tracer.entering(definition.class.getSimpleName(), "initialize");
		Matcher match = pattern_std_fqname.matcher( fqname );
		if( match.find() ) {
			shortname = match.group( 2 );
			pvob = new PVob( match.group( 3 ) );
		} else {
			throw new UCMEntityNotInitializedException( fqname );
		}
tracer.exiting(definition.class.getSimpleName(), "initialize");
	}

	/**
	 * Default load functionality for the entity. Every UCM entity should
	 * implement this method itself.
	 * 
	 * @return
	 * 
	 * @throws UnableToLoadEntityException
	 * @throws UCMEntityNotFoundException
	 * @throws UnableToCreateEntityException
	 * @throws UnableToInitializeEntityException
	 * @throws UnableToGetEntityException
	 */
	public UCMEntity load() throws UnableToLoadEntityException, UCMEntityNotFoundException, UnableToInitializeEntityException {
tracer.entering(definition.class.getSimpleName(), "load");
		logger.debug( "Load method is not implemented for this Entity(" + this.fqname + ")" );
		this.loaded = true;

tracer.exiting(definition.class.getSimpleName(), "load", this);
		return this;
	}

	public LabelStatus getLabelStatusFromString( String ls ) {
tracer.entering(definition.class.getSimpleName(), "getLabelStatusFromString", new Object[]{ls});
		if( ls.equalsIgnoreCase( "not labeled" ) ) {
tracer.exiting(definition.class.getSimpleName(), "getLabelStatusFromString", LabelStatus.UNLABLED);
			return LabelStatus.UNLABLED;
		} else if( ls.equalsIgnoreCase( "fully labeled" ) ) {
tracer.exiting(definition.class.getSimpleName(), "getLabelStatusFromString", LabelStatus.FULL);
			return LabelStatus.FULL;
		} else if( ls.equalsIgnoreCase( "incrementally labeled" ) ) {
tracer.exiting(definition.class.getSimpleName(), "getLabelStatusFromString", LabelStatus.INCREMENTAL);
			return LabelStatus.INCREMENTAL;
		} else {
tracer.exiting(definition.class.getSimpleName(), "getLabelStatusFromString", LabelStatus.UNKNOWN);
			return LabelStatus.UNKNOWN;
		}
	}

	public LabelStatus getLabelStatus() {
tracer.entering(definition.class.getSimpleName(), "getLabelStatus");
tracer.exiting(definition.class.getSimpleName(), "getLabelStatus", labelStatus);
		return labelStatus;
	}

	public static UCMEntity getEntity( String fqname ) throws UnableToInitializeEntityException, UnknownEntityException {
tracer.entering(definition.class.getSimpleName(), "getEntity", new Object[]{fqname});
		if( fqname.startsWith( "baseline:" ) ) {
tracer.exiting(definition.class.getSimpleName(), "getEntity", Baseline.get( fqname ));
			return Baseline.get( fqname );
		} else if( fqname.startsWith( "project:" ) ) {
tracer.exiting(definition.class.getSimpleName(), "getEntity", Project.get( fqname ));
			return Project.get( fqname );
		} else if( fqname.startsWith( "stream:" ) ) {
tracer.exiting(definition.class.getSimpleName(), "getEntity", Stream.get( fqname ));
			return Stream.get( fqname );
		} else if( fqname.startsWith( "activity:" ) ) {
tracer.exiting(definition.class.getSimpleName(), "getEntity", Activity.get( fqname ));
			return Activity.get( fqname );
		} else if( fqname.startsWith( "component:" ) ) {
tracer.exiting(definition.class.getSimpleName(), "getEntity", Component.get( fqname ));
			return Component.get( fqname );
		} else if( fqname.startsWith( "folder:" ) ) {
tracer.exiting(definition.class.getSimpleName(), "getEntity", Folder.get( fqname ));
			return Folder.get( fqname );
		}

		throw new UnknownEntityException( fqname );
	}

	/* Syntactic static helper methods for retrieving entity objects */

	/* Tag stuff */

	public Tag getTag( String tagType, String tagID ) throws TagException, UnableToInitializeEntityException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
tracer.entering(definition.class.getSimpleName(), "getTag", new Object[]{tagType, tagID});
		logger.debug( "Retrieving tags for " + tagType + ", " + tagID );
tracer.exiting(definition.class.getSimpleName(), "getTag", Tag.getTag( this, tagType, tagID, true ));
		return Tag.getTag( this, tagType, tagID, true );
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
tracer.exiting(definition.class.getSimpleName(), "UCMException");
tracer.entering(definition.class.getSimpleName(), "UCMException", new Object[]{hyperlink, \"", match.group(, ), "\", not, ", UCMType.UNKNOWN_HLINK_TYPE});
				//ucme.addInformation(  "The Hyperlink type \"" + match.group( 1 ) + "\" was not found.\nInstallation: \"cleartool mkhltype -global -nc " + match.group( 1 ) + "@" + match.group( 2 ) );
				HyperlinkException ex = new HyperlinkException( this, context, match.group( 1 ), e );
tracer.exiting(definition.class.getSimpleName(), "HyperlinkException");
tracer.entering(definition.class.getSimpleName(), "HyperlinkException", new Object[]{context, 1, e});
				ex.addInformation( "The Hyperlink type \"" + match.group( 1 ) + "\" was not found.\nInstallation: \"cleartool mkhltype -global -nc " + match.group( 1 ) + "@" + match.group( 2 ) );
				throw ex;
			} else {
				HyperlinkException ex = new HyperlinkException( this, context, hyperlinkType, e );
tracer.exiting(definition.class.getSimpleName(), "HyperlinkException");
tracer.entering(definition.class.getSimpleName(), "HyperlinkException", new Object[]{context, e});
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

					HyperLink h = HyperLink.getHyperLink( match.group( 1 ).trim(), match.group( 2 ).trim() );

					hlinks.add( h );
				}
			}
		}

		return hlinks;
	}

	/* Getters */

	public String getUser() {
tracer.entering(definition.class.getSimpleName(), "getUser");
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
tracer.exiting(definition.class.getSimpleName(), "getUser", this.user);
		return this.user;
	}

	public void setUser( String user ) {
tracer.entering(definition.class.getSimpleName(), "setUser", new Object[]{user});
		this.user = user;
tracer.exiting(definition.class.getSimpleName(), "setUser");
	}

        @Override
	public String getFullyQualifiedName() {
tracer.entering(definition.class.getSimpleName(), "getFullyQualifiedName");
tracer.exiting(definition.class.getSimpleName(), "getFullyQualifiedName", this.fqname);
		return this.fqname;
	}

	public String getShortname() {
tracer.entering(definition.class.getSimpleName(), "getShortname");
tracer.exiting(definition.class.getSimpleName(), "getShortname", this.shortname);
		return this.shortname;
	}

	public PVob getPVob() {
tracer.entering(definition.class.getSimpleName(), "getPVob");
tracer.exiting(definition.class.getSimpleName(), "getPVob", pvob);
		return pvob;
	}

	public void setMastership( String mastership ) throws CleartoolException {
tracer.entering(definition.class.getSimpleName(), "setMastership", new Object[]{mastership});
		if( this.mastership != mastership ) {
			this.mastership = mastership;

			String cmd = "chmaster replica:" + mastership + " " + fqname;

			try {
				Cleartool.run( cmd );
			} catch( AbnormalProcessTerminationException e ) {
				throw new CleartoolException( "Could not set mastership. ", e );
			}

		}
tracer.exiting(definition.class.getSimpleName(), "setMastership");
	}

	public String getMastership() {
tracer.entering(definition.class.getSimpleName(), "getMastership");
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
tracer.exiting(definition.class.getSimpleName(), "getMastership", this.mastership);
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
tracer.entering(definition.class.getSimpleName(), "stringify");
		StringBuffer sb = new StringBuffer();

		sb.append( this.fqname + ":" + linesep );
		sb.append( " * Shortname: " + this.shortname + linesep );

tracer.exiting(definition.class.getSimpleName(), "stringify", sb.toString());
		return sb.toString();
	}

	private static final Pattern pattern_name_part = Pattern.compile( "^(?:\\w+:)*(.*?)@" );

	public static String getNamePart( String fqname ) throws CleartoolException {
tracer.entering(definition.class.getSimpleName(), "getNamePart", new Object[]{fqname});
		Matcher m = pattern_name_part.matcher( fqname );

		if( m.find() ) {
			String name = m.group( 1 );
			if( name.matches( rx_ccdef_cc_name ) ) {
tracer.exiting(definition.class.getSimpleName(), "getNamePart", name);
				return name;
			}
		}

		throw new CleartoolException( "Not a valid UCM name." );
	}

	/**
	 * @return A shorthand representation of the object.
	 */
	public String toString() {
tracer.entering(definition.class.getSimpleName(), "toString");
tracer.exiting(definition.class.getSimpleName(), "toString", this.getFullyQualifiedName());
		return this.getFullyQualifiedName();
	}

	public Date getDate() {
tracer.entering(definition.class.getSimpleName(), "getDate");
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
tracer.exiting(definition.class.getSimpleName(), "getDate", date);
		return date;
	}

	public void setComment( String comment ) {
tracer.entering(definition.class.getSimpleName(), "setComment", new Object[]{comment});
		this.comment = comment;
tracer.exiting(definition.class.getSimpleName(), "setComment");
	}

	public String getComment() {
tracer.entering(definition.class.getSimpleName(), "getComment");
tracer.exiting(definition.class.getSimpleName(), "getComment", comment);
		return comment;
	}

	public Kind getKind() {
tracer.entering(definition.class.getSimpleName(), "getKind");
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
tracer.exiting(definition.class.getSimpleName(), "getKind", kind);
		return kind;
	}

	public void setKind( Kind kind ) {
tracer.entering(definition.class.getSimpleName(), "setKind", new Object[]{kind});
		this.kind = kind;
tracer.exiting(definition.class.getSimpleName(), "setKind");
	}

	@Override
	public boolean equals( Object other ) {
tracer.entering(definition.class.getSimpleName(), "equals", new Object[]{other});
		if( other instanceof UCMEntity ) {
tracer.exiting(definition.class.getSimpleName(), "equals", ((UCMEntity)other).getFullyQualifiedName().equals( this.getFullyQualifiedName() ));
			return ((UCMEntity)other).getFullyQualifiedName().equals( this.getFullyQualifiedName() );
		} else {
tracer.exiting(definition.class.getSimpleName(), "equals", false);
			return false;
		}
	}

	public String getEntitySelector() {
tracer.entering(definition.class.getSimpleName(), "getEntitySelector");
tracer.exiting(definition.class.getSimpleName(), "getEntitySelector", this.entitySelector);
		return this.entitySelector;
	}

	public void changeOwnership( String username, File viewContext ) throws UnknownVobException, UnknownUserException, UCMEntityNotFoundException, CleartoolException {
tracer.entering(definition.class.getSimpleName(), "changeOwnership", new Object[]{username, viewContext});
		UCMEntity.changeOwnership( this, username, viewContext );
tracer.exiting(definition.class.getSimpleName(), "changeOwnership");
	}

	public static void changeOwnership( UCMEntity entity, String username, File viewContext ) throws UnknownVobException, UnknownUserException, UCMEntityNotFoundException, CleartoolException {
tracer.entering(definition.class.getSimpleName(), "changeOwnership", new Object[]{entity, username, viewContext});
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
tracer.exiting(definition.class.getSimpleName(), "changeOwnership");
	}

	public boolean isLoaded() {
tracer.entering(definition.class.getSimpleName(), "isLoaded");
tracer.exiting(definition.class.getSimpleName(), "isLoaded", this.loaded);
		return this.loaded;
	}

	public boolean isCreated() {
tracer.entering(definition.class.getSimpleName(), "isCreated");
tracer.exiting(definition.class.getSimpleName(), "isCreated", created);
		return created;
	}

	public void setCreated( boolean created ) {
tracer.entering(definition.class.getSimpleName(), "setCreated", new Object[]{created});
		this.created = created;
tracer.exiting(definition.class.getSimpleName(), "setCreated");
	}

	public String getNormalizedName() {
tracer.entering(definition.class.getSimpleName(), "getNormalizedName");
		int idx = fqname.indexOf( ':' );
		if( idx < 0 ) {
tracer.exiting(definition.class.getSimpleName(), "getNormalizedName", fqname);
			return fqname;
		} else {
tracer.exiting(definition.class.getSimpleName(), "getNormalizedName", fqname.substring( idx + 1 ));
			return fqname.substring( idx + 1 );
		}
	}

	public static String getargComment( String comment ) {
tracer.entering(definition.class.getSimpleName(), "getargComment", new Object[]{comment});
tracer.exiting(definition.class.getSimpleName(), "getargComment", ( comment == null || comment.length() == 0 ? "-nc " : "-comment \"" + comment + "\"" ));
		return ( comment == null || comment.length() == 0 ? "-nc " : "-comment \"" + comment + "\"" );
	}

	public static String getargIn( String in ) {
tracer.entering(definition.class.getSimpleName(), "getargIn", new Object[]{in});
tracer.exiting(definition.class.getSimpleName(), "getargIn", "-in " + ( in == null ? "RootFolder" : in ));
		return "-in " + ( in == null ? "RootFolder" : in );
	}
}
