package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class Baseline extends UCMEntity
{
	/* Baseline specific fields */
	private Component component            = null;
	private Plevel plevel                  = Plevel.INITIAL;
	private Stream stream                  = null;
	private ArrayList<Activity> activities = null;
	
	/* BaselineDiffs class */
	public class BaselineDiff extends ArrayList<Activity>
	{
		
		public BaselineDiff()
		{
			this.addAll( context.GetBaselineDiff( Baseline.this ) );
		}
		
		public void Print()
		{
			for( Activity a : this )
			{
				System.out.println( "----- Activity:" );
				System.out.println( a.Stringify() );
				for( Version v : a.changeset.versions )
				{
					//System.out.println( v.Stringify() );
					System.out.println( v.toString() );
				}
			}
		}
		
		public ArrayList<Version> GetUniqueFiles()
		{
			return null;
		}
	}
	
	
	Baseline()
	{
	}
	
	public void Load()
	{
		String[] rs = context.LoadBaseline( this );
		
		/* Component */
		String c = rs[1].matches( ".*@\\\\.*$" ) ? rs[1] : rs[1] + "@" + this.pvob;
		logger.debug( "Component = " + c );
		/* Stream */
		String s = rs[2].matches( ".*@\\\\.*$" ) ? rs[2] : rs[2] + "@" + this.pvob;
		logger.debug( "Stream = " + s );
		
		
		/* Now with factory creation! */
		this.component = (Component)UCMEntity.GetEntity( c );
		this.stream    = (Stream)UCMEntity.GetEntity( s );
		this.plevel    = GetPlevelFromString( rs[3] );
		this.user      = rs[4];
		
		activities = new ArrayList<Activity>();
		
		this.loaded = true;
	}
	
	/**
	 * This method is only available to the package, because only ClearcaseEntity should
	 * be allowed to call it.
	 * @return A new Baseline Entity
	 */
	static Baseline GetEntity()
	{
		return new Baseline();
	}
	
	public Plevel GetPromotionLevel( boolean cached )
	{
		if( cached )
		{
			return this.plevel;
		}
		else
		{
			/* TODO Get from clear case, uses cached value */
			return this.plevel;
		}
	}
	
	public Plevel Promote()
	{
		if( !loaded ) this.Load();
		
		switch( this.plevel )
		{
		case INITIAL:
			this.plevel = Plevel.BUILT;
			break;
		case BUILT:
			this.plevel = Plevel.TESTED;
			break;
		case TESTED:
			this.plevel = Plevel.RELEASED;
			break;
		case RELEASED:
			this.plevel = Plevel.RELEASED;
			break;
		default:
			this.plevel = Plevel.BUILT;
		}
		
		//CTF.SetPromotionLevel( this.fqname, this.plevel.GetName() );
		context.SetPromotionLevel( this );
		
		return this.plevel;
	}
	
	public void Demote()
	{
		if( !loaded ) this.Load();
		
		this.plevel = Plevel.REJECTED;
	}
	

	public BaselineDiff GetDiffs()
	{
		return new BaselineDiff();
		//diff.Print();
	}
	
	public Component GetComponent()
	{
		if( !loaded ) Load();
		return this.component;
	}
	
	public String Stringify()
	{
		if( !this.loaded ) Load();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append( super.Stringify() );
		
		sb.append( "PLEVEL   : " + this.plevel + linesep );
		sb.append( "Component: " + this.component.toString() + linesep );
		sb.append( "Stream   : " + this.stream.toString() + linesep );

		return sb.toString();
	}
}
