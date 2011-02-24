package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.utils.BaselineDiff;
import net.praqma.clearcase.ucm.view.SnapshotView;

public class Baseline extends UCMEntity
{
	/* Baseline specific fields */
	private Component component            = null;
	private Project.Plevel plevel          = Project.Plevel.INITIAL;
	private Stream stream                  = null;
	private ArrayList<Activity> activities = null;
	
	/**
	 * Nested public class for easy compilation and access of the differences Activities and Versions.
	 * @author wolfgang
	 *
	 */

	
	
	Baseline()
	{
	}
	
	/**
	 * Load the Baseline into memory from ClearCase.<br>
	 * This function is automatically called when needed by other functions.
	 */
	public void Load() throws UCMException
	{
		String[] rs = context.LoadBaseline( this );
		
		/* Component */
		String c = ( rs[1].matches( "^component:.*$" ) ? "" : "component:" ) + ( rs[1].matches( ".*@\\\\.*$" ) ? rs[1] : rs[1] + "@" + this.pvob );
		logger.debug( "Component = " + c );
		/* Stream */
		if( rs[2].trim().length() > 0 )
		{
			String s = ( rs[2].matches( "^stream:.*$" ) ? "" : "stream:" ) + ( rs[2].matches( ".*@\\\\.*$" ) ? rs[2] : rs[2] + "@" + this.pvob );
			logger.debug( "Stream = " + s );
			this.stream = (Stream)UCMEntity.GetEntity( s );
		}
		else
		{
			logger.warning( "The stream was not set. Propably because the baseline was INITIAL." );
		}

		/* Now with factory creation! */
		this.component = (Component)UCMEntity.GetEntity( c );
		this.plevel    = Project.GetPlevelFromString( rs[3] );
		this.user      = rs[4];
		
		activities = new ArrayList<Activity>();
		
		this.loaded = true;
	}
	
	public static Baseline create( String fqname, Component component, File view, boolean incremental, boolean identical ) throws UCMException
	{
		if( fqname.toLowerCase().startsWith( "baseline:" ) )
		{
			logger.warning( "The baseline name should not be prefixed with \"baseline:\", removing it" );
			fqname = fqname.replaceFirst( "baseline:", "" );
		}
		
		context.createBaseline( fqname, component, view, incremental, identical );
		
		return UCMEntity.GetBaseline( fqname + "@" + component.GetPvob(), true );
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
	
	/**
	 * Return the promotion level of a baseline. <br>
	 * If <code>cached</code> is not set, the promotion level is loaded from ClearCase.
	 * @param cached Whether to use the cached promotion level or not
	 * @return The promotion level of the Baseline
	 */
	public Project.Plevel GetPromotionLevel( boolean cached ) throws UCMException
	{
		if( !loaded ) this.Load();
		
		if( cached )
		{
			return this.plevel;
		}
		else
		{
			/* TODO Get from clear case, uses cached value */
			/* If different from cached, cache the new */
			return this.plevel;
		}
	}
	
	/**
	 * Promote the Baseline.
	 * <ul>
	 * <li><code>INITIAL -> BUILT</code></li>
	 * <li><code>BUILD&nbsp;&nbsp; -> TESTED</code></li>
	 * <li><code>TESTED&nbsp; -> RELEASED</code></li>
	 * </ul>
	 * 
	 * If the promotion level is not set, it is set to <code>INITAL</code>.
	 * @return The new promotion level.
	 */
	public Project.Plevel Promote() throws UCMException
	{
		if( !loaded ) this.Load();
		
		this.plevel = Project.PromoteFrom( this.plevel );
		
		context.SetPromotionLevel( this );
		
		return this.plevel;
	}
	
	/**
	 * Demotes the Baseline to <code>REJECTED</code>.
	 */
	public void Demote() throws UCMException
	{
		if( !loaded ) this.Load();
		
		this.plevel = Project.Plevel.REJECTED;
		
		context.SetPromotionLevel( this );
	}
	

	/**
	 * Get the differences between two Baselines.<br>
	 * Currently this method only support the previous Baseline and with -nmerge set.<br>
	 * @return A BaselineDiff object containing a set of Activities.
	 */
	public BaselineDiff GetDiffs( SnapshotView view ) throws UCMException
	{
		return new BaselineDiff( view, this );
	}
	
	public Component GetComponent() throws UCMException
	{
		if( !loaded ) Load();
		return this.component;
	}
	
	public Stream GetStream() throws UCMException
	{
		if( !loaded ) Load();
		return this.stream;
	}
	
	public String Stringify() throws UCMException
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
