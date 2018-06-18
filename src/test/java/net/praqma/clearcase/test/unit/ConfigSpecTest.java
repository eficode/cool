package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.*;
import net.praqma.clearcase.exceptions.*;
import org.apache.commons.io.*;
import org.junit.Test;
import org.mockito.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;

/**
 * @author cwolfgang
 */
public class ConfigSpecTest {

    private static Logger logger = Logger.getLogger( ConfigSpec.class.getName() );

    @Test
    public void noNewLoadRules() throws IOException, CleartoolException {
        File file = new File( URLDecoder.decode( this.getClass().getResource( "cs.txt" ).getFile(), "UTF-8" ) );
        List<String> csList = FileUtils.readLines( file );

        ConfigSpec cs = new ConfigSpec( null );
        ConfigSpec spy = Mockito.spy( cs );
        Mockito.doReturn( csList ).when( spy ).catcs();
        spy.generate();

        List<String> newCSList = FileUtils.readLines( spy.getTemporaryConfigSpecFile() );

        assertThat( newCSList.size(), is( csList.size() ) );
    }

    @Test
    public void oldConfigSpec() throws IOException, CleartoolException {
        File file = new File( URLDecoder.decode( this.getClass().getResource( "cs.txt" ).getFile(), "UTF-8" ) );
        List<String> csList = FileUtils.readLines( file );

        ConfigSpec cs = new ConfigSpec( null );
        ConfigSpec spy = Mockito.spy( cs );
        Mockito.doReturn( csList ).when( spy ).catcs();
        spy.generate();

        List<String> newCSList = FileUtils.readLines( spy.getTemporaryConfigSpecFile() );

        assertThat( spy.getCurrentLoadRules().size(), is( 2 ) );
        assertTrue( spy.getCurrentLoadRules().contains( "\\crot\\Model" ) );
        assertTrue( spy.getCurrentLoadRules().contains( "\\crot\\Clientapp" ) );
    }

    @Test
    public void newLoadRules() throws IOException, CleartoolException {
        File file = new File( URLDecoder.decode( this.getClass().getResource( "cs.txt" ).getFile(), "UTF-8" ) );
        List<String> csList = FileUtils.readLines( file );

        ConfigSpec cs = new ConfigSpec( null );
        ConfigSpec spy = Mockito.spy( cs );
        spy.addLoadRule( "\\crot\\Service" );
        Mockito.doReturn( csList ).when( spy ).catcs();
        spy.generate();

        List<String> newCSList = FileUtils.readLines( spy.getTemporaryConfigSpecFile() );

        assertThat( newCSList.size(), is( csList.size() ) );
        assertThat( spy.getLoadRules().size(), is( 1 ) );

        assertTrue( spy.getLoadRules().contains( "\\crot\\Service" ) );
    }

    @Test( expected = IllegalStateException.class )
    public void apply() throws IOException, CleartoolException {
        File file = new File( URLDecoder.decode( this.getClass().getResource( "cs.txt" ).getFile(), "UTF-8" ) );
        List<String> csList = FileUtils.readLines( file );

        ConfigSpec cs = new ConfigSpec( null );
        cs.appy();
    }

}
