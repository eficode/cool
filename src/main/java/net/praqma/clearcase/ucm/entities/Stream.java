package net.praqma.clearcase.ucm.entities;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umd.cs.findbugs.annotations.*;
import net.praqma.clearcase.Deliver;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.clearcase.interfaces.StreamContainable;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import net.praqma.util.execute.CmdResult;

/**
 * This is the OO implementation of the ClearCase entity Stream
 * 
 * @author wolfgang
 * 
 */
@SuppressFBWarnings("")
public class Stream extends UCMEntity implements Diffable, Serializable, StreamContainable {

	private static final String rx_stream_load = "\\s*Error: stream not found\\s*";

	private static final transient Logger logger = Logger.getLogger( Stream.class.getName() );

    /**
     * The list of recommended {@link Baseline}s
     */
	private ArrayList<Baseline> recommendedBaselines = null;

    /**
     * The {@link Project} for this {@link Stream}
     */
	private Project project = null;

    /**
     * The default target {@link Stream} for this {@link Stream}
     */
	private Stream defaultTarget = null;

    /**
     * If this {@link Stream} is read only or not
     */
	private boolean readOnly = true;

    /**
     * The list of foundation {@link Baseline}s for this {@link Stream}
     */
	private List<Baseline> foundations = new ArrayList<Baseline>();
	
	private String status = null;

    /**
     * The parent {@link Stream}
     */
	private Stream parent;

	public Stream() {
		super( "stream" );
	}

	static Stream getEntity() {
		return new Stream();
	}
	
	public static Stream create( Stream parent, String nstream, boolean readonly, Baseline baseline ) throws UnableToCreateEntityException, UnableToInitializeEntityException {
		List<Baseline> baselines = new ArrayList<Baseline>();
		baselines.add( baseline );
		return create( parent, nstream, readonly, baselines );
	}

	/**
	 * Create a new stream in ClearCase, given a parent Stream, a fully qualified name for
	 * the new Stream and whether the Stream is read only or not
	 * 
	 * @param parent
	 *            The parent Stream
	 * @param nstream
	 *            The fully qualified name of the new Stream
	 * @param readonly
	 *            Whether the new Stream is read only or not
     * @param baselines Initial configuration of baselines for the stream
	 * @return A new Stream given the parameters
     * @throws net.praqma.clearcase.exceptions.UnableToCreateEntityException Thrown when ClearCase reports errors 
     * @throws net.praqma.clearcase.exceptions.UnableToInitializeEntityException Thrown when ClearCase reports errors 
	 */
	public static Stream create( StreamContainable parent, String nstream, boolean readonly, List<Baseline> baselines ) throws UnableToCreateEntityException, UnableToInitializeEntityException {
		logger.fine( "Creating stream " + nstream + " as child of " + parent );

		String cmd = "mkstream -in " + parent;
		if( baselines != null && baselines.size() > 0 ) {
            logger.finest( "Baselines: " + baselines );
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
            /* The nstream wasn't prefixed with "stream:" */
			nstream = "stream:" + nstream + "@" + parent.getPVob();
		}
		
		cmd += " " + nstream;
		
		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
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

    /**
     * Create an integration {@link Stream} in ClearCase
     * @param name The name of the new integration stream
     * @param project The project under which the stream is created
     * @param baselines Initial configuration of baselines
     * @return The created {@link Stream}
     * @throws net.praqma.clearcase.exceptions.UnableToCreateEntityException Thrown when ClearCase reports errors 
     * @throws net.praqma.clearcase.exceptions.UCMEntityNotFoundException Thrown when ClearCase reports errors 
     * @throws net.praqma.clearcase.exceptions.UnableToGetEntityException Thrown when ClearCase reports errors 
     * @throws net.praqma.clearcase.exceptions.UnableToInitializeEntityException Thrown when ClearCase reports errors 
     */
	public static Stream createIntegration( String name, Project project, List<Baseline> baselines ) throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException, UnableToInitializeEntityException {
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
            /* name wasn't prefixed with "stream:" */
			name = "stream:" + name + "@" + project.getPVob();
		}
				
		cmd += " " + name;

		try {
			Cleartool.run( cmd );
		} catch( Exception e ) {
			throw new UnableToCreateEntityException( Stream.class, e );
		}

		return Stream.get( name );
	}

