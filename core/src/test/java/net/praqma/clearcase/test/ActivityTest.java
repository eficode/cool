package net.praqma.clearcase.test;

import java.io.IOException;
import java.util.List;

import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.util.io.IO;

import org.junit.Test;

import junit.framework.TestCase;

public class ActivityTest extends TestCase {
	@Test
	public void testParser() throws IOException, UnableToLoadEntityException, UCMEntityNotFoundException, UnableToInitializeEntityException {
		List<String> acts = IO.streamToStrings( ActivityTest.class.getClassLoader().getResourceAsStream( "activity.txt" ) );
		
		System.out.println( Activity.parseActivityStrings( acts, 0 ) );
	}
}
