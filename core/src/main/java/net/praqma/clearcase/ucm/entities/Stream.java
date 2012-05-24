package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.praqma.clearcase.Deliver;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.NoSingleTopComponentException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToListBaselinesException;
import net.praqma.clearcase.exceptions.UnableToListProjectsException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.UnableToRemoveEntityException;
import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.clearcase.interfaces.StreamContainable;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.utils.Baselines;
import net.praqma.clearcase.ucm.view.SnapshotView;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.debug.Logger;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

/**
 * This is the OO implementation of the ClearCase entity Stream
 * 
 * @author wolfgang
 * 
 */
public class Stream extends UCMEntity implements Diffable, Serializable, StreamContainable {

	private static final String rx_stream_load = "\\s*Error: stream not found\\s*";

	transient static private Logger logger = Logger.getLogger();

	/* Stream specific fields */
	private ArrayList<Baseline> recommendedBaselines = null;
	private Project project = null;
	private Stream defaultTarget = null;
	private boolean readOnly = true;
	private List<Baseline> foundations = new ArrayList<Baseline>();
	
	private String status = null;

	private Stream parent;

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
	
	public static Stream create( Stream parent, String nstream, boolean readonly, Baseline baseline ) throws UnableToCreateEntityException, UnableToInitializeEntityException {
		List<Baseline> baselines = new ArrayList<Baseline>();
		baselines.add( baseline );
		return create( parent, nstream, readonly, baselines );
	}

	/**
	 * Create a new stream, given a parent Stream, a fully qualified name for
	 * the new Stream and whether the Stream is read only or not
	 * 
	 * @param parent
	 *            The parent Stream
	 * @param nstream
	 *            The fully qualified name of the new Stream
	 * @param readonly
	 *            Whether the new Stream is read only or not
	 * @return A new Stream given the parameters
	 */
		public static Stream create( StreamContainable parent, String nstream, boolean readonly, List<Baseline> baselines ) throws UnableToCreateEntityException, UnableToInitializeEntityException {
		logger.debug( "Creating stream " + nstream + " as child of " + parent );

		String cmd = "mkstream -in " + parent;
		if( baselines != null && baselines.size() > 0 ) {
			cmd += " -baseline ";
			for( Baseline b : baselines ) {
				cmd += b.getNormalizedName() + ",";
			}
			cmd = cmd.substring( 0, ( cmd.length() - 1 ) );
		}
		
		cmd += ( readonly ? " -readonly" : "" );
		
		try {
			UCMEntity.getNamePart( nstream );
		} catch( CleartoolException e1 ) {
			nstream = "stream:" + nstream + "@" + parent.getPVob();
		}
		
		cmd += " " + nstream;
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			//throw new UCMException( "Could not create stream: " + e.getMessage() );
			throw new UnableToCreateEntityException( Stream.class, e );
		}

		Stream stream = Stream.get( nstream );

		stream.setCreated( true );

		if( parent != null && parent instanceof Stream ) {
			stream.setParent( (Stream)parent );
		}

