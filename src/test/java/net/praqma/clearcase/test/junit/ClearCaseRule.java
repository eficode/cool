package net.praqma.clearcase.test.junit;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.praqma.clearcase.api.Describe;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.logging.LoggingUtil;
import net.praqma.logging.PraqmaticLogHandler;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.praqma.clearcase.Environment;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.test.annotations.ClearCaseFullVobName;
import net.praqma.clearcase.test.annotations.ClearCaseLess;
import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.view.UCMView;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.clearcase.util.SetupUtils;

public class ClearCaseRule extends Environment implements TestRule {

    private static Logger logger = Logger.getLogger( ClearCaseRule.class.getName() );

	protected Description testDescription;
	
	protected String baseName;
	protected String vobName;
	protected String uniqueBaseName;
	protected String uniqueName;
	protected File setupFile;

    protected File logdir;
	
	public ClearCaseRule( String name ) {
		this.baseName = name;
		this.uniqueBaseName = name + "_" + Environment.getUniqueTimestamp();
        logToFile();
	}
	
	public ClearCaseRule( String name, String setupFile ) {
		this.baseName = name;
		this.uniqueBaseName = name + "_" + Environment.getUniqueTimestamp();
		this.setupFile = new File( Environment.class.getClassLoader().getResource( setupFile ).getFile() );
        logToFile();
	}

    public ClearCaseRule logToFile() {
        if( System.getenv().containsKey( "BUILD_NUMBER" ) ) {
            Integer number = new Integer( System.getenv( "BUILD_NUMBER" ) );
            this.logdir = new File( new File( new File( new File( System.getProperty( "user.dir" ) ), "test-logs" ), number.toString() ), getSafeName( baseName ) );
            this.logdir.mkdirs();
        } else {
            System.out.println( "NO BUILD NUMBER" );
        }

        return this;
    }

    public static String getSafeName( String name ) {
        return name.replaceAll( "[^\\w]", "_" );
    }


    public String getVobName() {
		return vobName;
	}
	
	public String getUniqueName() {
		return uniqueName;
	}

	protected void before( String name ) throws Exception {

        if( logdir != null ) {
            File logfile = new File( logdir, name );
            List<String> loggers = new ArrayList<String>(2);
            loggers.add( "net.praqma" );
            try {
                LoggingUtil.setPraqmaticHandler( Level.ALL, loggers, logfile );
            } catch( FileNotFoundException e ) {
                e.printStackTrace();
            }
        }

		variables.put( "name", name );
		
		this.vobName = name;
		this.uniqueName = name;
		
		if( setupFile != null ) {
			bootStrap( setupFile );
		} else {
			bootStrap();
		}
		
		if( context.getVariable( "vobname" ) != null ) {
			this.vobName = context.getVariable( "vobname" );
		}
	}

    public void printComponents() {
        for( String c : context.components.keySet() ) {
            try {
                Component component = context.components.get( c );
                logger.fine( "Printing " + component.toString() );

                String blname = new Describe( component ).addModifier( Describe.initialBaseline ).execute().get( 0 );
                logger.info( "Baseline name: " + blname );
                Baseline baseline = Baseline.get( blname );
                List<Baseline> dependents = baseline.getDependent();
                logger.info( "Dependent:" );
                for( Baseline d : dependents ) {
                    logger.info( " * " + d.getComponent() );
                }
            } catch( Exception e ) {
                e.printStackTrace();
            }
        }
    }

	protected void after() {

        //printComponents();

		if( System.getProperty( "saveEnv", null ) == null ) {
			if( getPVob() != null ) {
				try {
					SetupUtils.tearDown( getPVob() );
				} catch( Exception e ) {
					ExceptionUtils.print( e, System.out, true );
				}
			} else {
				/* Not possible to tear down */
			}
		} else {
            logger.info( "== Saving environment ==" );
		}

        if( logdir != null ) {
            int threadId = (int) Thread.currentThread().getId();
            Logger logger = Logger.getLogger( "net.praqma" );
            for( Handler handler : logger.getHandlers() ) {
                if( handler instanceof PraqmaticLogHandler ) {
                    PraqmaticLogHandler h = (PraqmaticLogHandler) handler;
                    if( h.getThreadId() == threadId ) {
                        logger.removeHandler( handler );
                        handler.close();
                    }
                }
            }

        }
	}

	@Override
	public Statement apply( final Statement base, final Description description ) {
		
		/* ClearCase less test, just return base */
		if( description.getAnnotation( ClearCaseLess.class ) != null ) {
			return base;
		}
		
		/* Test for ClearCase annotations */
		String uniqueName = this.uniqueBaseName;
		
		/* Set an explicit vob name */
		if( description.getAnnotation( ClearCaseFullVobName.class ) != null ) {
			ClearCaseFullVobName d = description.getAnnotation( ClearCaseFullVobName.class );
			if( d.name().length() > 0 ) {
				uniqueName = d.name();
			}
		}
		
		/* Set a unique vob name */
		if( description.getAnnotation( ClearCaseUniqueVobName.class ) != null ) {
			ClearCaseUniqueVobName d = description.getAnnotation( ClearCaseUniqueVobName.class );
			if( d.name().length() > 0 ) {
				uniqueName = uniqueName + "-" + d.name();
			}
		}
		
		final String theUniqueName = uniqueName;
		
		return new Statement() {
						
			@Override
			public void evaluate() throws Throwable {
				testDescription = description;
				Thread t = Thread.currentThread();
				String o = t.getName();
				t.setName( "Executing " + testDescription.getDisplayName() );
				logger.fine( "===== Setting up ClearCase =====" );
				before( theUniqueName );
				try {
					base.evaluate();
				} finally {
                    logger.fine( "===== Tearing down ClearCase =====" );
					after();
					testDescription = null;
					t.setName( o );
				}
			}
		};
	}

	
	
	public File getDynamicPath( String viewtag ) {
		return new File( context.mvfs + "/" + viewtag + "/" + getVobName() );
	}
	
	public File setDynamicActivity( Stream stream, String viewtag, String name ) throws ClearCaseException {
		System.out.println( "VIEW: " + context.views.get( viewtag ) );
		File path = new File( context.mvfs + "/" + viewtag + "/" + getVobName() );
				
		System.out.println( "PATH: " + path );
		
		Activity activity = Activity.create( name, stream, getPVob(), true, "activity for " + name, null, path );
		UCMView.setActivity( activity, path, null, null );
		
		return path;
	}
	
}
