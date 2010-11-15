package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.ucm.UCMException;
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
	
	/* The encapsulated BaselineList class */
	public class BaselineList extends ArrayList<Baseline>
	{
		private Stream stream = null;
		
		private BaselineList( BaselineList bls )
		{
			this.stream = bls.stream;
			this.addAll( bls );
		}
		
		private BaselineList( Stream stream )
		{
			this.stream = stream;
		}
		
		public BaselineList( Stream stream, Plevel plevel )
		{
			this.stream = stream;
			this.addAll( context.GetBaselines( stream, Component.this, plevel ) );
		}
		
		public BaselineList Filter( TagQuery tq, String tagType, String tagID )
		{
			BaselineList bls = new BaselineList( this.stream );
			
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
			ArrayList<Baseline> recommended = this.stream.GetRecommendedBaselines();
			
			if( recommended.size() != 1 )
			{
				logger.warning( "Only one baseline can be recommended simultaneously, " + recommended.size() + " found." );
				throw new UCMException( "Only one baseline can be recommended simultaneously, " + recommended.size() + " found." );
			}
			
			Baseline recbl = recommended.get( 0 );
			logger.debug( "The recommended=" + recbl.toString() );
			logger.debug( "REC COMP=" + recbl.GetComponent().GetFQName() );
			logger.debug( "THIS COM=" + Component.this.GetFQName() );
			
			if( !recbl.GetComponent().GetFQName().equals( Component.this.GetFQName() ) )
			{
				logger.warning( Component.this.GetFQName() + " is not represented in " + stream.GetFQName() + " Recommended baseline" );
				throw new UCMException( Component.this.GetFQName() + " is not represented in " + stream.GetFQName() + " Recommended baseline" );
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
	
	public BaselineList GetBaselines( Stream stream )
	{
		return new BaselineList( stream, null );
	}
	
	public BaselineList GetBaselines( Stream stream, Plevel plevel )
	{
		return new BaselineList( stream, plevel );
	}

}