    @Override
	public Stream load() throws UCMEntityNotFoundException, UnableToLoadEntityException, UnableToInitializeEntityException {
		logger.fine( "loading stream" );

		String rawdata = "";

		String cmd = "describe -fmt %[name]p}{%[project]Xp}{%X[def_deliver_tgt]p}{%[read_only]p}{%[found_bls]Xp}{%[master]p " + this;
		try {
			rawdata = Cleartool.run( cmd ).stdoutBuffer.toString();
		} catch( AbnormalProcessTerminationException e ) {
			if( e.getMessage().matches( rx_stream_load ) ) {
				throw new UCMEntityNotFoundException( this, e );
			} else {
				throw new UnableToLoadEntityException( this, e );
			}
		}

		String[] data = rawdata.split( "\\}\\{" );
		logger.fine( "I got: " + Arrays.asList( data ) );

		/* Set project */
		setProject( Project.get( data[1] ) );

		/* Set default target, if it exists */
		if( !data[2].trim().equals( "" ) ) {
            defaultTarget = Stream.get( data[2].trim() );
		}

		/* Set read only */
		if( data[3].length() > 0 ) {
			setReadOnly( true );
		} else {
			setReadOnly( false );
		}

		/* Set foundation baseline */
        String[] blss = data[4].trim().split( "\\s+" );
        for( String bls : blss ) {
            if( bls != null && !bls.isEmpty() ) {
                addFoundationBaseline( Baseline.get( bls ) );
            }
        }

		/* Set mastership */
		try {
			String ms = data[5].trim();
			this.mastership = ms;
		} catch( Exception e ) {
			logger.warning( "Could not set mastership: " + e.getMessage() );
            /* PEH */
		}

		this.loaded = true;

		return this;
	}

	public List<Stream> getChildStreams( boolean multisitePolling ) throws UnableToInitializeEntityException, CleartoolException {
		/* We need to load this, because we need the mastership */
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
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
			}

            logger.finest( "Command result: " + res.stdoutBuffer.toString() );

