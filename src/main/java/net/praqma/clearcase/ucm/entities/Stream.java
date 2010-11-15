package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;

public class Stream extends UCMEntity
{
	/* Stream specific fields */
	private ArrayList<Baseline> recommendedBaselines = null;
	
	
	Stream()
	{
	}
	
	/**
	 * This method is only available to the package, because only UCMEntity should
	 * be allowed to call it.
	 * @return A new Stream Entity
	 */
	static Stream GetEntity()
	{
		return new Stream();
	}
	
	public ArrayList<Baseline> GetRecommendedBaselines()
	{
		return GetRecommendedBaselines( false );
	}
	
	public ArrayList<Baseline> GetRecommendedBaselines( boolean force )
	{
		logger.trace_function();
		logger.debug( "Getting recommended baselines" );
		
		if( this.recommendedBaselines == null || force )
		{
			this.recommendedBaselines = context.GetRecommendedBaselines( this );
		}
		
		return this.recommendedBaselines;
	}
	
	public boolean RecommendBaseline( Baseline baseline )
	{
		return context.RecommnedBaseline( this, baseline );
	}
	
	public String Stringify()
	{
		if( !this.loaded ) Load();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append( super.Stringify() );
		
		if( this.recommendedBaselines != null )
		{
			sb.append( "Recommended baselines: " + this.recommendedBaselines.size() + linesep );
			for( Baseline b : this.recommendedBaselines )
			{
				sb.append( "\t" + b.toString() + linesep );
			}
		}
		else
		{
			sb.append( "Recommended baselines: Undefined/not loaded" + linesep );
		}

		return sb.toString();
	}
}
