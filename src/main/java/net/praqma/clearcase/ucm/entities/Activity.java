package net.praqma.clearcase.ucm.entities;

public class Activity extends UCMEntity
{
	/* Activity specific fields */
	public Changeset changeset = new Changeset();
	private boolean specialCase = false;
	
	
	Activity()
	{
	}
	
	public void SetSpecialCase( boolean b )
	{
		this.specialCase = b;
	}
	
	public boolean GetSpecialCase()
	{
		return this.specialCase;
	}
	
	/**
	 * This method is only available to the package, because only ClearcaseEntity should
	 * be allowed to call it.
	 * @return A new Activity Entity
	 */
	static Activity GetEntity()
	{
		return new Activity();
	}
	
	/**
	 * Load the Activity into memory from ClearCase.<br>
	 * This function is automatically called when needed by other functions.
	 */
	public void Load()
	{
		context.LoadActivity( this );
	}
	
}
