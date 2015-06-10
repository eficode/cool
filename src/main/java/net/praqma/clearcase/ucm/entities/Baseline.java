package net.praqma.clearcase.ucm.entities;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.api.Describe;
import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.*;
import net.praqma.clearcase.interfaces.Diffable;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import org.apache.commons.lang.StringUtils;

public class Baseline extends UCMEntity implements Diffable {

	transient static final private Logger logger = Logger.getLogger( Baseline.class.getName()  );

    /**
     * The {@link Component} of the {@link Baseline}
     */
	private Component component = null;

    /**
     * The {@link PromotionLevel} of the {@link Baseline}
     */
	private Project.PromotionLevel plevel = Project.PromotionLevel.INITIAL;

    /**
     * The {@link Stream} of the {@link Baseline}
     */
	private Stream stream = null;
    
    //TODO: Usage of this? See FB case 
    private transient ArrayList<Activity> activities = new ArrayList<Activity>();
	
	public enum LabelBehaviour {
		NOLABEL,
		INCREMENTAL,
		FULL,
		full,
		none,
		DEFAULT;
		
		public String toArgument() {
			switch( this ) {
			case NOLABEL:
				return "-nlabel";
			case INCREMENTAL:
				return "-incremental";
			case FULL:
				return "-full";
			case full:
				return "-full";
			case none:
				return "-nlabel";
			default:
				return "";
			}
		}
		
		public static LabelBehaviour fromIncremental( boolean incremental ) {
			return ( incremental ? INCREMENTAL : FULL );
		}
	}

	Baseline() {
		super( "baseline" );
	}

	/**
	 * Load the Baseline into memory from ClearCase.<br>
	 * This function is automatically called when needed by other functions.
	 * 
	 * @throws UnableToLoadEntityException
	 * @throws UnableToInitializeEntityException 
     * @return The baseline in question, with stream and component data
	 */
    @Override
	public Baseline load() throws UnableToLoadEntityException, UnableToInitializeEntityException {
        /**
         * [FB11107] Performance tweaks. Avoid unnecssary load of baseline if already loaded. If it is loaded then return immediately return
         */
        if(loaded) {
            return this;
        }
        
		String result = "";

		String cmd = "desc -fmt %n" + Cool.delim + "%X[component]p" + Cool.delim + "%X[bl_stream]p" + Cool.delim + "%[plevel]p" + Cool.delim + "%u" + Cool.delim + "%Nd" + Cool.delim + "%[label_status]p" + Cool.delim + "%[master]p " + this;
		try {
			result = Cleartool.run( cmd ).stdoutBuffer.toString();
		} catch( Exception e ) {
			throw new UnableToLoadEntityException( this, e );
		}

		String[] rs = result.split( UCMEntity.delim );
        logger.fine( "Result:" + result );

		/* Component */
		String c = ( rs[1].matches( "^component:.*$" ) ? "" : "component:" ) + ( rs[1].matches( ".*@" + PVob.rx_tag_format + "$" ) ? rs[1] : rs[1] + "@" + this.pvob );
		/* Stream */
		if( rs[2].trim().length() > 0 ) {
			String s = ( rs[2].matches( "^stream:.*$" ) ? "" : "stream:" ) + ( rs[2].matches( ".*@" + PVob.rx_tag_format + "$" ) ? rs[2] : rs[2] + "@" + this.pvob );
			this.stream = Stream.get( s );
		} else {
			logger.fine( "No stream set for baseline" );
		}

		/* Now with factory creation! */
		this.component = Component.get( c );
		this.plevel = Project.getPlevelFromString( rs[3] );
		this.user = rs[4];
		try {
            synchronized( dateFormatter ) {
            	this.date = dateFormatter.parse( rs[5] );
            }
		} catch( ParseException e ) {
			logger.fine( "Unable to parse date: " + e.getMessage() );
			this.date = null;
		}

		this.labelStatus = getLabelStatusFromString( rs[6] );
		
		/* mastership */
		this.mastership = rs[7];

		this.loaded = true;

		return this;
	}
	
	public static Baseline create( String basename, Component component, File view, LabelBehaviour labelBehaviour, boolean identical ) throws UnableToInitializeEntityException, UnableToCreateEntityException, NothingNewException {
		return create( basename, component, view, labelBehaviour, identical, null, null );
	}
    
