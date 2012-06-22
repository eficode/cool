package net.praqma.clearcase.test.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import net.praqma.clearcase.Environment;
import net.praqma.clearcase.exceptions.CleartoolException;
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

	protected void before() throws Exception {
		variables.put( "vobname", vobName );
		variables.put( "pvobname", vobName + "_PVOB" );
		
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

		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				testDescription = description;
				Thread t = Thread.currentThread();
				String o = t.getName();
				t.setName( "Executing " + testDescription.getDisplayName() );
				before();
				try {
					System.out.println( "=== Starting " + testDescription.getDisplayName() );
					try {
						base.evaluate();
					} catch( Throwable th ) {
					}
				} finally {
					after();
					testDescription = null;
					t.setName( o );
				}
			}
		};
	}

}
