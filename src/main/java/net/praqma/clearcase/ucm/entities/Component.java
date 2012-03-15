package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.util.List;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.util.debug.Logger;

public class Component extends UCMEntity {
	
	private static final long serialVersionUID = -6186110079026697257L;
	private transient static Logger logger = Logger.getLogger();
	
	/* Component specific fields */

	Component() {
		super( "component" );
	}

	/**
	 * This method is only available to the package, because only
	 * ClearcaseEntity should be allowed to call it.
	 * 
	 * @return A new Component Entity
	 */
	static Component getEntity() {
		return new Component();
	}

	public void load() throws UCMException {
		context.loadComponent( this );
	}

	public static Component create( String name, PVob pvob, String root, String comment, File view ) throws UCMException {
		context.createComponent( name, pvob, root, comment, view );

		return UCMEntity.getComponent( name, pvob, true );
	}

	public String getRootDir() throws UCMException {
		return context.getRootDir( this );
	}

	public List<Baseline> getBaselines( Stream stream ) throws UCMException {
		logger.debug( "Getting Baselines from " + stream.getFullyQualifiedName() + " and " + getFullyQualifiedName() );

		return this.getBaselines(stream, null, false);
	}

	public List<Baseline> getBaselines( Stream stream, Project.Plevel plevel ) throws UCMException {
		logger.debug( "Getting Baselines from " + stream.getFullyQualifiedName() + " and " + getFullyQualifiedName() );

		return this.getBaselines(stream, plevel, false);
	}

	public List<Baseline> getBaselines( Stream stream, boolean multisitePolling ) throws UCMException {
		logger.debug( "Getting Baselines from " + stream.getFullyQualifiedName() + " and " + getFullyQualifiedName() );

		return this.getBaselines(stream, null, multisitePolling);
		//return new BaselineList( this, stream, null );
	}

	public List<Baseline> getBaselines( Stream stream, Project.Plevel plevel, boolean multisitePolling ) throws UCMException {
		logger.debug( "Getting Baselines from " + stream.getFullyQualifiedName() + " and " + this.getFullyQualifiedName() + " with plevel " + plevel );

		return stream.getBaselines(this, plevel, multisitePolling);
		//return new BaselineList( this, stream, plevel );
	}

}