			String[] strms = res.stdoutBuffer.toString().split( ", " );
			for( String stream : strms ) {
				streams.add( Stream.get( stream ) );
			}

		} catch( UCMEntityNotFoundException e ) {
			logger.fine( "The Stream has no child streams" );
		}
		
		/**/
		Iterator<Stream> it = streams.iterator();
		String mastership = this.getMastership();
		while( it.hasNext() ) {
			Stream stream = it.next();
			String childMastership = stream.getMastership();
			logger.fine( "Child Mastership = " + childMastership );

			if( !multisitePolling && stream.hasPostedDelivery() ) {
				logger.fine( "Removing [" + stream.getShortname() + "] due to non-supported posted delivery" );
				it.remove();
			} else if( !mastership.equals( childMastership ) ) {
				logger.fine( "Removing [" + stream.getShortname() + "] due to different mastership" );
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

                List<Baseline> possibleBaselines = b.getPostedBaselinesFor( component );
                for( Baseline bl : possibleBaselines ) {
                    if( bl.getPromotionLevel() == plevel ) {
                        res.add( bl );
                    }
                }
			}
		} catch( Exception e ) {
			throw new UnableToInitializeEntityException( Baseline.class, e );
		}
		
		return res;
	}
	
	public boolean hasPostedDelivery() throws UnableToInitializeEntityException {
		try {			
            String statustext = Deliver.getStatus(this);
            boolean hasPostedDelivery = statustext.contains( "Operation posted from" );
            logger.fine("Posted deliver: " + hasPostedDelivery +"\n"+statustext);
			return hasPostedDelivery;
		} catch( Exception e ) {
			throw new UnableToInitializeEntityException( Stream.class, e );
		}
	}

    /**
     * Set the {@link Project} of the {@link Stream}
     * @param project The {@link Project}
     */
	public void setProject( Project project ) {
		this.project = project;
	}

    /**
     * Set the default target {@link Stream} in ClearCase
     * @param stream The {@link Stream}
     * @throws CleartoolException Thrown when ClearCase reports errors 
     */
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
	 * @return A list of {@link Stream}s
     * @throws net.praqma.clearcase.exceptions.UnableToListProjectsException Thrown when ClearCase reports errors 
     * @throws net.praqma.clearcase.exceptions.UnableToInitializeEntityException Thrown when ClearCase reports errors 
     * @throws net.praqma.clearcase.exceptions.UnableToLoadEntityException Thrown when ClearCase reports errors 
     * @throws net.praqma.clearcase.exceptions.UCMEntityNotFoundException Thrown when ClearCase reports errors 
	 */
	public List<Stream> getSiblingStreams() throws UnableToListProjectsException, UnableToInitializeEntityException, UnableToLoadEntityException, UCMEntityNotFoundException {
		logger.fine( "Getting sibling streams" );
		List<Project> projects = Project.getProjects( this.getPVob() );
		List<Stream> streams = new ArrayList<Stream>();

		for( Project p : projects ) {
            p.load();
            p.getIntegrationStream().load();
            Stream dts = p.getIntegrationStream().getDefaultTarget();
            if( dts != null && this.equals( dts ) ) {
                streams.add( p.getIntegrationStream() );
            }
		}

		logger.fine( streams.toString() );

		return streams;
	}
    
    /**
     * For JENKINS-26484 
     * @param hyperLinkName The name of the hyperlink
     * @return A list of streams 
     * @throws UnableToListProjectsException Thrown when ClearCase cannot list projects
     * @throws UnableToInitializeEntityException Thrown when ClearCase reports error
     * @throws UnableToLoadEntityException Thrown when ClearCase reports error
     * @throws UCMEntityNotFoundException Thrown when ClearCase reports error 
     * @throws net.praqma.clearcase.exceptions.HyperlinkException Thrown when a ClearTool error occurs 
     */
    public List<Stream> getDeliveringStreamsUsingHlink(String hyperLinkName) throws UnableToListProjectsException, UnableToInitializeEntityException, UnableToLoadEntityException, UCMEntityNotFoundException, HyperlinkException {
		logger.fine( String.format( "Getting delivering streams using hyperlink of type: %s", hyperLinkName) );
		List<Project> projects = Project.getProjects( this.getPVob() );
		List<Stream> streams = new ArrayList<Stream>();
		for( Project p : projects ) {
            
            p.load();
            List<HyperLink> links = p.getIntegrationStream().getHyperlinks(hyperLinkName, null);
            //This is the integration stream we're interested in. Should we look for baslines?
            //Compare the mastership of this integration stream, to the one configured in the job.            
            Stream intStream = p.getIntegrationStream();
            String sMastership = intStream.getMastership();
            
            if(links.isEmpty()) {
                logger.fine( String.format("Found no hyperlinks on integration stream: %s", intStream.getFullyQualifiedName()) );
            }            
            
            for(HyperLink hl : links) {
                logger.fine(String.format("Found hyperlink with name %s, value was %s", hyperLinkName, hl.getValue()));                
                if(this.getFullyQualifiedName().equals( hl.getValue() ) ) {                                        
                    if (sMastership.equals(this.getMastership())) {
                        logger.fine(String.format("Hyperlink of type %s with value %s matches stream: %s", hyperLinkName, hl.getValue(), intStream)) ;
                        logger.fine(String.format("Adding ingration stream %s to list of streams to look for baselines", intStream));
                        logger.fine(String.format("The local mastership is: %s", this.getMastership()));
                        logger.fine(String.format("Mastership of integration stream %s is: %s", intStream, sMastership));
                        streams.add(intStream);                    
                    } else {
                        logger.fine(String.format("The stream %s was not mastered locally, it is mastered in %s", intStream, sMastership));
                    }
                }
            }
		}

		logger.fine( streams.toString() );

		return streams;
	}

	/**
	 * Determines whether a Stream exists, given a fully qualified name
	 * 
	 * @param fqname
	 *            Fully qualified name
	 * @return True if the Stream exists, false otherwise
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

    /**
     * Get the {@link Stream} delivering to this {@link Stream}. Null if no {@link Deliver} is in progress.
     * @param isMultiSite Multisite
     * @return The delivering {@link Stream}
     * @throws net.praqma.clearcase.exceptions.ClearCaseException Thrown when ClearCase reports errors 
     */
    public Stream getDeliveringStream( boolean isMultiSite ) throws ClearCaseException {
        /* Heuristic: Typically a child stream delivering
         * Let's ask them first. */

        logger.finer( "Searching amongst child-streams" );
        Stream s = getDeliveringStream( isMultiSite, getChildStreams( isMultiSite ) );

        if( s == null ) {
            logger.finer( "Searching among all streams" );
            s = getDeliveringStream( isMultiSite, this.getProject().getStreams() );
        }

        logger.fine( "Found " + s );
        return s;
    }

    /**
     * @param isMultiSite Is multisite enabled?
     * @param streams Streams
     * @return The delivering {@link Stream}
     * @throws ClearCaseException Thrown when ClearCase reports errors  
     */
    public Stream getDeliveringStream( boolean isMultiSite, List<Stream> streams ) throws ClearCaseException {

        for( Stream stream : streams ) {
            Deliver.Status status = stream.getDeliverStatus();
            logger.finer( "Status: " + status );
            if( status.isInProgress() && status.getSourceStream().equals( stream ) ) {
                return stream;
            }
        }

        return null;
    }

    public Deliver.Status getDeliverStatus() throws ClearCaseException {
        return Deliver.Status.getStatus( this );
    }


	public List<Baseline> getRecommendedBaselines() throws UnableToListBaselinesException, UnableToInitializeEntityException {
		return getRecommendedBaselines( false );
	}

    /**
     * Update the config spec of the {@link Stream}
     * @throws CleartoolException Thrown when ClearCase reports error
     */
	public void generate() throws CleartoolException {
		String cmd = "chstream -generate " + this;
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to generate " + this, e );
		}
	}

	public List<Baseline> getRecommendedBaselines( boolean force ) throws UnableToListBaselinesException, UnableToInitializeEntityException {
		logger.fine( "Getting recommended baselines" );

		if( this.recommendedBaselines == null || force ) {
			ArrayList<Baseline> bls = new ArrayList<Baseline>();

			String result = "";
			String cmd = "desc -fmt %[rec_bls]p " + this;
			try {
				result = Cleartool.run( cmd ).stdoutBuffer.toString();
			} catch( AbnormalProcessTerminationException e ) {
				throw new UnableToListBaselinesException( this, null, null, e );
			}
			
			String[] rs = result.split( " " );

			for( int i = 0; i < rs.length; i++ ) {
				/* There is something in the element. */
				if( rs[i].matches( "\\S+" ) ) {
					bls.add( Baseline.get( rs[i], pvob ) );
				}
			}

			return bls;
		}

		return this.recommendedBaselines;
	}

    /**
     * Recommend the {@link Baseline} for this {@link Stream} in ClearCase
     * @param baseline The {@link Baseline} to recommend
     * @throws net.praqma.clearcase.exceptions.CleartoolException Thrown when ClearCase reports errors 
     */
	public void recommendBaseline( Baseline baseline ) throws CleartoolException {
		String cmd = "chstream -recommend " + baseline + " " + this;
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to recommend " + baseline, e );
		}
	}

	public List<Baseline> getLatestBaselines() throws UnableToInitializeEntityException, CleartoolException {
		String cmd = "desc -fmt %[latest_bls]Xp " + this;
		List<String> lines;
		try {
			lines = Cleartool.run( cmd, null, false ).stdoutList;
		} catch( AbnormalProcessTerminationException e ) {
			throw new CleartoolException( "Unable to get latest baselines from " + this, e );
		}
		
		List<Baseline> bls = new ArrayList<Baseline>();
		for( String line : lines ) {
			for( String s : line.split( " " ) ) {
                bls.add( Baseline.get( s.trim() ) );
			}
		}
		
		logger.fine( "The list is " + bls );

		return bls;
	}

    /**
     * Get the latest {@link Baseline} from this {@link Stream} and a {@link Component}.
     * @param component The {@link Component}
     * @return The latest {@link Baseline} under the given {@link Component}
     * @throws net.praqma.clearcase.exceptions.UnableToInitializeEntityException Thrown when ClearCase reports errors 
     * @throws net.praqma.clearcase.exceptions.CleartoolException Thrown when ClearCase reports errors 
     */
    public Baseline getLatestBaseline( Component component ) throws UnableToInitializeEntityException, CleartoolException {
        List<Baseline> baselines = getLatestBaselines();

        for( Baseline baseline : baselines ) {
            if( baseline.getComponent().equals( component ) ) {
                return baseline;
            }
        }

        return null;
    }

	public Component getSingleTopComponent() throws NoSingleTopComponentException, UnableToInitializeEntityException {
		List<Baseline> bls;
		try {
			bls = this.getRecommendedBaselines();
		} catch( UnableToListBaselinesException e ) {
			throw new NoSingleTopComponentException( this );
		}

		if( bls.size() != 1 ) {
			throw new NoSingleTopComponentException( this );
		}

		return bls.get( 0 ).getComponent();
	}

	public Project getProject() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		return this.project;
	}
	
	public List<UCMView> getViews() throws CleartoolException, ViewException {
        logger.fine( "Finding views for " + this.getNormalizedName() );

		String cmd = "describe -fmt %X[views]p " + this;
		
		String[] viewsString = null;
		try {
            viewsString = Cleartool.run( cmd ).stdoutBuffer.toString().split( "\\s+" );
            logger.finest( "Output: " + Arrays.asList( viewsString ) );
		} catch( Exception e ) {
			throw new CleartoolException( "Unable to list views for " + this, e );
		}

        List<UCMView> views = new ArrayList<UCMView>( viewsString.length );
		
		for( String view : viewsString ) {
            if( !view.isEmpty() ) {
                try {
                    views.add( UCMView.getView( view ) );
                } catch( Exception e ) {
                    logger.log( Level.WARNING, "View failed", e );
                }
            } else {
                logger.finest( "View tag was empty" );
            }
		}
		
		return views;
	}

    /**
     * Remove this {@link Stream} from ClearCase
     * @throws UnableToRemoveEntityException Thrown when ClearCase reports errors 
     */
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
	 */
	public Stream getDefaultTarget() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		return this.defaultTarget;
	}


	public void setReadOnly( boolean readOnly ) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		return readOnly;
	}

	/**
	 * @param baseline The {@link Baseline} to use as foundation for this {@link Stream}
	 */
	public void setFoundationBaseline( Baseline baseline ) {
		this.foundations.clear();
		this.foundations.add( baseline );
	}

    /**
     * Add a foundation {@link Baseline} for this {@link Stream}
     * @param baseline The {@link Baseline} to add as a foundation baselin to this {@link Stream}  
     */
	public void addFoundationBaseline( Baseline baseline ) {
		this.foundations.add( baseline );
	}

	/**
	 * Get the first foundation baseline. This is a method implemented to maintain backwards compatibility.
     * @return The foundation {@link Baseline}
	 */
	public Baseline getFoundationBaseline() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		return this.foundations.get( 0 );
	}

    /**
     *
     * @return A list of {@link Baseline}s, never null
     */
	public List<Baseline> getFoundationBaselines() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		return this.foundations;
	}

	public void setParent( Stream parent ) {
		this.parent = parent;
	}

	public Stream getParent() {
		return parent;
	}

    /**
     * Get the original mastership from ClearCase
     * @return The original mastership
     * @throws net.praqma.clearcase.exceptions.CleartoolException Thrown when ClearCase reports errors 
     */
	public String getOriginalMastership() throws CleartoolException {
		Matcher m = Pattern.compile( ".*Operation posted from replica \"(\\w*)\".*" ).matcher( Deliver.getStatus( this ) );
		if( m.find() ) {
			logger.warning( "Posted from replica : " + m.group( 1 ) );
			return m.group( 1 );
		}
		return this.getMastership();
	}

    @Override
	public String stringify() {
		StringBuilder sb = new StringBuilder();
		try {
			if( !this.loaded ) load();

			if( this.recommendedBaselines != null ) {
				sb.append("Recommended baselines: ").append(this.recommendedBaselines.size()).append(linesep);
				for( Baseline b : this.recommendedBaselines ) {
					sb.append("\t").append(b.toString()).append(linesep);
				}
			} else {
				sb.append("Recommended baselines: Undefined/not loaded").append(linesep);
			}
		} catch( Exception e ) {

		} finally {
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
