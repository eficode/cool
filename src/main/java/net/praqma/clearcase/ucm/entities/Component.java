package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.clearcase.ucm.utils.TagQuery;

public class Component extends UCMEntity
{
	/* Component specific fields */
	
	
	Component()
	{
	}
	
	/**
	 * This method is only available to the package, because only ClearcaseEntity should
	 * be allowed to call it.
	 * @return A new Component Entity
	 */
	static Component GetEntity()
	{
		return new Component();
	}
	
	
	public String GetRootDir()
	{
		return context.GetRootDir( this );
	}
	
	public BaselineList GetBaselines( Stream stream ) throws UCMException
	{
		return new BaselineList( this, stream, null );
	}
	
	public BaselineList GetBaselines( Stream stream, Project.Plevel plevel ) throws UCMException
	{
		return new BaselineList( this, stream, plevel );
	}

}