    public static Baseline create(Stream stream, Component component, String basename, File view, LabelBehaviour labelBehaviour, boolean identical)  throws UnableToInitializeEntityException, UnableToCreateEntityException, NothingNewException  {
		/* Remove prefixed baseline: */
		if( basename.toLowerCase().startsWith( "baseline:" ) ) {
			basename = basename.replaceFirst( "baseline:", "" );
		}

		boolean created = false;
        String cmd = "mkbl -nc" + ( identical ? " -identical" : "" );
        cmd += " " + labelBehaviour.toArgument();
		cmd += " " + basename;
        String out = "";        
        try {
			if( view != null ) {
				out = Cleartool.run( cmd, view ).stdoutBuffer.toString();
			} else {
				out = Cleartool.run( cmd ).stdoutBuffer.toString();
			}
			logger.fine("Baseline output:");
            logger.fine(out);

			created = out.matches( "(?s).*Created baseline \".*?\" in component \".*?\".*" ); // Created baseline

		} catch( AbnormalProcessTerminationException e ) {
			throw new UnableToCreateEntityException( Baseline.class, e );
		}
        
        String newname = null;

		if( created ) {
            if(component != null) {
                try {
                    Pattern p = Pattern.compile("^Created baseline \"(.*)\" in component \""+component.getShortname()+"\".$", Pattern.MULTILINE);
                    Matcher m = p.matcher(out);
                    if(m.find()) {
                        newname = m.group(1);
                    }
                    logger.fine("Got labeled name: "+newname);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error", ex);                
                }
                
                if(newname == null) {
                    throw new NothingNewException( String.format( "Baselines were creted. but was unable to find a baseline created for component %s", component) );
                }
            }
            return get( newname == null ? basename : newname , component.getPVob() ); 
		} else {
			throw new NothingNewException( "No baseline created, nothing new." );
		}
    }

	/**
	 * Given a baseline basename, a component and a view, the baseline is
	 * created.
     * @return Created baselines with the given arguments
	 */
	public static Baseline create( String basename, Component component, File view, LabelBehaviour labelBehaviour, boolean identical, List<Activity> activities, List<Component> depends ) throws UnableToInitializeEntityException, UnableToCreateEntityException, NothingNewException {
		/* Remove prefixed baseline: */
		if( basename.toLowerCase().startsWith( "baseline:" ) ) {
			basename = basename.replaceFirst( "baseline:", "" );
		}

		boolean created = false;
        String cmd = null;
        
        if(component != null) { 
            cmd = "mkbl -nc -component " + component.getNormalizedName() + ( identical ? " -identical" : "" );
        } else {
            cmd = "mkbl -nc" + ( identical ? " -identical" : "" );
        }
        
		if( depends != null && depends.size() > 0 ) {
			cmd += " -adepends_on ";
			for( Component c : depends ) {
				cmd += c.getNormalizedName() + ",";
			}
			cmd = cmd.substring( 0, ( cmd.length() - 1 ) );
		}

		if( activities != null ) {
			cmd += " -activities";
			for( Activity a : activities ) {
				cmd += " " + a.getFullyQualifiedName() + ",";
			}
			cmd = cmd.substring( 0, ( cmd.length() - 1 ) );
		}

		cmd += " " + labelBehaviour.toArgument();
		cmd += " " + basename;
        String newname = null;
		try {
			String out = "";
			if( view != null ) {
				out = Cleartool.run( cmd, view ).stdoutBuffer.toString();
			} else {
				out = Cleartool.run( cmd ).stdoutBuffer.toString();
			}
			logger.fine("Baseline output:");
            logger.fine(out);

			created = out.matches( "(?s).*Created baseline \".*?\" in component \".*?\".*" ); // Created baseline
           
            //If component is unspecified...we must look for a particular name
            if(component != null) {
                try {
                    Pattern p = Pattern.compile("^Created baseline \"(.*)\" in component \""+component.getShortname()+"\".$", Pattern.MULTILINE);
                    Matcher m = p.matcher(out);
                    if(m.find()) {
                        newname = m.group(1);
                    }
                    logger.fine("Got labeled name: "+newname);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Error", ex);                
                }
            }
            
            
		} catch( AbnormalProcessTerminationException e ) {
			throw new UnableToCreateEntityException( Baseline.class, e );
		}

		if( created ) {
			return get( newname == null ? basename : newname , component.getPVob() );
		} else {
			throw new NothingNewException( "No baseline created, nothing new." );
		}
	}

    public PromotionLevel getPromotionLevel() {
        autoLoad();

        return plevel;
    }

