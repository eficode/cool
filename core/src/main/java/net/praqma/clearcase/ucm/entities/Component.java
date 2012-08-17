package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.*;
import net.praqma.logging.Config;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;

public class Component extends UCMEntity {

	private transient static Logger logger = Logger.getLogger();
	private static final java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);
	private static final String rx_component_load = "\\s*Error: component not found\\s*";

	/* Component specific fields */

	Component() {
		super( "component" );
		tracer.entering(Component.class.getSimpleName(), "Component");
		tracer.exiting(Component.class.getSimpleName(), "Component");
	}

	/**
	 * This method is only available to the package, because only
	 * ClearcaseEntity should be allowed to call it.
	 * 
	 * @return A new Component Entity
	 */
	static Component getEntity() {
		tracer.entering(Component.class.getSimpleName(), "getEntity");

		Component output = new Component();

		tracer.exiting(Component.class.getSimpleName(), "getEntity", output);

		return output;
	}

	public Component load() throws UCMEntityNotFoundException, UnableToLoadEntityException {
		tracer.entering(Component.class.getSimpleName(), "load");

		String cmd = "describe -fmt %[name]p " + this;
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			if( e.getMessage().matches( rx_component_load ) ) {
				UCMEntityNotFoundException exception = new UCMEntityNotFoundException( this, e );

				tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));

				throw exception;
			} else {
				UnableToLoadEntityException exception = new UnableToLoadEntityException( this, e );

				tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));

				throw exception;
			}
		}

		this.loaded = true;

		tracer.exiting(Component.class.getSimpleName(), "load", this);

		return this;
	}

	public static Component create( String name, PVob pvob, String root, String comment, File view ) throws UnableToCreateEntityException, UnableToInitializeEntityException {
		tracer.entering(Component.class.getSimpleName(), "create", new Object[]{name, pvob, root, comment, view});
		
		//context.createComponent( name, pvob, root, comment, view );

		String cmd = "mkcomp" + ( comment != null ? " -c \"" + comment + "\"" : "" ) + ( root != null ? " -root " + root : " -nroot" ) + " " + name + "@" + pvob;

		try {
			Cleartool.run( cmd, view );
		} catch( Exception e ) {
			//throw new UCMException( e.getMessage(), UCMType.CREATION_FAILED );
			UnableToCreateEntityException exception = new UnableToCreateEntityException( Component.class, e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			throw exception;
		}

		Component output = get( name, pvob );
		
		tracer.exiting(Component.class.getSimpleName(), "create", output);
		
		return output;
	}

	public String getRootDir() throws CleartoolException {
		tracer.entering(Component.class.getSimpleName(), "getRootDir");
		
		String cmd = "desc -fmt %[root_dir]p " + this;
		try {
			String output = Cleartool.run( cmd ).stdoutBuffer.toString();
			
			tracer.exiting(Component.class.getSimpleName(), "getRootDir", output);
			
			return output;
		} catch( AbnormalProcessTerminationException e ) {
			CleartoolException exception = new CleartoolException( "Unable to get rootdir: " + e.getMessage(), e );
			
			tracer.severe(String.format("Exception thrown type: %s; message: %s", e.getClass(), e.getMessage()));
			
			throw exception;
		}
	}


	public static Component get( String name, PVob pvob ) throws UnableToInitializeEntityException {
		tracer.entering(Component.class.getSimpleName(), "get", new Object[]{name, pvob});
		
		if( !name.startsWith( "component:" ) ) {
			name = "component:" + name;
		}
		Component entity = (Component) UCMEntity.getEntity( Component.class, name + "@" + pvob );
		
		tracer.exiting(Component.class.getSimpleName(), "get", entity);
		
		return entity;
	}

	public static Component get( String name ) throws UnableToInitializeEntityException {
		tracer.entering(Component.class.getSimpleName(), "get", new Object[]{name});
		
		if( !name.startsWith( "component:" ) ) {
			name = "component:" + name;
		}
		Component entity = (Component) UCMEntity.getEntity( Component.class, name );
		
		tracer.exiting(Component.class.getSimpleName(), "get", entity);
		
		return entity;
	}

	/**
	 * Unique hash code for object based on fully qualified nave
	 * Neccessary if adding objects to hash sets, -maps etc.
	 * @return unique hash value for object.
	 */
	@Override
	public int hashCode()
	{
		// check for load not needed fqname always given
		// FullyQualifiedName is unique in ClearCase (at least in this installation)
		return (this.getFullyQualifiedName().hashCode());
	}

}
