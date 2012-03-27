package net.praqma.clearcase.ucm.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.*;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class Project extends UCMEntity {

	private static Logger logger = Logger.getLogger();

	/* Project specific fields */
	private Stream stream = null;

	/**
	 * Enables inter-project deliveries
	 */
	public static final int POLICY_INTERPROJECT_DELIVER = 1;
	public static final int POLICY_CHSTREAM_UNRESTRICTED = 2;
	public static final int POLICY_DELIVER_REQUIRE_REBASE = 4;
	public static final int POLICY_DELIVER_NCO_DEVSTR = 8;

	Project() {
		super( "project" );
	}

	/**
	 * This method is only available to the package, because only UCMEntity
	 * should be allowed to call it.
	 * 
	 * @return A new Project Entity
	 */
	static Project getEntity() {
		return new Project();
	}

	/* For now, the project implements the Plevel functionality */
	public enum PromotionLevel implements Serializable {
		INITIAL, BUILT, TESTED, RELEASED, REJECTED;
	}

	/**
	 * Given a String, return the corresponding Promotion Level.
	 * 
	 * @param str
	 *            , if not a valid Promotion Level INITAL is returned.
	 * @return A Promotion Level
	 */
	public static PromotionLevel getPlevelFromString( String str ) {
		PromotionLevel plevel = PromotionLevel.INITIAL;

		try {
			plevel = PromotionLevel.valueOf( str );
		} catch( Exception e ) {
			/* Do nothing... */
		}

		return plevel;
	}

	public static PromotionLevel promoteFrom( PromotionLevel plevel ) {
		switch ( plevel ) {
		case INITIAL:
			plevel = PromotionLevel.BUILT;
			break;
		case BUILT:
			plevel = PromotionLevel.TESTED;
			break;
		case TESTED:
			plevel = PromotionLevel.RELEASED;
			break;
		case RELEASED:
			plevel = PromotionLevel.RELEASED;
			break;
		}

		return plevel;
	}

	public static String getPolicy( int policy ) {
		String p = "";
		if( ( policy & POLICY_INTERPROJECT_DELIVER ) > 0 ) {
			p += "POLICY_INTERPROJECT_DELIVER,";
		}

		if( ( policy & POLICY_CHSTREAM_UNRESTRICTED ) > 0 ) {
			p += "POLICY_CHSTREAM_UNRESTRICTED,";
		}

		if( ( policy & POLICY_DELIVER_REQUIRE_REBASE ) > 0 ) {
			p += "POLICY_DELIVER_REQUIRE_REBASE,";
		}

		if( ( policy & POLICY_DELIVER_NCO_DEVSTR ) > 0 ) {
			p += "POLICY_DELIVER_NCO_DEVSTR,";
		}

		if( p.length() > 0 ) {
			p = p.substring( 0, ( p.length() - 1 ) );
		}

		return p;
	}

	/**
	 * Create a project.
	 * @param name The name of the Project
	 * @param root If not null, the root folder of the project
	 * @param pvob The {@link PVob}}
	 * @param policy Policies as integer
	 * @param comment If not null, the comment of the project
	 * @param mcomps Modifiable components of the project
	 * @return
	 * @throws UnableToCreateEntityException
	 * @throws UnableToLoadEntityException
	 * @throws UCMEntityNotFoundException
	 * @throws UnableToGetEntityException 
	 */
	
	public static Project create( String name, String root, PVob pvob, int policy, String comment, Component... mcomps ) throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		return create( name, root, pvob, policy, comment, mcomps );
	}
	
	public static Project create( String name, String root, PVob pvob, int policy, String comment, boolean normal, Component... mcomps ) throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		return create( name, root, pvob, policy, comment, normal, Arrays.asList( mcomps ) );
	}
	
	public static Project create( String name, String root, PVob pvob, int policy, String comment, boolean normal, List<Component> mcomps ) throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		//context.createProject( name, root, pvob, policy, comment, mcomps );

		String cmd = "mkproject" + ( comment != null ? " -c \"" + comment + "\"" : "" ) + " -in " + ( root == null ? "RootFolder" : root ) + " -modcomp" + ( normal ? " " : " -model SIMPLE " );
		
		if( mcomps != null ) {
			for( Component c : mcomps ) {
				cmd += c.getNormalizedName() + ",";
			}
			cmd = cmd.substring( 0, ( cmd.length() - 1 ) );
		}
		
		if( policy > 0 ) {
			cmd += " -policy " + Project.getPolicy( policy );
		}
		cmd += " " + name + "@" + pvob;

		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Could not create Project " + root + ": " + e.getMessage(), e, UCMType.CREATION_FAILED );
			throw new UnableToCreateEntityException( Project.class, e );
		}

		return get( name, pvob );
	}

	public Project load() throws UnableToLoadEntityException, UnableToCreateEntityException {
		//context.loadProject( this );
		//String result = strategy.loadProject( project.getFullyQualifiedName() );
		String result = "";

		String cmd = "lsproj -fmt %[istream]Xp " + this;

		try {
			result = Cleartool.run( cmd ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( e );
			throw new UnableToLoadEntityException( this, e );
		}

		logger.debug( "Result: " + result );

		setStream( Stream.get( result ) );

		return this;
	}

	public void setStream( Stream stream ) {
		this.stream = stream;
	}

	public Stream getIntegrationStream() {
		return stream;
	}
	
	public List<Stream> getStreams() throws CleartoolException {
		String cmd = "lsstream -in " + this;
		
		List<String> list;
		try {
			list = Cleartool.run( cmd ).stdoutList;
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to list streams for " + this, e );
		}
		
		List<Stream> streams = new ArrayList<Stream>();
		for( String item : list ) {
			try {
				streams.add( Stream.get( item ) );
			} catch( ClearCaseException e ) {
				logger.error( "Could not get " + item );
			}
		}
		
		return streams;
	}

	public static List<String> getPromotionLevels() {
		List<String> retval = new ArrayList<String>();
		for( Object o : PromotionLevel.values() ) {
			retval.add( o.toString() );
		}
		return retval;
	}

	public static List<Project> getProjects( PVob pvob ) throws UnableToListProjectsException, UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		//return context.getProjects( vob );
		logger.debug( "Getting projects for " + pvob );
		String cmd = "lsproject -s -invob " + pvob.toString();

		List<String> projs = null;

		try {
			projs = Cleartool.run( cmd ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( e.getMessage(), e.getMessage() );
			throw new UnableToListProjectsException( pvob, e );
		}

		logger.debug( projs );

		List<Project> projects = new ArrayList<Project>();
		for( String p : projs ) {
			projects.add( Project.get( p + "@" + pvob ) );
		}

		logger.debug( projects );

		return projects;
	}
	
	
	public List<Component> getModifiableComponents() throws UnableToCreateEntityException, UCMEntityNotFoundException, CleartoolException, UnableToGetEntityException {
		//List<String> cs = strategy.getModifiableComponents( project.getFullyQualifiedName() );
		String[] cs;
		String cmd = "desc -fmt %[mod_comps]p " + this;
		try {
			cs = Cleartool.run( cmd ).stdoutBuffer.toString().split( "\\s+" );
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to modifiable components", e );
		}
		
		List<Component> comps = new ArrayList<Component>();

		for( String c : cs ) {
			comps.add( Component.get( c, pvob ) );
		}

		return comps;
	}
	
	public void remove() throws UnableToRemoveEntityException {
		String cmd = "rmproject -force " + this;
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new UnableToRemoveEntityException( this, e );
		}
	}

	
	public static Project get( String name, PVob pvob ) throws UnableToCreateEntityException {
		if( !name.startsWith( "project:" ) ) {
			name = "project:" + name;
		}
		Project entity = (Project) UCMEntity.getEntity( Project.class, name + "@" + pvob );
		return entity;
	}

	public static Project get( String name ) throws UnableToCreateEntityException {
		if( !name.startsWith( "project:" ) ) {
			name = "project:" + name;
		}
		Project entity = (Project) UCMEntity.getEntity( Project.class, name );
		return entity;
	}

}