    //TODO: This method should be refactored. The parameter is redundant, as we use the same method regardless.
	/**
	 * Return the promotion level of a baseline. <br>
	 * If <code>cached</code> is not set, the promotion level is loaded from
	 * ClearCase.
	 * @param cached
	 *            Whether to use the cached promotion level or not
	 * @return The promotion level of the Baseline
	 */   
	public Project.PromotionLevel getPromotionLevel( boolean cached ) {
		if( cached ) {
			return getPromotionLevel();
		} else {
            //String cmd = "desc -fmt %[plevel]p " + this;
			return getPromotionLevel();
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
	 * 
	 * @return The new promotion level.
	 * @throws UnableToPromoteBaselineException
	 */
	public Project.PromotionLevel promote() throws UnableToPromoteBaselineException {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}

		if( this.plevel.equals( PromotionLevel.REJECTED ) ) {
			throw new UnableToPromoteBaselineException( this, PromotionLevel.REJECTED );
		}

		this.plevel = Project.promoteFrom( this.plevel );
		setPromotionLevel( this.plevel );

		return this.plevel;
	}

	/**
	 * <code>REJECTED</code> the Baseline.
     * @return 
     * @throws net.praqma.clearcase.exceptions.UnableToPromoteBaselineException
	 */
	public Project.PromotionLevel reject() throws UnableToPromoteBaselineException {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}

		setPromotionLevel( PromotionLevel.REJECTED );
        this.plevel = Project.PromotionLevel.REJECTED;

		return Project.PromotionLevel.REJECTED;
	}

    /**
     * Set the {@link PromotionLevel}
     * @param plevel
     * @throws net.praqma.clearcase.exceptions.UnableToPromoteBaselineException
     */
	public void setPromotionLevel( Project.PromotionLevel plevel ) throws UnableToPromoteBaselineException {
		String cmd = "chbl -level " + plevel + " " + this;
		try {
			Cleartool.run( cmd );
		} catch( AbnormalProcessTerminationException e ) {
			throw new UnableToPromoteBaselineException( this, this.plevel );
		}

        this.plevel = plevel;
	}

    /**
     * Set the {@link PromotionLevel} of this {@link Baseline}, without persisting it in ClearCase.
     * @param plevel
     */
    public void setLocalPromotionLevel( Project.PromotionLevel plevel ) {
        this.plevel = plevel;
    }

