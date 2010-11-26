package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.view.SnapshotView;

/**
 * This is the OO implementation of the ClearCase entity Stream
 * @author wolfgang
 *
 */
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
	
	/**
	 * Create a new stream, given a parent Stream, a fully qualified name for the new Stream and whether the Stream is read only or not 
	 * @param pstream The parent Stream
	 * @param nstream The fully qualified name of the new Stream
	 * @param readonly Whether the new Stream is read only or not
	 * @return A new Stream given the parameters
	 */
	public static Stream Create( Stream pstream, String nstream, boolean readonly, Baseline baseline )
	{
		if( pstream == null || nstream == null )
		{
			logger.error( "Incorrect CreateStream() parameters" );
			throw new UCMException( "Incorrect CreateStream() parameters" );
		}
		
		return context.CreateStream( pstream, nstream, readonly, baseline );
	}
	
	/**
	 * Determines whether a Stream exists, given a fully qualified name
	 * @param fqname Fully qualified name
	 * @return True if the Stream exists, false otherwise
	 */
	public static boolean StreamExists( String fqname )
	{
		return context.StreamExists( fqname );
	}
	
	public void Rebase( SnapshotView view, Baseline baseline, boolean complete )
	{
		
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
		return context.RecommendBaseline( this, baseline );
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
