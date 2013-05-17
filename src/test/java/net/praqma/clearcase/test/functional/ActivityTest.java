package net.praqma.clearcase.test.functional;

import net.praqma.clearcase.test.junit.ClearCaseRule;
import net.praqma.clearcase.ucm.entities.Activity;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class ActivityTest {

	@Rule
	public static ClearCaseRule ccenv = new ClearCaseRule( "cool-activitytest" );
	
	@Test
	public void headline() {
		Activity a = ccenv.context.activities.get( "initial_files" );
		assertThat( a.getHeadline(), is( "Adding initial files into components" ) );
	}
}
