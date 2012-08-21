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
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.EntityNotLoadedException;
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
private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

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
tracer.entering(Stream.class.getSimpleName(), "Stream");
		super( "stream" );
tracer.exiting(Stream.class.getSimpleName(), "Stream");
	}

	/**
	 * This method is only available to the package, because only UCMEntity
	 * should be allowed to call it.
	 * 
	 * @return A new Stream Entity
	 */
	static Stream getEntity() {
tracer.entering(Stream.class.getSimpleName(), "getEntity");
tracer.exiting(Stream.class.getSimpleName(), "getEntity", new Stream());
		return new Stream();
	}
	
	public static Stream create( Stream parent, String nstream, boolean readonly, Baseline baseline ) throws UnableToCreateEntityException, UnableToInitializeEntityException {
tracer.entering(Stream.class.getSimpleName(), "create", new Object[]{parent, nstream, readonly, baseline});
		List<Baseline> baselines = new ArrayList<Baseline>();
		baselines.add( baseline );
tracer.exiting(Stream.class.getSimpleName(), "create", create( parent, nstream, readonly, baselines ));
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
tracer.entering(Stream.class.getSimpleName(), "create", new Object[]{parent, nstream, readonly, baselines});
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

tracer.exiting(Stream.class.getSimpleName(), "create", stream);
		return stream;
	}
		
	public static Stream createIntegration( String name, Project project, Baseline baseline ) throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException, UnableToInitializeEntityException {
tracer.entering(Stream.class.getSimpleName(), "createIntegration", new Object[]{name, project, baseline});
		List<Baseline> baselines = new ArrayList<Baseline>();
		baselines.add( baseline );
tracer.exiting(Stream.class.getSimpleName(), "createIntegration", createIntegration( name, project, baselines ));
		return createIntegration( name, project, baselines );
	}
	
	public static Stream createIntegration( String name, Project project, List<Baseline> baselines ) throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException, UnableToInitializeEntityException {
tracer.entering(Stream.class.getSimpleName(), "createIntegration", new Object[]{name, project, baselines});
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

tracer.exiting(Stream.class.getSimpleName(), "createIntegration", Stream.get( name ));
		return Stream.get( name );
	}

	public Stream load() throws UCMEntityNotFoundException, UnableToLoadEntityException, UnableToInitializeEntityException {
tracer.entering(Stream.class.getSimpleName(), "load");
		logger.debug( "loading stream" );
		//context.loadStream( this );

		String rawdata = "";

		String cmd = "describe -fmt %[name]p}{%[project]Xp}{%X[def_deliver_tgt]p}{%[read_only]p}{%[found_bls]Xp}{%[master]p " + this;
		try {
			rawdata = Cleartool.run( cmd ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			if( e.getMessage().matches( rx_stream_load ) ) {
				//throw new UCMException( "The component \"" + this + "\", does not exist.", UCMType.LOAD_FAILED );
				throw new UCMEntityNotFoundException( this, e );
			} else {
				//throw new UCMException( e.getMessage(), e.getMessage(), UCMType.LOAD_FAILED );
				throw new UnableToLoadEntityException( this, e );
			}
		}

		String[] data = rawdata.split( "\\}\\{" );
		logger.debug( "I got: " + data );

		/* Set project */
		setProject( Project.get( data[1] ) );

		/* Set default target, if exists */
		if( !data[2].trim().equals( "" ) ) {
			try {
				setDefaultTarget( Stream.get( data[2].trim() ) );
			} catch( Exception e ) {
				logger.debug( "The Stream did not have a default target." );
			}
		}

		/* Set read only */
		if( data[3].length() > 0 ) {
			setReadOnly( true );
		} else {
			setReadOnly( false );
		}

		/* Set foundation baseline */
		try {
			String[] blss = data[4].split( "\\s+" );
			for( String bls : blss ) {
				addFoundationBaseline( Baseline.get( bls ) );
			}
		} catch( Exception e ) {
			logger.warning( "Could not get the foundation baseline: " + e.getMessage() );
		}
		
		/* Set mastership */
		try {
			String ms = data[5].trim();
			this.mastership = ms;
		} catch( Exception e ) {
			logger.warning( "Could not set mastership: " + e.getMessage() );
		}

		this.loaded = true;

tracer.exiting(Stream.class.getSimpleName(), "load", this);
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
		/* We need to load this, because we need the mastership */
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
tracer.exiting(Stream.class.getSimpleName(), "EntityNotLoadedException");
tracer.entering(Stream.class.getSimpleName(), "EntityNotLoadedException", new Object[]{fqname, ", not, auto, e});
			}
		}

		List<Stream> streams = new ArrayList<Stream>();
		try {
			CmdResult res = null;

			String cmd = "desc -fmt %[dstreams]CXp " + this;
			try {
				res = Cleartool.run( cmd );
			} catch( AbnormalProcessTerminationException e ) {
				throw new UCMEntityNotFoundException( this, e );
tracer.exiting(Stream.class.getSimpleName(), "UCMEntityNotFoundException");
tracer.entering(Stream.class.getSimpleName(), "UCMEntityNotFoundException", new Object[]{e});
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
tracer.exiting(Stream.class.getSimpleName(), "if");
tracer.entering(Stream.class.getSimpleName(), "if", new Object[]{childMastership});
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
tracer.exiting(Stream.class.getSimpleName(), "UnableToInitializeEntityException");
tracer.entering(Stream.class.getSimpleName(), "UnableToInitializeEntityException", new Object[]{e});
		}
		
		return res;
	}
	
	public boolean hasPostedDelivery() throws UnableToInitializeEntityException {
tracer.entering(Stream.class.getSimpleName(), "hasPostedDelivery");
		logger.debug( "hasPostedDelivery" );
		try {
			logger.debug( "Status: " + Deliver.getStatus( this ) );
tracer.exiting(Stream.class.getSimpleName(), "hasPostedDelivery", Deliver.getStatus( this ).contains( "Operation posted from" ));
			return Deliver.getStatus( this ).contains( "Operation posted from" );
		} catch( Exception e ) {
			throw new UnableToInitializeEntityException( Stream.class, e );
		}
	}

	public void setProject( Project project ) {
tracer.entering(Stream.class.getSimpleName(), "setProject", new Object[]{project});
		this.project = project;
tracer.exiting(Stream.class.getSimpleName(), "setProject");
	}

	public void setDefaultTarget( Stream stream ) throws CleartoolException {
tracer.entering(Stream.class.getSimpleName(), "setDefaultTarget", new Object[]{stream});
		
		String cmd = "chstream -target " + stream + " " + this;
		
		try {
			Cleartool.run( cmd );
			this.defaultTarget = stream;
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to change default target to " + stream.getNormalizedName(), e );
		}
tracer.exiting(Stream.class.getSimpleName(), "setDefaultTarget");
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
				p.getIntegrationStream().load();
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
tracer.entering(Stream.class.getSimpleName(), "streamExists", new Object[]{fqname});
		String cmd = "describe " + fqname;
		try {
			Cleartool.run( cmd );
tracer.exiting(Stream.class.getSimpleName(), "streamExists", true);
			return true;
		} catch( Exception e ) {
tracer.exiting(Stream.class.getSimpleName(), "streamExists", false);
			return false;
		}
	}

	public boolean exists() {
tracer.entering(Stream.class.getSimpleName(), "exists");
tracer.exiting(Stream.class.getSimpleName(), "exists", streamExists( this.fqname ));
		return streamExists( this.fqname );
	}



	public List<Baseline> getRecommendedBaselines() throws UnableToListBaselinesException, UnableToInitializeEntityException {
		return getRecommendedBaselines( false );
tracer.exiting(Stream.class.getSimpleName(), "getRecommendedBaselines", getRecommendedBaselines( false ));
tracer.entering(Stream.class.getSimpleName(), "getRecommendedBaselines");
	}

	public void generate() throws CleartoolException {
tracer.entering(Stream.class.getSimpleName(), "generate");
		String cmd = "chstream -generate " + this;
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to generate " + this, e );
		}
tracer.exiting(Stream.class.getSimpleName(), "generate");
	}

	public List<Baseline> getRecommendedBaselines( boolean force ) throws UnableToListBaselinesException, UnableToInitializeEntityException {
		logger.debug( "Getting recommended baselines" );

		if( this.recommendedBaselines == null || force ) {
			ArrayList<Baseline> bls = new ArrayList<Baseline>();

			String result = "";
			String cmd = "desc -fmt %[rec_bls]p " + this;
			try {
				result = Cleartool.run( cmd ).stdoutBuffer.toString();
			} catch( AbnormalProcessTerminationException e ) {
				//throw new UCMException( "Unable to get recommended baselines from " + stream + ": " + e.getMessage() );
tracer.exiting(Stream.class.getSimpleName(), "UCMException");
tracer.entering(Stream.class.getSimpleName(), "UCMException", new Object[]{to, recommended, from, +, +, ", e.getMessage()});
				throw new UnableToListBaselinesException( this, null, null, e );
tracer.exiting(Stream.class.getSimpleName(), "UnableToListBaselinesException");
tracer.entering(Stream.class.getSimpleName(), "UnableToListBaselinesException", new Object[]{null, e});
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
tracer.entering(Stream.class.getSimpleName(), "recommendBaseline", new Object[]{baseline});
		String cmd = "chstream -recommend " + baseline + " " + this;
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to recommend " + baseline, e );
		}
tracer.exiting(Stream.class.getSimpleName(), "recommendBaseline");
	}

	public List<Baseline> getLatestBaselines() throws UnableToInitializeEntityException, CleartoolException {
		String cmd = "desc -fmt %[latest_bls]Xp " + this;
		List<String> lines;
		try {
			lines = Cleartool.run( cmd, null, false ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to get latest baselines from " + this, e );
tracer.exiting(Stream.class.getSimpleName(), "CleartoolException");
tracer.entering(Stream.class.getSimpleName(), "CleartoolException", new Object[]{to, latest, from, +, e});
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
tracer.entering(Stream.class.getSimpleName(), "getSingleTopComponent");
		List<Baseline> bls;
		try {
			bls = this.getRecommendedBaselines();
		} catch( UnableToListBaselinesException e ) {
			throw new NoSingleTopComponentException( this );
		}

		if( bls.size() != 1 ) {
			throw new NoSingleTopComponentException( this );
		}

tracer.exiting(Stream.class.getSimpleName(), "getSingleTopComponent", bls.get( 0 ).getComponent());
		return bls.get( 0 ).getComponent();
	}

	public Project getProject() {
tracer.entering(Stream.class.getSimpleName(), "getProject");
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
tracer.exiting(Stream.class.getSimpleName(), "getProject", this.project);
		return this.project;
	}
	
	public List<UCMView> getViews() throws CleartoolException {
		String cmd = "describe -fmt %X[views]p " + this;
		
		String[] views = null;
		try {
			views = Cleartool.run( cmd ).stdoutBuffer.toString().split( " " );
			
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to list views for " + this, e );
tracer.exiting(Stream.class.getSimpleName(), "CleartoolException");
tracer.entering(Stream.class.getSimpleName(), "CleartoolException", new Object[]{to, views, ", this});
		}
		
		for( String view : views ) {
			
		}
		
		return null;
	}
	
	public void remove() throws UnableToRemoveEntityException {
tracer.entering(Stream.class.getSimpleName(), "remove");
		String cmd = "rmstream -force " + this;
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new UnableToRemoveEntityException( this, e );
		}
tracer.exiting(Stream.class.getSimpleName(), "remove");
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
tracer.entering(Stream.class.getSimpleName(), "getDefaultTarget");
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
tracer.exiting(Stream.class.getSimpleName(), "getDefaultTarget", this.defaultTarget);
		return this.defaultTarget;
	}


	public void setReadOnly( boolean readOnly ) {
tracer.entering(Stream.class.getSimpleName(), "setReadOnly", new Object[]{readOnly});
		this.readOnly = readOnly;
tracer.exiting(Stream.class.getSimpleName(), "setReadOnly");
	}

	public boolean isReadOnly() {
tracer.entering(Stream.class.getSimpleName(), "isReadOnly");
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
tracer.exiting(Stream.class.getSimpleName(), "isReadOnly", readOnly);
		return readOnly;
	}

	/**
	 * Add a single foundation baseline
	 * @param baseline
	 */
	public void setFoundationBaseline( Baseline baseline ) {
tracer.entering(Stream.class.getSimpleName(), "setFoundationBaseline", new Object[]{baseline});
		this.foundations.clear();
		this.foundations.add( baseline );
tracer.exiting(Stream.class.getSimpleName(), "setFoundationBaseline");
	}
	
	public void addFoundationBaseline( Baseline baseline ) {
tracer.entering(Stream.class.getSimpleName(), "addFoundationBaseline", new Object[]{baseline});
		this.foundations.add( baseline );
tracer.exiting(Stream.class.getSimpleName(), "addFoundationBaseline");
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
tracer.entering(Stream.class.getSimpleName(), "getFoundationBaseline");
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
tracer.exiting(Stream.class.getSimpleName(), "getFoundationBaseline", this.foundations.get( 0 ));
		return this.foundations.get( 0 );
	}
	
	public List<Baseline> getFoundationBaselines() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
tracer.exiting(Stream.class.getSimpleName(), "EntityNotLoadedException");
tracer.entering(Stream.class.getSimpleName(), "EntityNotLoadedException", new Object[]{fqname, ", not, auto, e});
			}
		}
		
		return this.foundations;
	}

	public void setParent( Stream parent ) {
tracer.entering(Stream.class.getSimpleName(), "setParent", new Object[]{parent});
		this.parent = parent;
tracer.exiting(Stream.class.getSimpleName(), "setParent");
	}

	public Stream getParent() {
tracer.entering(Stream.class.getSimpleName(), "getParent");
tracer.exiting(Stream.class.getSimpleName(), "getParent", parent);
		return parent;
	}
	
	public String getOriginalMastership() throws CleartoolException {
tracer.entering(Stream.class.getSimpleName(), "getOriginalMastership");
		Matcher m = Pattern.compile( ".*Operation posted from replica \"(\\w*)\".*" ).matcher( Deliver.getStatus( this ) );
		if( m.find() ) {
			logger.warning( "Posted from replica : " + m.group( 1 ) );
tracer.exiting(Stream.class.getSimpleName(), "getOriginalMastership", m.group( 1 ));
			return m.group( 1 );
		}
tracer.exiting(Stream.class.getSimpleName(), "getOriginalMastership", this.getMastership());
		return this.getMastership();
	}

	public String stringify() {
tracer.entering(Stream.class.getSimpleName(), "stringify");
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

tracer.exiting(Stream.class.getSimpleName(), "stringify", sb.toString());
		return sb.toString();
	}

	public static Stream get( String name ) throws UnableToInitializeEntityException {
tracer.entering(Stream.class.getSimpleName(), "get", new Object[]{name});
		if( !name.startsWith( "stream:" ) ) {
			name = "stream:" + name;
		}
		Stream entity = (Stream) UCMEntity.getEntity( Stream.class, name );
tracer.exiting(Stream.class.getSimpleName(), "get", entity);
		return entity;
	}

	public static Stream get( String name, PVob pvob ) throws UnableToInitializeEntityException {
tracer.entering(Stream.class.getSimpleName(), "get", new Object[]{name, pvob});
		if( !name.startsWith( "stream:" ) ) {
			name = "stream:" + name;
		}
		Stream entity = (Stream) UCMEntity.getEntity( Stream.class, name + "@" + pvob );
tracer.exiting(Stream.class.getSimpleName(), "get", entity);
		return entity;
	}

}
