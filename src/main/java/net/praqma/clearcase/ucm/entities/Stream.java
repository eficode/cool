package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Project.Plevel;
import net.praqma.clearcase.ucm.utils.BaselineList;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.util.debug.Logger;

/**
 * This is the OO implementation of the ClearCase entity Stream
 * 
 * @author wolfgang
 * 
 */
public class Stream extends UCMEntity implements Diffable, Serializable {

	private static final long serialVersionUID = 112121212L;
	
	transient static private Logger logger = Logger.getLogger();
	
	/* Stream specific fields */
	transient private ArrayList<Baseline> recommendedBaselines = null;
	private Project project = null;
	private Stream defaultTarget = null;
	private boolean readOnly = true;
	private Baseline foundation;
	
	public Stream() {
		super( "stream" );
	}

	/**
	 * This method is only available to the package, because only UCMEntity
	 * should be allowed to call it.
	 * 
	 * @return A new Stream Entity
	 */
	static Stream getEntity() {
		return new Stream();
	}

	/**
	 * Create a new stream, given a parent Stream, a fully qualified name for
	 * the new Stream and whether the Stream is read only or not
	 * 
	 * @param pstream
	 *            The parent Stream
	 * @param nstream
	 *            The fully qualified name of the new Stream
	 * @param readonly
	 *            Whether the new Stream is read only or not
	 * @return A new Stream given the parameters
	 */
	public static Stream create( Stream pstream, String nstream, boolean readonly, Baseline baseline ) throws UCMException {
		UCMEntity.getNamePart( nstream );
		
		logger.debug( "PSTREAM:" + pstream.getShortname() + ". NSTREAM: " + nstream + ". BASELINE: " + baseline.getShortname() );

		if( pstream == null || nstream == null ) {
			throw new UCMException( "Incorrect CreateStream() parameters" );
		}

		return context.createStream( pstream, nstream, readonly, baseline );
	}

	public static Stream createIntegration( String name, Project project, Baseline baseline ) throws UCMException {
		context.createIntegrationStream( name, project, baseline );

		return UCMEntity.getStream( name, project.getPVob(), true );
	}

	public void load() throws UCMException {
		logger.debug( "loading stream" );
		context.loadStream( this );
		
		this.loaded = true;
	}

	public List<Baseline> getBaselines( Plevel plevel ) throws UCMException {
		return context.getBaselines( this, getSingleTopComponent(), plevel, pvob );
	}
	
	public List<Baseline> getBaselines( Component component, Plevel plevel ) throws UCMException {
		return context.getBaselines( this, component, plevel, pvob );
	}
	
	public List<Baseline> getBaselines( Component component, Plevel plevel, Date date ) throws UCMException {
		List<Baseline> baselines = context.getBaselines( this, component, plevel, pvob );
		
		if( date == null ) {
			return baselines;
		}
		
		Iterator<Baseline> it = baselines.iterator();
		while( it.hasNext() ) {
			Baseline baseline = it.next();
			
			if( date.after( baseline.getDate() ) ) {
				logger.debug( "Removing [" + baseline.getShortname() + " " + baseline.getDate() + "/" + date + "]" );
				it.remove();
			}
		}
		
		return baselines;
	}

	public List<Stream> getChildStreams() throws UCMException {

		List<Stream> res = new ArrayList<Stream>();
		try {
			res = context.getChildStreams( this );
		} catch (UCMException e) {
			logger.info( "The Stream has no child streams" );
		}
		return res;
	}
	
	public void setProject( Project project ) {
		this.project = project;
	}
	
	public void setDefaultTarget( Stream stream ) {
		this.defaultTarget = stream;
	}

	/**
	 * For each project return their integration streams
	 * 
	 * @return
	 * @throws UCMException
	 */
	public List<Stream> getSiblingStreams() throws UCMException {
		List<Project> projects = Project.getProjects( this.getPVob() );
		List<Stream> streams = new ArrayList<Stream>();

		for( Project p : projects ) {
			try {
				if( p.getIntegrationStream().getDefaultTarget() != null && this.equals( p.getIntegrationStream().getDefaultTarget() ) ) {
					streams.add( p.getIntegrationStream() );
				}
			} catch( Exception e ) {
				/* Just move on! */
			}
		}

		return streams;
	}

