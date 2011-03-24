package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.UCMException.UCMType;
import net.praqma.clearcase.ucm.persistence.*;
import net.praqma.util.structure.Tuple;

/**
 * 
 * @author wolfgang
 *
 */
public abstract class UCMEntity extends UCM
{
	private static final String rx_ccdef_allowed        = "[\\w\\.-]";
	private static final String rx_ccdef_vob            = "[\\\\\\w\\.-]";
	private static final Pattern pattern_std_fqname     = Pattern.compile( "^(\\w+):(" + rx_ccdef_allowed + "+)@(" + rx_ccdef_vob + "+)$" );
	/* TODO Make a better character class definition for files(Version) */
	private static final Pattern pattern_version_fqname = Pattern.compile( "^(\\w:[\\S\\s\\\\\\.]+)@@(" + rx_ccdef_vob + "+)$" );
	public static final Pattern pattern_hlink_fqname    = Pattern.compile( "^hlink:(" + rx_ccdef_allowed + "+)@(\\d+)@(" + rx_ccdef_vob + "+)$" );
	protected static final Pattern pattern_tag_fqname   = Pattern.compile( "^tag@(\\w+)@(" + rx_ccdef_vob + "+)$" );
	
	protected static final String rx_ccdef_cc_name      = "[\\w\\.][\\w\\.-]*";
	
	private static ClassLoader classloader = UCMEntity.class.getClassLoader();
		
	protected static TagPool tp = TagPool.GetInstance();
	
	/* For future caching purposes */
	private static HashMap<String, UCMEntity> entities = new HashMap<String, UCMEntity>();
	
	protected Map<String, String> attributes = new HashMap<String, String>();
	
	public enum ClearcaseEntityType
	{
		Activity,
		Baseline,
		Component,
		HyperLink,
		Stream,
		Project,
		Tag,
		Version,
		Undefined;
		
		static ClearcaseEntityType GetFromString( String type )
		{
			if( type.equalsIgnoreCase( "baseline" ) )
			{
				return Baseline;
			}
			else if( type.equalsIgnoreCase( "activity" ) )
			{
				return Activity;
			}
			else if( type.equalsIgnoreCase( "project" ) )
			{
				return Project;
			}
			else if( type.equalsIgnoreCase( "stream" ) )
			{
				return Stream;
			}
			else if( type.equalsIgnoreCase( "component" ) )
			{
				return Component;
			}
			
			return Undefined;
		}
	}
	
	/* Fields that need not to be loaded */
	protected String fqname            = "";
	protected String shortname         = "";
	protected ClearcaseEntityType type = ClearcaseEntityType.Undefined;
	protected String pvob              = "";
	
	protected String mastership        = null;
	
	/* Loadable standard fields */
	protected String user = "";
	
	protected boolean loaded = false;
	
	public UCMEntity( )
	{
		
	}
	
	/**
	 * Overloaded method, defaulting trusted to true and cachable to false.
	 * @param fqname The fully qualified name
	 * @return A new entity of the type given by the fully qualified name.
	 * @throws UCMEntityException
	 */
	public static UCMEntity GetEntity( String fqname ) throws UCMException
	{
		return GetEntity( fqname, true, false );
	}
	
	/**
	 * Overloaded method, defaulting cachable to false.
	 * @param fqname The fully qualified name
	 * @param trusted If not trusted, the entity's content is loaded from clear case.
	 * @return A new entity of the type given by the fully qualified name.
	 * @throws UCMEntityException
	 */
	public static UCMEntity GetEntity( String fqname, boolean trusted ) throws UCMException
	{
		return GetEntity( fqname, trusted, false );
	}
	