	public Component getComponent() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		return this.component;
	}

    /**
     * Get the {@link List} of {@link Baseline}'s, that depends on this {@link Baseline}.
     * @return A list of dependant baselines
     * @throws net.praqma.clearcase.exceptions.CleartoolException
     * @throws net.praqma.clearcase.exceptions.UnableToInitializeEntityException
     */
    public List<Baseline> getDependent() throws CleartoolException, UnableToInitializeEntityException {
        logger.fine( "Finding dependent baselines for " + this.getNormalizedName() );

        String[] ds = new net.praqma.clearcase.Describe( this ).dependentsOn().describe().get( "depends_on" );
        
        List<Baseline> baselines = new ArrayList<Baseline>( ds.length );
        for( String bl : ds ) {
            //For JENKINS-26623
            if(!StringUtils.isBlank(bl)) {
                baselines.add( Baseline.get( bl ) );
            }
        }

        return baselines;
    }

	public Stream getStream() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		return this.stream;
	}
	
	public boolean shouldResetMastership() throws ClearCaseException {
		if( !getMastership().equals( this.getStream().getOriginalMastership() ) ) {
			return true;
		}
		return false;
	}

	public void resetMastership() throws ClearCaseException {
		this.setMastership( this.getStream().getOriginalMastership() );
	} 

    @Override
	public String stringify() {
		if( !loaded ) {
			try {
				load();
			} catch( ClearCaseException e ) {
				throw new EntityNotLoadedException( fqname, fqname + " could not be auto loaded", e );
			}
		}
		
		StringBuffer sb = new StringBuffer();

		try {
			if( !this.loaded ) {
                load();
            }

			sb.append( " * Level    : " + this.plevel + linesep );
			sb.append( " * Component: " + this.component.toString() + linesep );
			sb.append( " * Stream   : " + this.stream.toString() + linesep );
			sb.append( " * Date     : " + this.date.toString() + linesep );

		} catch( Exception e ) {

		} finally {
			sb.insert( 0, super.stringify() );
		}

		return sb.toString();
	}

	public static Baseline get( String name ) throws UnableToInitializeEntityException {
		if( !name.startsWith( "baseline:" ) ) {
			name = "baseline:" + name;
		}
		Baseline entity = (Baseline) UCMEntity.getEntity( Baseline.class, name );
		return entity;
	}

	public static Baseline get( String name, PVob pvob ) throws UnableToInitializeEntityException {
		if( !name.startsWith( "baseline:" ) ) {
			name = "baseline:" + name;
		}
		Baseline entity = (Baseline) UCMEntity.getEntity( Baseline.class, name + "@" + pvob );
		return entity;
	}
	
	public List<Baseline> getPostedBaselinesFor( Component component ) throws UnableToInitializeEntityException, CleartoolException {
        logger.fine( "Getting posted baselines for " + this + " and " + component );

        List<Baseline> baselines = getCompositeMemberBaselines( component );

        if( baselines.isEmpty()) {
            if( this.getComponent().equals(component) ) {
                logger.fine( "Baseline " + this.getFullyQualifiedName() + " used because it was not rootless, and matched component " + component.getFullyQualifiedName());
                baselines.add(this);
            } else {
                logger.warning( "Could not find a baseline matching component: " + component.getFullyQualifiedName());
            }
        }
        return baselines;
    }

    /**
     * Get all the composite {@link Baseline}s that are members of this {@link Baseline}.
     * That is, {@link Baseline}s that are descendants of this.
     */
    public List<Baseline> getCompositeMemberBaselines() throws UnableToInitializeEntityException, CleartoolException {
        return getCompositeMemberBaselines( null );
    }

    /**
     * Get all the composite {@link Baseline}s that are members of this {@link Baseline} given a specific {@link Component}. <br />
     * That is, {@link Baseline}s that are descendants of this.
     */
    public List<Baseline> getCompositeMemberBaselines( Component component ) throws UnableToInitializeEntityException, CleartoolException {
		logger.fine( "Getting composite member baselines for " + this + ( component != null ? " and " + component : "" ) );

        Map<String, String[]> bls_str = new Describe( this ).addModifier( Describe.memberOfClosure ).describe();

		List<Baseline> bls = new ArrayList<Baseline>();

        for( String bl : bls_str.get( "member_of_closure" ) ) {
            logger.fine( "Baseline " + bl );
            if(!StringUtils.isBlank(bl)) {
                Baseline b = Baseline.get( bl );
                if( component == null || b.getComponent().equals( component ) ) {
                    bls.add( b );
                }
            }
        }

        return bls;
    }

    /**
     * Get all the composite {@link Baseline}s that are dependant on this {@link Baseline}.
     * That is, {@link Baseline}s that are ancestors of this.
     */
    public List<Baseline> getCompositeDependantBaselines() throws UnableToInitializeEntityException, CleartoolException {
        return getCompositeDependantBaselines( null );
    }

    /**
     * Get all the composite {@link Baseline}s that are dependant on this {@link Baseline} given a specific {@link Component}. <br />
     * That is, {@link Baseline}s that are ancestors of this.
     */
    public List<Baseline> getCompositeDependantBaselines( Component component ) throws UnableToInitializeEntityException, CleartoolException {
        logger.fine( "Getting composite dependant baselines for " + this + ( component != null ? " and " + component : "" ) );

        Map<String, String[]> bls_str = new Describe( this ).addModifier( Describe.dependsOnClosure ).describe();

        List<Baseline> bls = new ArrayList<Baseline>();

        for( String bl : bls_str.get( "depends_on_closure" ) ) {
            logger.fine( "Baseline " + bl );
            if(!StringUtils.isBlank(bl)) {
                Baseline b = Baseline.get( bl );
                if( component == null || b.getComponent().equals( component ) ) {
                    bls.add( b );
                }
            }
        }

        return bls;
    }

    /**
     * Get the rooted version of this {@link Baseline}. Returns null if none found.
     */
    public Baseline getRootedBaseline() throws UnableToInitializeEntityException, CleartoolException {
        List<Baseline> baselines = getCompositeDependantBaselines();

        /* Find a rooted baseline */
        Baseline b = null;
        for( Baseline baseline : baselines ) {
            if( baseline.getFullyQualifiedName().contains( getShortname() ) && !baseline.getComponent().isRootLess() ) {
                return baseline;
            }
        }

        return null;
    }
    
    public boolean isCompositeBaseline() throws UnableToInitializeEntityException, CleartoolException {
        return getCompositeDependantBaselines().isEmpty();
    }

}