	public boolean equals( Stream other ) {
		if( this.fqname.equals( other.getFullyQualifiedName() ) ) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determines whether a Stream exists, given a fully qualified name
	 * 
	 * @param fqname
	 *            Fully qualified name
	 * @return True if the Stream exists, false otherwise
	 * @throws UCMException
	 *             Is thrown if the fully qualified name is not a valid name
	 */
	public static boolean streamExists( String fqname ) throws UCMException {
		/* Determine the name of the entity */
		UCMEntity.getNamePart( fqname );

		return context.streamExists( fqname );
	}
	
	public boolean exists() {
		return context.streamExists( fqname );
	}

	public void rebase( SnapshotView view, Baseline baseline, boolean complete ) {
		context.rebaseStream( view, this, baseline, complete );
	}

	public boolean isRebaseInProgress() {
		return context.isRebasing( this );
	}

	public void cancelRebase() {
		context.cancelRebase( this );
	}

	public List<Baseline> getRecommendedBaselines() throws UCMException {
		return getRecommendedBaselines( false );
	}

	public void generate() {
		context.genereate( this );
	}

	public ArrayList<Baseline> getRecommendedBaselines( boolean force ) throws UCMException {
		logger.debug( "Getting recommended baselines" );

		if( this.recommendedBaselines == null || force ) {
			this.recommendedBaselines = context.getRecommendedBaselines( this );
		}

		return this.recommendedBaselines;
	}

	public void recommendBaseline( Baseline baseline ) throws UCMException {
		context.recommendBaseline( this, baseline );
	}

	public BaselineList getLatestBaselines() throws UCMException {
		return new BaselineList( context.getLatestBaselines( this ) );
	}

	public Component getSingleTopComponent() throws UCMException {
		List<Baseline> bls = this.getRecommendedBaselines();

		if( bls.size() != 1 ) {
			throw new UCMException( "The Stream " + this.getShortname() + " does not have a single composite component." );
		}

		return bls.get( 0 ).getComponent();
	}

	public Project getProject() throws UCMException {
		if( !this.loaded ) load();

		return this.project;
	}

	/**
	 * This method returns the default Stream the given Stream will deliver to.
	 * 
	 * @return A Stream
	 * @throws UCMException
	 */
	public Stream getDefaultTarget() throws UCMException {
		if( !this.loaded ) {
			load();
		}
		return this.defaultTarget;
	}

	public boolean deliver( Baseline baseline, Stream target, File viewcontext, String viewtag, boolean force, boolean complete, boolean abort ) throws UCMException {
		try {
			return context.deliver( baseline, this, target, viewcontext, viewtag, force, complete, abort );
		} catch (UCMException e) {
			logger.warning( "Could not deliver baseline: " + e.getMessage() );
			logger.warning( e );
			throw e;
		}
	}

	public void cancelDeliver( File viewcontext ) throws UCMException {
		context.cancelDeliver( viewcontext, this );
	}

	public boolean isDelivering() throws UCMException {
		return context.isDelivering( this );
	}
	
	public void setReadOnly( boolean readOnly ) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() throws UCMException {
		if( !this.loaded ) load();
		return readOnly;
	}
	
	public void setFoundationBaseline( Baseline baseline ) {
		this.foundation = baseline;
	}
	
	public Baseline getFoundationBaseline() throws UCMException {
		if( !this.loaded ) load();
		
		return this.foundation;
	}

	public String stringify() throws UCMException {
		if( !this.loaded ) load();

		StringBuffer sb = new StringBuffer();

		sb.append( super.stringify() );

		if( this.recommendedBaselines != null ) {
			sb.append( "Recommended baselines: " + this.recommendedBaselines.size() + linesep );
			for( Baseline b : this.recommendedBaselines ) {
				sb.append( "\t" + b.toString() + linesep );
			}
		} else {
			sb.append( "Recommended baselines: Undefined/not loaded" + linesep );
		}

		return sb.toString();
	}

}
