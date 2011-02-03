package net.praqma.clearcase.ucm.entities;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.utils.BaselineList;
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
	private Project project                          = null;
	
	
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
	public static Stream Create( Stream pstream, String nstream, boolean readonly, Baseline baseline ) throws UCMException
	{
		UCMEntity.GetNamePart( nstream );
		
		if( pstream == null || nstream == null )
		{
			logger.error( "Incorrect CreateStream() parameters" );
			throw new UCMException( "Incorrect CreateStream() parameters" );
		}
		
		return context.CreateStream( pstream, nstream, readonly, baseline );
	}
	
	public void Load() throws UCMException
	{
		String[] data = context.LoadStream( this ).split( UCM.delim );
		
		/* name::project */
		this.project = UCMEntity.GetProject( data[1] );
		
		this.loaded = true;
	}
	
	/**
	 * Determines whether a Stream exists, given a fully qualified name
	 * @param fqname Fully qualified name
	 * @return True if the Stream exists, false otherwise
	 * @throws UCMException Is thrown if the fully qualified name is not a valid name
	 */
	public static boolean StreamExists( String fqname ) throws UCMException
	{
		/* Determine the name of the entity */
		UCMEntity.GetNamePart( fqname );
		
		return context.StreamExists( fqname );
	}
	
	public void Rebase( SnapshotView view, Baseline baseline, boolean complete )
	{
		context.RebaseStream( view, this, baseline, complete );
	}
	
	public boolean IsRebaseInProgress()
	{
		return context.IsRebaseInProgress( this );
	}
	
	public void CancelRebase()
	{
		context.CancelRebase( this );
	}
	
	public List<Baseline> GetRecommendedBaselines() throws UCMException
	{
		return GetRecommendedBaselines( false );
	}
	
	public void Generate()
	{
		context.Genereate( this );
	}
	
	public ArrayList<Baseline> GetRecommendedBaselines( boolean force ) throws UCMException
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
	
	public BaselineList GetLatestBaselines() throws UCMException
	{
		return new BaselineList( context.GetLatestBaselines( this ) );
	}
	
	public Component GetSingleTopComponent() throws UCMException
	{
		List<Baseline> bls = this.GetRecommendedBaselines();
		
		if( bls.size() != 1 )
		{
			throw new UCMException( "The Stream " + this.GetShortname() + " does not have a single composite component." );
		}
		
		return bls.get( 0 ).GetComponent();
	}
	
	public Project getProject() throws UCMException
	{
		if( !this.loaded ) Load();
		
		return this.project;
	}
	
	public String Stringify() throws UCMException
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
