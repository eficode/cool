package net.praqma.clearcase.test.junit;

import java.io.File;
import java.util.logging.Logger;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.praqma.clearcase.Environment;
import net.praqma.clearcase.exceptions.ClearCaseException;
import net.praqma.clearcase.exceptions.CleartoolException;
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
	
	public ClearCaseRule( String name ) {
		this.baseName = name;
		this.uniqueBaseName = name + "_" + Environment.getUniqueTimestamp();
	}
	
	public ClearCaseRule( String name, String setupFile ) {
		this.baseName = name;
		this.uniqueBaseName = name + "_" + Environment.getUniqueTimestamp();
		this.setupFile = new File( Environment.class.getClassLoader().getResource( setupFile ).getFile() );
	}
	
	public String getVobName() {
		return vobName;
	}
	
	public String getUniqueName() {
		return uniqueName;
	}

	protected void before( String name ) throws Exception {
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

	protected void after() {
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
            logger.fine( "== Saving environment ==" );
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