		return stream;
	}
		
	public static Stream createIntegration( String name, Project project, Baseline baseline ) throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException, UnableToInitializeEntityException {
		List<Baseline> baselines = new ArrayList<Baseline>();
		baselines.add( baseline );
		return createIntegration( name, project, baselines );
	}
	
	public static Stream createIntegration( String name, Project project, List<Baseline> baselines ) throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException, UnableToInitializeEntityException {
		//context.createIntegrationStream( name, project, baseline );
		
		String cmd = "mkstream -integration -in " + project;
				
		if( baselines != null && baselines.size() > 0 ) {
			cmd += " -baseline ";
			for( Baseline b : baselines ) {
				cmd += b.getNormalizedName() + ",";
			}
			cmd = cmd.substring( 0, ( cmd.length() - 1 ) );
		}
		
		try {
			UCMEntity.getNamePart( name );
		} catch( CleartoolException e1 ) {
			name = "stream:" + name + "@" + project.getPVob();
		}
				
		cmd += " " + name;

		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			//throw new UCMException( "Could not create integration stream: " + e.getMessage(), UCMType.CREATION_FAILED );
			throw new UnableToCreateEntityException( Stream.class, e );
		}

		return Stream.get( name );
	}

	public Stream load() throws UCMEntityNotFoundException, UnableToLoadEntityException, UnableToInitializeEntityException {
		logger.debug( "loading stream" );
		//context.loadStream( this );

		List<String> data = null;

		String cmd = "describe -fmt %[name]p\\n%[project]Xp\\n%X[def_deliver_tgt]p\\n%[read_only]p\\n%[found_bls]Xp " + this;
		try {
			data = Cleartool.run( cmd ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			if( e.getMessage().matches( rx_stream_load ) ) {
				//throw new UCMException( "The component \"" + this + "\", does not exist.", UCMType.LOAD_FAILED );
				throw new UCMEntityNotFoundException( this, e );
			} else {
				//throw new UCMException( e.getMessage(), e.getMessage(), UCMType.LOAD_FAILED );
				throw new UnableToLoadEntityException( this, e );
			}
		}

		logger.debug( "I got: " + data );

		/* Set project */
		setProject( Project.get( data.get( 1 ) ) );

		/* Set default target, if exists */
		if( !data.get( 2 ).trim().equals( "" ) ) {
			try {
				setDefaultTarget( Stream.get( data.get( 2 ) ) );
			} catch( Exception e ) {
				logger.debug( "The Stream did not have a default target." );
			}
		}

		/* Set read only */
		if( data.get( 3 ).length() > 0 ) {
			setReadOnly( true );
		} else {
			setReadOnly( false );
		}

		/* Set foundation baseline */
		try {
			String[] blss = data.get( 4 ).split( "\\s+" );
			for( String bls : blss ) {
				addFoundationBaseline( Baseline.get( bls ) );
			}
		} catch( Exception e ) {
			logger.warning( "Could not get the foundation baseline: " + e.getMessage() );
		}

		this.loaded = true;

		return this;
	}

	public List<Baseline> getBaselines( PromotionLevel plevel ) throws UnableToInitializeEntityException, UnableToListBaselinesException, NoSingleTopComponentException {
		return Baselines.get( this, getSingleTopComponent(), plevel );
	}

	public List<Baseline> getBaselines( Component component, PromotionLevel plevel ) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		return Baselines.get( this, component, plevel );
	}

	public List<Baseline> getBaselines( Component component, PromotionLevel plevel, Date date ) throws UnableToInitializeEntityException, UnableToListBaselinesException {
		List<Baseline> baselines = Baselines.get( this, component, plevel );

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



	public List<Stream> getChildStreams( boolean multisitePolling ) throws UnableToInitializeEntityException, CleartoolException {

		List<Stream> streams = new ArrayList<Stream>();
		try {
			CmdResult res = null;

			String cmd = "desc -fmt %[dstreams]CXp " + this;
			try {
				res = Cleartool.run( cmd );
			} catch( AbnormalProcessTerminationException e ) {
				throw new UCMEntityNotFoundException( this, e );
			}

			String[] strms = res.stdoutBuffer.toString().split( ", " );
			for( String stream : strms ) {
				streams.add( Stream.get( stream ) );
			}

		} catch( UCMEntityNotFoundException e ) {
			logger.debug( "The Stream has no child streams" );
		}
		
		/**/
		Iterator<Stream> it = streams.iterator();
		String mastership = this.getMastership();
		while( it.hasNext() ) {
			Stream stream = it.next();
			String childMastership = stream.getMastership();
			logger.debug( "Child Mastership = " + childMastership );

			if( stream.hasPostedDelivery() && !multisitePolling ) {
				logger.debug( "Removing [" + stream.getShortname() + "] due to non-supported posted delivery" );
				it.remove();
			} else if( !mastership.equals( childMastership ) ) {
				logger.debug( "Removing [" + stream.getShortname() + "] due to different mastership" );
				it.remove();

			}

		}

		return streams;
	}
	
	public List<Baseline> getPostedBaselines( Component component, PromotionLevel plevel ) throws UnableToInitializeEntityException {
		List<Baseline> res = new ArrayList<Baseline>();

		try {
			if( status == null ) {
				status = Deliver.getStatus( this );
			}
			
			Matcher m = Pattern.compile( ".*baseline:(\\S*).*" ).matcher( Deliver.getStatus( this ) );
	
			if( m.find() ) {
				logger.warning( "Posted baseline : " + m.group( 1 ) );
	
				Baseline b = Baseline.get( m.group( 1 ) );
				
				if( b.getPromotionLevel( true ) == plevel ) {
					res.addAll( b.getPostedBaselinesFor(component) );
				}
			}
		} catch( Exception e ) {
			throw new UnableToInitializeEntityException( Baseline.class, e );
		}
		
		return res;
	}
	
	public boolean hasPostedDelivery() throws UnableToInitializeEntityException {
		logger.debug( "hasPostedDelivery" );
		try {
			logger.debug( "Status: " + Deliver.getStatus( this ) );
			return Deliver.getStatus( this ).contains( "Operation posted from" );
		} catch( Exception e ) {
			throw new UnableToInitializeEntityException( Stream.class, e );
		}
	}

	public void setProject( Project project ) {
		this.project = project;
	}

	public void setDefaultTarget( Stream stream ) throws CleartoolException {
		
		String cmd = "chstream -target " + stream + " " + this;
		
		try {
			Cleartool.run( cmd );
			this.defaultTarget = stream;
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to change default target to " + stream.getNormalizedName(), e );
		}
	}

	/**
	 * For each project return their integration streams
	 * 
	 * @return
	 */
	public List<Stream> getSiblingStreams() throws UnableToListProjectsException, UnableToInitializeEntityException {
		logger.debug( "Getting sibling streams" );
		List<Project> projects = Project.getProjects( this.getPVob() );
		List<Stream> streams = new ArrayList<Stream>();

		for( Project p : projects ) {
			try {
				p.load();
				logger.debug( "Checking " + p );
				logger.debug( "INT: " + p.getIntegrationStream() );
				logger.debug( "TGT: " + p.getIntegrationStream().getDefaultTarget() );
				if( p.getIntegrationStream().getDefaultTarget() != null && this.equals( p.getIntegrationStream().getDefaultTarget() ) ) {
					streams.add( p.getIntegrationStream() );
				}
			} catch( Exception e ) {
				/* Just move on! */
				logger.warning( "Could not check project " + p + ": " + e.getMessage() );
			}
		}

		logger.debug( streams );

		return streams;
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
	public static boolean streamExists( String fqname ) {
		String cmd = "describe " + fqname;
		try {
			Cleartool.run( cmd );
			return true;
		} catch( Exception e ) {
			return false;
		}
	}

	public boolean exists() {
		return streamExists( this.fqname );
	}



	public List<Baseline> getRecommendedBaselines() throws UnableToListBaselinesException, UnableToInitializeEntityException {
		return getRecommendedBaselines( false );
	}

	public void generate() throws CleartoolException {
		String cmd = "chstream -generate " + this;
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to generate " + this, e );
		}
	}

	public List<Baseline> getRecommendedBaselines( boolean force ) throws UnableToListBaselinesException, UnableToInitializeEntityException {
		logger.debug( "Getting recommended baselines" );

		if( this.recommendedBaselines == null || force ) {
			//this.recommendedBaselines = context.getRecommendedBaselines( this );
			ArrayList<Baseline> bls = new ArrayList<Baseline>();

			//String result = strategy.getRecommendedBaselines( stream.getFullyQualifiedName() );
			String result = "";
			String cmd = "desc -fmt %[rec_bls]p " + this;
			try {
				result = Cleartool.run( cmd ).stdoutBuffer.toString();
			} catch( AbnormalProcessTerminationException e ) {
				//throw new UCMException( "Unable to get recommended baselines from " + stream + ": " + e.getMessage() );
				throw new UnableToListBaselinesException( this, null, null, e );
			}
			
			String[] rs = result.split( " " );

			for( int i = 0; i < rs.length; i++ ) {
				/* There is something in the element. */
				if( rs[i].matches( "\\S+" ) ) {
					// bls.add( (Baseline)UCMEntity.GetEntity( rs[i], true ) );
					bls.add( Baseline.get( rs[i], pvob ) );
				}
			}

			return bls;
		}

		return this.recommendedBaselines;
	}

	public void recommendBaseline( Baseline baseline ) throws CleartoolException {
		String cmd = "chstream -recommend " + baseline + " " + this;
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Could not recommend Baseline: " + e.getMessage(), e.getMessage() );
			throw new CleartoolException( "Unable to recommend " + baseline, e );
		}
	}

	public List<Baseline> getLatestBaselines() throws UnableToInitializeEntityException, CleartoolException {
		//return context.getLatestBaselines( this );
		
		//List<String> bs = strategy.getLatestBaselines( stream.getFullyQualifiedName() );
		String cmd = "desc -fmt %[latest_bls]Xp " + this;
		List<String> lines;
		try {
			lines = Cleartool.run( cmd, null, false ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			//throw new UCMException( "Unable to get latest baseline from " + stream + ": " + e.getMessage() );
			throw new CleartoolException( "Unable to get latest baselines from " + this, e );
		}
		
		List<Baseline> bls = new ArrayList<Baseline>();
		for( String line : lines ) {
			for( String s : line.split( " " ) ) {
				try {
					bls.add( Baseline.get( s.trim() ) );
				} catch( Exception e ) {
					/* Unable to add, no op */
				}
			}
		}
		
		logger.debug( "The list is " + bls );

		return bls;
	}

	public Component getSingleTopComponent() throws NoSingleTopComponentException, UnableToInitializeEntityException {
		List<Baseline> bls;
		try {
			bls = this.getRecommendedBaselines();
		} catch( UnableToListBaselinesException e ) {
			throw new NoSingleTopComponentException( this );
		}

		if( bls.size() != 1 ) {
			//throw new Cleartool( "The Stream " + this.getShortname() + " does not have a single composite component." );
			throw new NoSingleTopComponentException( this );
		}

		return bls.get( 0 ).getComponent();
	}

	public Project getProject() {
		return this.project;
	}
	
	public List<UCMView> getViews() throws CleartoolException {
		String cmd = "describe -fmt %X[views]p " + this;
		
		String[] views = null;
		try {
			views = Cleartool.run( cmd ).stdoutBuffer.toString().split( " " );
			
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to list views for " + this, e );
		}
		
		for( String view : views ) {
			
		}
		
		return null;
	}
	
	public void remove() throws UnableToRemoveEntityException {
		String cmd = "rmstream -force " + this;
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new UnableToRemoveEntityException( this, e );
		}
	}

	/**
	 * This method returns the default Stream the given Stream will deliver to.
	 * 
	 * @return A Stream
	 * @throws UnableToLoadEntityException 
	 * @throws UCMEntityNotFoundException 
	 * @throws UnableToCreateEntityException 
	 * @throws UnableToGetEntityException 
	 * @throws UCMException
	 */
	public Stream getDefaultTarget() {
		return this.defaultTarget;
	}


	public void setReadOnly( boolean readOnly ) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Add a single foundation baseline
	 * @param baseline
	 */
	public void setFoundationBaseline( Baseline baseline ) {
		this.foundations.clear();
		this.foundations.add( baseline );
	}
	
	public void addFoundationBaseline( Baseline baseline ) {
		this.foundations.add( baseline );
	}

	/**
	 * Get the first foundation baseline. This is a method implemented to maintain backwards compatibility. 
	 * @return
	 * @throws UCMEntityNotFoundException
	 * @throws UnableToLoadEntityException
	 * @throws UnableToCreateEntityException
	 * @throws UnableToGetEntityException
	 */
	public Baseline getFoundationBaseline() {
		return this.foundations.get( 0 );
	}
	
	public List<Baseline> getFoundationBaselines() {
		return this.foundations;
	}

	public void setParent( Stream parent ) {
		this.parent = parent;
	}

	public Stream getParent() {
		return parent;
	}
	
	public String getOriginalMastership() throws CleartoolException {
		Matcher m = Pattern.compile( ".*Operation posted from replica \"(\\w*)\".*" ).matcher( Deliver.getStatus( this ) );
		if( m.find() ) {
			logger.warning( "Posted from replica : " + m.group( 1 ) );
			return m.group( 1 );
		}
		return this.getMastership();
	}

	public String stringify() {
		StringBuffer sb = new StringBuffer();
		try {
			if( !this.loaded ) load();

			if( this.recommendedBaselines != null ) {
				sb.append( "Recommended baselines: " + this.recommendedBaselines.size() + linesep );
				for( Baseline b : this.recommendedBaselines ) {
					sb.append( "\t" + b.toString() + linesep );
				}
			} else {
				sb.append( "Recommended baselines: Undefined/not loaded" + linesep );
			}
		} catch( Exception e ) {

		} finally {
			//sb.append( super.stringify() );
			sb.insert( 0, super.stringify() );
		}

		return sb.toString();
	}

	public static Stream get( String name ) throws UnableToInitializeEntityException {
		if( !name.startsWith( "stream:" ) ) {
			name = "stream:" + name;
		}
		Stream entity = (Stream) UCMEntity.getEntity( Stream.class, name );
		return entity;
	}

	public static Stream get( String name, PVob pvob ) throws UnableToInitializeEntityException {
		if( !name.startsWith( "stream:" ) ) {
			name = "stream:" + name;
		}
		Stream entity = (Stream) UCMEntity.getEntity( Stream.class, name + "@" + pvob );
		return entity;
	}

}
