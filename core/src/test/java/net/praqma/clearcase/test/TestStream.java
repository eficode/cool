package net.praqma.clearcase.test;

import java.io.IOException;

import org.junit.Test;

import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToCreateEntityException;
import net.praqma.clearcase.exceptions.UnableToGetEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.exceptions.ViewException;
import net.praqma.clearcase.test.junit.CoolTestCase;

public class TestStream extends CoolTestCase {

	@Test
	public void testFoundationBaselines() throws UnableToCreateEntityException, UCMEntityNotFoundException, UnableToGetEntityException, CleartoolException, ViewException, UnableToLoadEntityException, IOException {
		bootStrap( "testProject", "test_int" );
		
		integrationStream.load();
		System.out.println( "Foundation baselines:" + integrationStream.getFoundationBaselines() );

		assertTrue( true );
	}

}
