package net.praqma.clearcase.test;

import static org.junit.Assert.*;

import java.io.File;

import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Component;

import org.junit.ClassRule;
import org.junit.Test;

public class ComponentTest {
	
	@ClassRule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-component" );

	@Test
	public void testLoad() throws Exception {
		Component model = ccenv.context.components.get( "Model" ).load();
	}
	
	@Test
	public void testCreate() throws Exception {
		File view = new File( ccenv.context.mvfs + "/" + ccenv.getUniqueName() + "_one_int/" + ccenv.getVobName() );
		Component mycomp = Component.create( "new-component", ccenv.getPVob(), "Praqma", "my comment", view );
		
		assertNotNull( mycomp );
		assertEquals( "new-component", mycomp.getShortname() );
	}
	
	@Test
	public void testGetRootDir() throws Exception {
		Component model = ccenv.context.components.get( "Model" );
		
		String root = new File( ccenv.getVobName() + "/" + "Model" ).getPath();
		System.out.println( "---->ROOT: " + ccenv.getVobName() );
		System.out.println( "---->ROOT: " + root );
		assertEquals( root, model.getRootDir() );
	}
	
	@Test
	public void testGet() throws Exception {
		Component model = Component.get( "Model@\\" + ccenv.getVobName() + "_PVOB" );
		
		assertNotNull( model );
	}


}