	/**
	 * Generates a UCM entity given its fully qualified name.
	 * @param fqname The fully qualified name
	 * @param trusted If not trusted, the entity's content is loaded from clear case.
	 * @param cachable If cachable, the entity is stored and can later be retrieved from the cache without contacting clear case.
	 * @return A new entity of the type given by the fully qualified name.
	 * @throws UCMEntityException This exception is thrown if the type of the entity is not recognized, the entity class is not found or the default constructor is not found.
	 */
	public static UCMEntity GetEntity( String fqname, boolean trusted, boolean cachable ) throws UCMException
	{
		//logger.debug( "GetEntity = " + fqname );
		
		/* Is this needed? */
		fqname = fqname.trim();
		
		/* If exists, get the entity from cache cache? */
		if( cachable )
		{
			if( entities.containsKey( fqname ) )
			{
				logger.log( "Fetched " + fqname + " from cache." );
				return entities.get( fqname );
			}
		}
		
		UCMEntity entity    = null;
		
		String shortname = "";
		ClearcaseEntityType type  = ClearcaseEntityType.Undefined;
		String pvob      = "";
				
		/* Test standard fully qualified names */
		Matcher match = pattern_std_fqname.matcher( fqname );
		if( match.find() )
		{
			ClearcaseEntityType etype = ClearcaseEntityType.GetFromString( match.group( 1 ) );
			
			/* Set the Entity variables */
			shortname = match.group( 2 );
			pvob      = match.group( 3 );
			type      = etype;
		}
		else
		{
			
			/* Not a standard entity, lets try Version */
			match = pattern_version_fqname.matcher( fqname );
			if( match.find() )
			{
				/* Set the Entity variables */
				shortname = match.group( 1 );
				pvob      = match.group( 2 );
				type      = ClearcaseEntityType.Version;
			}
			else
			{
				
				/* Not a Version entity, lets try Tag */
				match = pattern_tag_fqname.matcher( fqname );
				if( match.find() )
				{
					/* Set the Entity variables */
					shortname = match.group( 1 ); // This is also the eid
					pvob      = match.group( 2 );
					type      = ClearcaseEntityType.Tag;
				}
				else
				{
					/* Not a Tag entity, lets try HLink(which really is a Tag) */
					match = pattern_hlink_fqname.matcher( fqname );
					if( match.find() )
					{
						/* Set the Entity variables */
						shortname = match.group( 1 );
						pvob      = match.group( 3 );
						type      = ClearcaseEntityType.HyperLink;
					}
				}
			}
		}
		
		/* If the entity is undefined, throw an exception  */
		if( type == ClearcaseEntityType.Undefined )
		{
			logger.error( "The entity type of " + fqname + " was not recognized" );
			throw new UCMException( "The entity type of " + fqname + " was not recognized", UCMType.ENTITY_ERROR );
		}
		
		/* Load the Entity class */
		Class<UCMEntity> eclass = null;
		try
		{	
			eclass = (Class<UCMEntity>) classloader.loadClass( "net.praqma.clearcase.ucm.entities." + type  );
		}
		catch ( ClassNotFoundException e )
		{
			logger.error( "The class " + type + " is not available." );
			throw new UCMException( "The class " + type + " is not available.", UCMType.ENTITY_ERROR );
		}
		
		/* Try to instantiate the Entity object */
		try
		{
			entity = (UCMEntity)eclass.newInstance();
		}
		catch ( Exception e )
		{
			logger.error( "Could not instantiate the class " + type );
			throw new UCMException( "Could not instantiate the class " + type, UCMType.ENTITY_ERROR );
		}
		
		/* Storing the variables in the object */
		entity.fqname    = fqname;
		entity.shortname = shortname;
		entity.type      = type;
		entity.pvob      = pvob;
		
		//logger.debug( "Created entity of type " + entity.type );
		
		entity.PostProcess();
		
		/* If not trusted, load the entity from the context */
		if( !trusted )
		{
			entity.Load();
		}
		
		if( cachable )
		{
			logger.log( "Storing " + fqname + " in cache." );
			entities.put( fqname, entity );
		}
		
		return entity;
	}
	
	/**
	 * Default PostProcess method. If an entity needs post processing of its creation, this method should be overridden.
	 */
	void PostProcess()
	{
		/* NOP, should be overridden */
	}
	
	
	/**
	 * Default load functionality for the entity.
	 * Every UCM entity should implement this method itself.
	 * @throws UCMException 
	 */
	public void Load() throws UCMException
	{
		logger.warning( "Load method is not implemented for this Entity(" + this.fqname + ")" );
		this.loaded = true;
	}
	

	/* Syntactic static helper methods for retrieving entity objects */
	
	
	public static Activity GetActivity( String name ) throws UCMException
	{
		return GetActivity( name, true );
	}
	
	/**
	 * Retrieve an Activity object.
	 * @param name Fully qualified name
	 * @param trusted If not trusted, the entity's content is loaded from clear case.
	 * @return An Activity object
	 */
	public static Activity GetActivity( String name, boolean trusted ) throws UCMException
	{
		if( !name.startsWith( "activity:" ) )
		{
			name = "activity:" + name;
		}
		Activity entity = (Activity)UCMEntity.GetEntity( name, trusted );
		return entity;
	}


	public static Baseline GetBaseline( String name ) throws UCMException
	{
		return GetBaseline( name, true );
	}
	
	/**
	 * Retrieve an Baseline object.
	 * @param name Fully qualified name
	 * @param trusted If not trusted, the entity's content is loaded from clear case.
	 * @return An Baseline object
	 */
	public static Baseline GetBaseline( String name, boolean trusted ) throws UCMException
	{
		if( !name.startsWith( "baseline:" ) )
		{
			name = "baseline:" + name;
		}
		Baseline entity = (Baseline)UCMEntity.GetEntity( name, trusted );
		return entity;
	}
	
	
	public static Component GetComponent( String name ) throws UCMException
	{
		return GetComponent( name, true );
	}
	
	/**
	 * Retrieve an Component object.
	 * @param name Fully qualified name
	 * @param trusted If not trusted, the entity's content is loaded from clear case.
	 * @return An Component object
	 */
	public static Component GetComponent( String name, boolean trusted ) throws UCMException
	{
		if( !name.startsWith( "component:" ) )
		{
			name = "component:" + name;
		}
		Component entity = (Component)UCMEntity.GetEntity( name, trusted );
		return entity;
	}
	
	
	public static HyperLink GetHyperLink( String name ) throws UCMException
	{
		return GetHyperLink( name, true );
	}
	
