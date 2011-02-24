package net.praqma.clearcase.ucm.utils;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Tag;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.util.debug.Logger;

public class BaselineList extends ArrayList<Baseline>
{
	protected static Logger logger = Logger.getLogger( false );
	
	private Stream stream       = null;
	private Component component = null;
	
	/* Default constructor */
	public BaselineList()
	{
		
	}
	
	private BaselineList( BaselineList bls )
	{
		this.stream    = bls.stream;
		this.component = bls.component;
		this.addAll( bls );
	}
	
	private BaselineList( Component component, Stream stream )
	{
		this.stream    = stream;
		this.component = component;
	}
	
	public BaselineList( Component component, Stream stream, Project.Plevel plevel ) throws UCMException
	{
		logger.debug( "Getting Baselines from " + stream.GetFQName() + " and " + component.GetFQName() + " with plevel " + plevel );
		
		this.stream    = stream;
		this.component = component;
		
		this.addAll( UCM.context.GetBaselines( stream, component, plevel, component.GetPvob() ) );
	}
	
	public BaselineList( List<Baseline> bls )
	{
		this.addAll( bls );
	}
	
	public BaselineList Filter( TagQuery tq, String tagType, String tagID ) throws UCMException
	{
		BaselineList bls = new BaselineList( this );
		
		for( Baseline b : this )
		{
			Tag tag = b.GetTag( tagType, tagID );
			
			logger.debug( "BASELINE="+b.toString() + ". tag="+tag.toString() );
			
			if( tag.QueryTag( tq ) )
			{
				bls.add( b );
			}
		}
		
		return bls;
	}
	
	public BaselineList NewerThanRecommended() throws UCMException
	{
		BaselineList bls = new BaselineList( this );
		List<Baseline> recommended = this.stream.GetRecommendedBaselines();
		
		if( recommended.size() != 1 )
		{
			logger.warning( "Only one baseline can be recommended simultaneously, " + recommended.size() + " found." );
			throw new UCMException( "Only one baseline can be recommended simultaneously, " + recommended.size() + " found." );
		}
		
		Baseline recbl = recommended.get( 0 );
		logger.debug( "The recommended=" + recbl.toString() );
		logger.debug( "REC COMP=" + recbl.GetComponent().GetFQName() );
		logger.debug( "THIS COM=" + component.GetFQName() );
		
		if( !recbl.GetComponent().GetFQName().equals( component.GetFQName() ) )
		{
			logger.warning( component.GetFQName() + " is not represented in " + stream.GetFQName() + " Recommended baseline" );
			throw new UCMException( component.GetFQName() + " is not represented in " + stream.GetFQName() + " Recommended baseline" );
		}
		
		boolean match = false;
		while( !bls.isEmpty() && !match )
		{
			Baseline b = bls.remove( 0 );
			match = b.GetFQName().equals( recbl.GetFQName() );
			logger.debug( "Matching: " + b.toString() + " == " + recbl.GetFQName() );
		}
		
		return bls;
	}
	
}