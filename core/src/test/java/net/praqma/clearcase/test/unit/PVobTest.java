package net.praqma.clearcase.test.unit;

import static org.junit.Assert.*;

import java.util.regex.Matcher;

import net.praqma.clearcase.Cool;
import net.praqma.clearcase.PVob;
import net.praqma.clearcase.Vob;
import net.praqma.util.execute.CommandLineInterface.OperatingSystem;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;

public class PVobTest {

	@Test
	public void naming() {
		String windowsName = "\\mypvob";
		String unixName1   = "/mypvob";
		String unixName2   = "/vobs/mypvob";
		
		assertTrue( Vob.isValidTag( windowsName ) );
		assertTrue( Vob.isValidTag( unixName1 ) );
		assertTrue( Vob.isValidTag( unixName2 ) );
	}
	/*
	@Test
	public void namingFindVob() {
		String windowsName = "\\mypvob\\comp1";
		String unixName1   = "/mypvob/comp1";
		String unixName2   = "/vobs/mypvob/comp1";
		
		System.out.println( "RX: " + PVob.rx_find_vob );
		
		if( Cool.getOS().equals( OperatingSystem.UNIX ) ) {
			Matcher m = PVob.rx_find_vob.matcher( unixName1 );
			
			if( m.find() ) {
				assertThat( m.group( 1 ), is( "/mypvob" ) );
			} else {
				fail();
			}
			
			m = PVob.rx_find_vob.matcher( unixName2 );
			
			if( m.find() ) {
				assertThat( m.group( 1 ), is( "/vobs/mypvob" ) );
			} else {
				fail();
			}
		} else {
			Matcher m = PVob.rx_find_vob.matcher( windowsName );
			
			if( m.find() ) {
				assertThat( m.group( 1 ), is( "\\mypvob\\comp1" ) );
			} else {
				fail();
			}
		}
	}
	*/
}
