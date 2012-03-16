package net.praqma.clearcase.test;

import net.praqma.clearcase.annotations.TestConfiguration;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;

import org.junit.Test;

// http://publib.boulder.ibm.com/infocenter/cchelp/v7r0m0/index.jsp?topic=/com.ibm.rational.clearcase.cc_ref.doc/topics/ct_rmproject.htm

@TestConfiguration( pvob = "TESTING_PVOB" )
public class Testing extends CoolTestCase {

	@Test
	public void testBasic() throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException {
		bootStrap();
		Project project = Project.create( "testProject", null, pvob, 0, "Test", modelComponent, clientComponent );
		Stream.createIntegration( "test_int", project, structure );
		assertTrue( true );
	}

}
