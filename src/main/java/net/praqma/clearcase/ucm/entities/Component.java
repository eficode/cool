package net.praqma.clearcase.ucm.entities;

import java.io.File;

import net.praqma.clearcase.PVob;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.utils.BaselineList;

public class Component extends UCMEntity
{
	/* Component specific fields */
	
	
	Component() {
	}
	
	/**
	 * This method is only available to the package, because only ClearcaseEntity should
	 * be allowed to call it.
	 * @return A new Component Entity
	 */
	static Component getEntity() {
		return new Component();
	}
	
	public void load() throws UCMException {
		context.loadComponent(this);
	}
	
	public static Component create( String name, PVob pvob, String root, String comment, File view ) throws UCMException {
		context.createComponent(name, pvob, root, comment, view);
		
		return UCMEntity.getComponent(name, pvob, true);
	}
	
	
	public String getRootDir() {
		return context.getRootDir(this);
	}

	public BaselineList getBaselines(Stream stream) throws UCMException {
		return new BaselineList(this, stream, null);
	}

	public BaselineList getBaselines(Stream stream, Project.Plevel plevel)
			throws UCMException {
		return new BaselineList(this, stream, plevel);
	}

}