	/**
	 * Retrieve an HyperLink object.
	 * @param name Fully qualified name
	 * @param trusted If not trusted, the entity's content is loaded from clear case.
	 * @return An Stream object
	 */
	public static HyperLink GetHyperLink( String name, boolean trusted ) throws UCMException
	{
		if( !name.startsWith( "hlink:" ) )
		{
			name = "hlink:" + name;
		}
		
		HyperLink entity = (HyperLink)UCMEntity.GetEntity( name, trusted );	
		return entity;
	}
	
	
	public static Project GetProject( String name ) throws UCMException
	{
		return GetProject( name, true );
	}
	
	/**
	 * Retrieve a Project object.
	 * @param name Fully qualified name
	 * @param trusted If not trusted, the entity's content is loaded from clear case.
	 * @return a Project object
	 */
	public static Project GetProject( String name, boolean trusted ) throws UCMException
	{
		if( !name.startsWith( "project:" ) )
		{
			name = "project:" + name;
		}
		Project entity = (Project)UCMEntity.GetEntity( name, trusted );
		return entity;
	}
	
	
	public static Stream GetStream( String name ) throws UCMException
	{
		return GetStream( name, true );
	}
	
	/**
	 * Retrieve an Stream object.
	 * @param name Fully qualified name
	 * @param trusted If not trusted, the entity's content is loaded from clear case.
	 * @return An Stream object
	 */
	public static Stream GetStream( String name, boolean trusted ) throws UCMException
	{
		if( !name.startsWith( "stream:" ) )
		{
			name = "stream:" + name;
		}
		Stream entity = (Stream)UCMEntity.GetEntity( name, trusted );
		return entity;
	}
	
	
	/* Tag stuff */

	public Tag GetTag( String tagType, String tagID ) throws UCMException
	{
		if( UCM.isVerbose() )
		{
			System.out.println( "Retrieving tags for " + tagType + ", " + tagID );
		}
		
		return tp.GetTag( tagType, tagID, this );
	}

	/**
	 * Retrieve the attributes for an entity, executed from the current working directory
	 * @return A Map of key, value pairs of the attributes
	 * @throws UCMException
	 */
	public Map<String, String> getAttributes() throws UCMException
	{
		return context.getAttributes( this );
	}
	
	public String getAttribute( String key ) throws UCMException
	{
		Map<String, String> atts = this.getAttributes();
		if( atts.containsKey( key ) )
		{
			return atts.get( key );
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Retrieve the attributes for an entity
	 * @param dir A File object of the directory where the command should be executed
	 * @return A Map of key, value pairs of the attributes
	 * @throws UCMException
	 */
	public Map<String, String> getAttributes( File dir ) throws UCMException
	{
		return context.getAttributes( this, dir );
	}
	
	public void setAttribute( String attribute, String value ) throws UCMException
	{
		context.setAttribute( this, attribute, value );
	}
	
	
	public List<HyperLink> getHlinks( String hlinkType, File dir ) throws UCMException
	{
		logger.debug( "THIS=" + this.GetFQName() );
		return context.getHlinks( this, hlinkType, dir );
	}

	
	/* Getters */
	
	public String getUser() throws UCMException
	{
		if( !loaded ) Load();
		return this.user;
	}
	
	public void SetUser( String user )
	{
		this.user = user;
	}
	
	public String GetFQName()
	{
		return this.fqname;
	}
	
	public String GetShortname()
	{
		return this.shortname;
	}
	
	public String GetPvob()
	{
		return this.pvob;
	}
	
	public String getMastership() throws UCMException
	{
		if( this.mastership == null )
		{
			this.mastership = context.getMastership( this );
		}
		
		return this.mastership;
	}
	
	/**
	 * Returns a string representation of the entity object
	 * @return A String
	 * @throws UCMException 
	 */
	public String Stringify() throws UCMException
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append( "----> " + this.fqname + " <----" + linesep );
		sb.append( "Shortname: " + this.shortname + linesep );
		if( !this.type.equals( ClearcaseEntityType.Version ) )
		{
			sb.append( "PVOB     : " + this.pvob + linesep );
		}
		sb.append( "Type     : " + this.type + linesep );
		
		return sb.toString();
	}
	
	private static final Pattern pattern_name_part = Pattern.compile( "^\\w+:(.*?)@" );
	
	public static String GetNamePart( String fqname ) throws UCMException
	{
		Matcher m = pattern_name_part.matcher( fqname );
		
		if( m.find() )
		{
			String name = m.group( 1 );
			if( name.matches( rx_ccdef_cc_name ) )
			{
				return name;
			}
		}

		throw new UCMException( "Not a valid UCM name.", UCMType.ENTITY_NAME_ERROR );
	}
	
	/**
	 * @returns A shorthand representation of the object.
	 */
	public String toString()
	{
		return this.GetFQName();
	}
	
	
	public String GetXML()
	{
		return context.GetXML();
	}
	

	public static void SaveState()
	{
		context.SaveState();
	}
}
