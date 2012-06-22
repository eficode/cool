package net.praqma.clearcase.test.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.praqma.clearcase.Environment;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.test.ClearCaseTest;
import net.praqma.clearcase.util.ExceptionUtils;
import net.praqma.clearcase.util.SetupUtils;

public class ClearCaseRule extends Environment implements TestRule {

	protected Description testDescription;
	
	protected String name;
	protected String vobName;
	
	public ClearCaseRule( String name, String vobName ) {
		this.name = name;
		this.vobName = vobName;
	}
	
	public String getVobName() {
		return vobName;
	}

	protected void before( String name ) throws Exception {
		variables.put( "vobname", name );
		variables.put( "pvobname", name + "_PVOB" );
		
		this.vobName = name;
		
		bootStrap();
	}

	protected void after() {
		if( pvob != null ) {
			try {
				SetupUtils.tearDown( pvob );
			} catch( CleartoolException e ) {
				ExceptionUtils.print( e, System.out, true );
			}
		} else {
			/* Not possible to tear down */
		}
	}

	@Override
	public Statement apply( final Statement base, final Description description ) {
		
		if( description.getAnnotation( ClearCaseTest.class ) == null ) {
			return base;
		}
		
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				testDescription = description;
				Thread t = Thread.currentThread();
				String o = t.getName();
				t.setName( "Executing " + testDescription.getDisplayName() );
				System.out.println( " ===== Setting up ClearCase =====" );
				before( vobName + description.getAnnotation( ClearCaseTest.class ).name() );
				try {
					System.out.println( " ===== Running test: " + testDescription.getDisplayName() + " =====" );
					base.evaluate();
				} finally {
					System.out.println( " ===== Tearing down up ClearCase =====" );
					after();
					testDescription = null;
					t.setName( o );
				}
			}
		};
	}

}
