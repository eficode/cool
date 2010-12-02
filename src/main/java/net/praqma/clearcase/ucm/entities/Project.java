package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;

public class Project extends UCMEntity
{
	/* Project specific fields */
	
	
	Project()
	{
	}
	
	/**
	 * This method is only available to the package, because only UCMEntity should
	 * be allowed to call it.
	 * @return A new Project Entity
	 */
	static Project GetEntity()
	{
		return new Project();
	}
}
