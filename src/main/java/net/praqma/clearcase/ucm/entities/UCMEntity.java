package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.ClearCase;
import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.clearcase.api.Describe;
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
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.UnknownEntityException;
import net.praqma.clearcase.exceptions.UnknownUserException;
import net.praqma.clearcase.exceptions.UnknownVobException;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

/**
 * 
 * @author wolfgang
 * 
 */
public abstract class UCMEntity extends ClearCase implements Serializable {

	transient private static Logger logger = Logger.getLogger( UCMEntity.class.getName() );

	//protected static final String rx_ccdef_allowed = "[\\w\\.-]";
    protected static final String rx_ccdef_allowed = "[.[^@:\\s]]";
	protected static final String rx_ccdef_vob = "[\\\\\\w\\./-]";
	protected static final Pattern pattern_std_fqname = Pattern.compile( "^(\\w+):(" + rx_ccdef_allowed + "+)@(" + rx_ccdef_allowed + "+)$" );
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

	}

	protected UCMEntity( String entitySelector ) {
		this.entitySelector = entitySelector;
	}

	/**
	 * Generates a UCM entity given its fully qualified name.
	 * 
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
	 * Initialize the UCM entity. This is a base implementation, storing the
	 * short name and pvob.
	 * 
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
		logger.fine( "Load method is not implemented for this Entity(" + this.fqname + ")" );
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
		} else if( fqname.startsWith( "folder:" ) ) {
			return Folder.get( fqname );
		}

		throw new UnknownEntityException( fqname );
	}

	/* Syntactic static helper methods for retrieving entity objects */

	/* Tag stuff */

	public Tag getTag( String tagType, String tagID ) throws TagException, UnableToInitializeEntityException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		logger.fine( "Retrieving tags for " + tagType + ", " + tagID );
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
				HyperlinkException ex = new HyperlinkException( this, context, match.group( 1 ), e );
				ex.addInformation( "The Hyperlink type \"" + match.group( 1 ) + "\" was not found.\nInstallation: \"cleartool mkhltype -shared -global -nc " + match.group( 1 ) + "@" + match.group( 2 ) );
				throw ex;
			} else {
				HyperlinkException ex = new HyperlinkException( this, context, hyperlinkType, e );
			}
		}

		List<String> list = res.stdoutList;

		List<HyperLink> hlinks = new ArrayList<HyperLink>();

		/* There are elements */
		if( list.size() > 2 ) {
			for( int i = 2; i < list.size(); i++ ) {
				Matcher match = pattern_hlink.matcher( list.get( i ) );
				if( match.find() ) {
					HyperLink h = HyperLink.getHyperLink( match.group( 1 ).trim(), match.group( 2 ).trim() );
					hlinks.add( h );
				}
			}
		}

		return hlinks;
	}

	/* Getters */

	public String getUser() {
		if( user == null && !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		return this.user;
	}

    /**
     * Set the user responsible for this entity
     * @param user
     */
	public void setUser( String user ) {
		this.user = user;
	}

        @Override
	public String getFullyQualifiedName() {
		return this.fqname;
	}
        
        public String getFqname() {
            return this.fqname;
        }
        
        public void setFqname(String fqname) {
            this.fqname = fqname;
        }

	public String getShortname() {
		return this.shortname;
	}

	public PVob getPVob() {
		return pvob;
	}

	public void setMastership( String mastership ) throws CleartoolException {
		if( this.mastership != mastership ) {
			this.mastership = mastership;

			String cmd = "chmaster replica:" + mastership + " " + fqname;

			try {
				Cleartool.run( cmd );
			} catch( AbnormalProcessTerminationException e ) {
				throw new CleartoolException( "Could not set mastership. ", e );
			}

		}
	}

	public String getMastership() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		return this.mastership;
	}

	/**
	 * Returns a string representation of the entity object
	 * 
	 * @return A String
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
        /* First try to do auto load */
        if( date == null ) {
		    autoLoad();
        }

        /* If auto load doesn't include the date, load it separately */
        if( date == null ) {
            try {
                loadDate();
            } catch( CleartoolException e ) {
                throw new EntityNotLoadedException( fqname, fqname + " could not be load date", e );
            }
        }

		return date;
	}

    public void loadDate() throws CleartoolException {

        String result = "";

        String cmd = "desc -fmt %Nd " + "\""+this + "\"";
        try {
            result = Cleartool.run( cmd ).stdoutBuffer.toString();
        } catch( Exception e ) {
            throw new CleartoolException( "Unable to load date for " + this.getNormalizedName(), e );
        }

        try {
            logger.fine( "Result:" + result );
            synchronized( dateFormatter ) {
                this.date = dateFormatter.parse( result );
            }
        } catch( ParseException e ) {
            logger.fine( "Unable to parse date: " + e.getMessage() );
            this.date = null;
        }
    }

    public void setDate( Date date ) {
        this.date = date;
    }

    public void setDate( String date ) throws ParseException {
        synchronized( dateFormatter ) {
            this.date = dateFormatter.parse( date );
        }
    }

	public void setComment( String comment ) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

	public Kind getKind() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		return kind;
	}

	public void setKind( Kind kind ) {
		this.kind = kind;
	}

	@Override
	public boolean equals( Object other ) {
		if( other instanceof UCMEntity ) {
			return ((UCMEntity)other).getFullyQualifiedName().equals( this.getFullyQualifiedName() );
		} else {
			return false;
		}
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

    protected void autoLoad() throws EntityNotLoadedException {
        if( !loaded ) {
            try {
                load();
                loaded = true; // TODO Should not be necessary
            } catch( ClearCaseException e ) {
                throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
            }
        }
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

    public String getObjectId() throws CleartoolException {
        return new Describe( this ).getObjectId().executeGetFirstLine();
    }

    public static String getObjectId( String name ) throws CleartoolException {
        return new Describe( name ).getObjectId().executeGetFirstLine();
    }

    @Override
    public int hashCode() {
        return getFullyQualifiedName().hashCode();
    }
}
