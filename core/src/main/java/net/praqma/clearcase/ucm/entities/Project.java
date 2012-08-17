package net.praqma.clearcase.ucm.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.interfaces.StreamContainable;
import net.praqma.clearcase.util.setup.ProjectTask;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class Project extends UCMEntity implements StreamContainable {

	private static Logger logger = Logger.getLogger();
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

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
		tracer.entering(Project.class.getSimpleName(), "Project");
		tracer.exiting(Project.class.getSimpleName(), "Project");
	}

	/**
	 * This method is only available to the package, because only UCMEntity
	 * should be allowed to call it.
	 * 
	 * @return A new Project Entity
	 */
	static Project getEntity() {
		tracer.entering(Project.class.getSimpleName(), "getEntity");

		Project output = new Project();

		tracer.exiting(Project.class.getSimpleName(), "getEntity", output);

		return output;
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
		tracer.entering(Project.class.getSimpleName(), "getPlevelFromString", str);

		PromotionLevel plevel = PromotionLevel.INITIAL;

		try {
			plevel = PromotionLevel.valueOf( str );
		} catch( Exception e ) {
			/* Do nothing... */
		}

		tracer.exiting(Project.class.getSimpleName(), "getEntity", plevel);

		return plevel;
	}

	public static PromotionLevel promoteFrom( PromotionLevel plevel ) {
		tracer.entering(Project.class.getSimpleName(), "promoteFrom", plevel);

		switch ( plevel ) {
		case INITIAL:
			plevel = PromotionLevel.BUILT;

			tracer.finest("Promoted to: " + plevel);

			break;
		case BUILT:
			plevel = PromotionLevel.TESTED;

			tracer.finest("Promoted to: " + plevel);

			break;
		case TESTED:
			plevel = PromotionLevel.RELEASED;

			tracer.finest("Promoted to: " + plevel);

			break;
		case RELEASED:
			plevel = PromotionLevel.RELEASED;

			tracer.finest("Promoted to: " + plevel);

			break;
		}

		tracer.exiting(Project.class.getSimpleName(), "promoteFrom", plevel);

		return plevel;
	}

	public static String getPolicy( int policy ) {
		tracer.entering(Project.class.getSimpleName(), "getPolicy", policy);

		tracer.finest("Aquering policies...");
		String p = "";
		if( ( policy & POLICY_INTERPROJECT_DELIVER ) > 0 ) {
			p += "POLICY_INTERPROJECT_DELIVER,";

			tracer.finest("Policy added: POLICY_INTERPROJECT_DELIVER");
		}

		if( ( policy & POLICY_CHSTREAM_UNRESTRICTED ) > 0 ) {
			p += "POLICY_CHSTREAM_UNRESTRICTED,";
			tracer.finest("Policy added: POLICY_CHSTREAM_UNRESTRICTED");
		}

		if( ( policy & POLICY_DELIVER_REQUIRE_REBASE ) > 0 ) {
			p += "POLICY_DELIVER_REQUIRE_REBASE,";
			tracer.finest("Policy added: POLICY_DELIVER_REQUIRE_REBASE");
		}

		if( ( policy & POLICY_DELIVER_NCO_DEVSTR ) > 0 ) {
			p += "POLICY_DELIVER_NCO_DEVSTR,";
			tracer.finest("Policy added: POLICY_DELIVER_NCO_DEVSTR");
		}

		if( p.length() > 0 ) {
			p = p.substring( 0, ( p.length() - 1 ) );
		}

		tracer.exiting(Project.class.getSimpleName(), "getPolicy", p);

		return p;
	}

	public static int getPolicyValue( String policy ) {
		tracer.entering(Project.class.getSimpleName(), "getPolicyValue", policy);

		int policyValue = 0;

		if( policy.equalsIgnoreCase( "POLICY_INTERPROJECT_DELIVER" ) ) {
			policyValue = POLICY_INTERPROJECT_DELIVER;
		} else if( policy.equalsIgnoreCase( "POLICY_CHSTREAM_UNRESTRICTED" ) ) {
			policyValue = POLICY_CHSTREAM_UNRESTRICTED;
		} else if( policy.equalsIgnoreCase( "POLICY_DELIVER_REQUIRE_REBASE" ) ) {
			policyValue = POLICY_DELIVER_REQUIRE_REBASE;
		} else if( policy.equalsIgnoreCase( "POLICY_DELIVER_NCO_DEVSTR" ) ) {
			policyValue = POLICY_DELIVER_NCO_DEVSTR;
		}

		tracer.exiting(Project.class.getSimpleName(), "getPolicyValue", policyValue);

		return policyValue;
	}

	public static Project create( String name, String root, PVob pvob, int policy, String comment, boolean normal, Component mcomps ) throws UnableToCreateEntityException, UnableToInitializeEntityException {
		tracer.entering(Project.class.getSimpleName(), "create", new Object[]{name, root, pvob, policy, comment, normal, mcomps});

		List<Component> components = new ArrayList<Component>();
		components.add( mcomps );

		Project output = create( name, root, pvob, policy, comment, normal, components );

		tracer.exiting(Project.class.getSimpleName(), "create", output);

		return output;
	}

	/**
	 * Create a project.
	 * @param name The name of the Project
	 * @param root If not null, the root folder of the project
	 * @param pvob The {@link PVob}}
	 * @param policy Policies as integer
	 * @param comment If not null, the comment of the project
	 * @param mcomps Modifiable components of the project
	 * @return a new {@link Project}
	 */
	public static Project create( String name, String root, PVob pvob, int policy, String comment, boolean normal, List<Component> mcomps ) throws UnableToCreateEntityException, UnableToInitializeEntityException {
		//context.createProject( name, root, pvob, policy, comment, mcomps );
		tracer.entering(Project.class.getSimpleName(), "create", new Object[]{name, root, pvob, policy, comment, normal, mcomps});

		String cmd = "mkproject" + ( comment != null ? " -c \"" + comment + "\"" : "" ) + " -in " + ( root == null ? "RootFolder" : root ) + ( normal ? "" : " -model SIMPLE" );

		if( mcomps != null && mcomps.size() > 0 ) {
			cmd += " -modcomp ";
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
			UnableToCreateEntityException exception = new UnableToCreateEntityException( Project.class, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}

		Project output = get( name, pvob );
		
		tracer.exiting(Project.class.getSimpleName(), "create", output);
		
		return output; 
	}

	public Project load() throws UnableToLoadEntityException, UnableToInitializeEntityException {
		tracer.entering(Project.class.getSimpleName(), "load");

		String result = "";

		String cmd = "lsproj -fmt %[istream]Xp " + this;

		try {
			result = Cleartool.run( cmd ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			UnableToLoadEntityException exception = new UnableToLoadEntityException( this, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));

			throw exception;
		}

		logger.debug( "Result: " + result );

		setStream( Stream.get( result ) );

		this.loaded = true;

		tracer.exiting(Project.class.getSimpleName(), "load", this);

		return this;
	}

	public void setStream( Stream stream ) {
		tracer.entering(Project.class.getSimpleName(), "setStream", stream);
		
		this.stream = stream;
		
		tracer.exiting(Project.class.getSimpleName(), "setStream");
	}

	public Stream getIntegrationStream() {
		tracer.entering(Project.class.getSimpleName(), "getIntegrationStream");

		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				EntityNotLoadedException exception = new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
				
				tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));

				throw exception;
			}
		}

		tracer.exiting(Project.class.getSimpleName(), "getIntegrationStream");

		return stream;
	}

	public List<Stream> getStreams() throws CleartoolException {
		tracer.entering(Project.class.getSimpleName(), "getStreams");
		
		String cmd = "lsstream -in " + this;

		List<String> list;
		try {
			list = Cleartool.run( cmd ).stdoutList;
		} catch( Exception e ) {
			CleartoolException exception = new CleartoolException( "Unable to list streams for " + this, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}

		List<Stream> streams = new ArrayList<Stream>();
		for( String item : list ) {
			try {
				streams.add( Stream.get( item ) );
			} catch( ClearCaseException e ) {
				logger.error( "Could not get " + item );
			}
		}

		tracer.exiting(Project.class.getSimpleName(), "getStreams", streams);
		
		return streams;
	}

	public static List<String> getPromotionLevels() {
		tracer.entering(Project.class.getSimpleName(), "getPromotionLevels");
		
		List<String> retval = new ArrayList<String>();
		for( Object o : PromotionLevel.values() ) {
			retval.add( o.toString() );
		}
		
		tracer.exiting(Project.class.getSimpleName(), "getPromotionLevels", retval);
		
		return retval;
	}

	public static List<Project> getProjects( PVob pvob ) throws UnableToListProjectsException, UnableToInitializeEntityException {
		tracer.entering(Project.class.getSimpleName(), "getProjects", pvob);
		
		logger.debug( "Getting projects for " + pvob );
		String cmd = "lsproject -s -invob " + pvob.toString();

		List<String> projs = null;

		try {
			projs = Cleartool.run( cmd ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			UnableToListProjectsException exception = new UnableToListProjectsException( pvob, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}

		logger.debug( projs );

		List<Project> projects = new ArrayList<Project>();
		for( String p : projs ) {
			projects.add( Project.get( p + "@" + pvob ) );
		}

		logger.debug( projects );

		tracer.exiting(Project.class.getSimpleName(), "getProjects", projects);
		
		return projects;
	}


	public List<Component> getModifiableComponents() throws UnableToInitializeEntityException, CleartoolException {
		tracer.entering(Project.class.getSimpleName(), "getModifiableComponents");

		String[] cs;
		String cmd = "desc -fmt %[mod_comps]p " + this;
		try {
			cs = Cleartool.run( cmd ).stdoutBuffer.toString().split( "\\s+" );
		} catch( AbnormalProcessTerminationException e ) {
			CleartoolException exception = new CleartoolException( "Unable to modifiable components", e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}

		List<Component> comps = new ArrayList<Component>();

		for( String c : cs ) {
			comps.add( Component.get( c, pvob ) );
		}

		tracer.exiting(Project.class.getSimpleName(), "getModifiableComponents", comps);

		return comps;
	}

	public void remove() throws UnableToRemoveEntityException {
		tracer.entering(Project.class.getSimpleName(), "remove");
		
		String cmd = "rmproject -force " + this;

		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			UnableToRemoveEntityException exception = new UnableToRemoveEntityException( this, e );

			tracer.severe(String.format("Exception thrown type: %s; message: %s", exception.getClass(), exception.getMessage()));
			
			throw exception;
		}
		
		tracer.exiting(Project.class.getSimpleName(), "remove");
	}


	public static Project get( String name, PVob pvob ) throws UnableToInitializeEntityException {
		tracer.entering(Project.class.getSimpleName(), "get");
		
		if( !name.startsWith( "project:" ) ) {
			name = "project:" + name;
		}
		Project entity = (Project) UCMEntity.getEntity( Project.class, name + "@" + pvob );
		
		tracer.exiting(Project.class.getSimpleName(), "get", entity);
		
		return entity;
	}

	public static Project get( String name ) throws UnableToInitializeEntityException {
		tracer.entering(Project.class.getSimpleName(), "get", name);
		
		if( !name.startsWith( "project:" ) ) {
			name = "project:" + name;
		}
		Project entity = (Project) UCMEntity.getEntity( Project.class, name );
		
		tracer.exiting(Project.class.getSimpleName(), "get", entity);
		
		return entity;
	}

}
