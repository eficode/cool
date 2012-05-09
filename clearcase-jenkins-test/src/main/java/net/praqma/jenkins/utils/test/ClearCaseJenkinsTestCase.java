package net.praqma.jenkins.utils.test;

import java.io.IOException;
import net.praqma.clearcase.test.junit.CoolTestCase;

import org.apache.commons.io.FileUtils;
import org.jvnet.hudson.test.HudsonTestCase;

public class ClearCaseJenkinsTestCase extends HudsonTestCase {
	public CoolTestCase coolTest = new ConcreteCoolTestCase();
	
	public CoolTestCase getCoolTestCase() {
		return coolTest;
	}
	
	public void bootStrap() {
		//JarUtils.getInputStream( jarPath, file )
	}
	
	@Override
	protected void setUp() throws Exception {
		coolTest.setUp();
		super.setUp();
	}
	
	@Override
	public void runTest() throws Throwable {
		if( !coolTest.hasFailed() ) {
			super.runTest();
		}
	}
	
	@Override
	public void tearDown() throws Exception {
		coolTest.tearDown();
		try {
			super.tearDown();
		} catch( Exception e ) {
			System.out.println( "PATH: " + super.jenkins.getRootDir() );
			//FileUtils.deleteDirectory( super.jenkins.getRootDir() );
            try {
                FileUtils.forceDelete(super.jenkins.getRootDir());
            } catch (IOException ioex) {
                System.out.println("Failed to forcibly remove temp directory!");
            }
		}
	}
}
