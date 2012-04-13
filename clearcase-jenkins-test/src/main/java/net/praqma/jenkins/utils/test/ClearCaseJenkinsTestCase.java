package net.praqma.jenkins.utils.test;

import net.praqma.clearcase.test.junit.CoolTestCase;
import net.praqma.util.JarUtils;

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
		super.tearDown();
	}
}
