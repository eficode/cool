package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;

import net.praqma.clearcase.ucm.UCMException;


public class Project extends UCMEntity
{
	/* Project specific fields */
	private Stream stream = null;
	
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
	
	/* For now, the project implements the Plevel functionality */
	public enum Plevel
	{
		REJECTED,
		INITIAL,
		BUILT,
		TESTED,
		RELEASED;
	}
	
	/**
	 * Given a String, return the corresponding Promotion Level.
	 * @param str, if not a valid Promotion Level INITAL is returned.
	 * @return A Promotion Level
	 */
	public static Plevel GetPlevelFromString( String str )
	{
		Plevel plevel = Plevel.INITIAL;
		
		try
		{
			plevel = Plevel.valueOf( str );
		}
		catch( Exception e )
		{
			/* Do nothing... */
		}
		
		return plevel;
	}
	
	public static Plevel PromoteFrom( Plevel plevel )
	{
		switch( plevel )
		{
		case INITIAL:
			plevel = Plevel.BUILT;
			break;
		case BUILT:
			plevel = Plevel.TESTED;
			break;
		case TESTED:
			plevel = Plevel.RELEASED;
			break;
		case RELEASED:
			plevel = Plevel.RELEASED;
			break;
		}
		
		return plevel;
	}
	
	public void Load() throws UCMException
	{
		context.LoadProject( this );
	}
	
	public void SetStream( Stream stream )
	{
		this.stream = stream;
	}
	
	public Stream getIntegrationStream() throws UCMException
	{
		if( !this.loaded ) Load();
		return stream;
	}
}
