package net.praqma.clearcase.test.unit;

import net.praqma.clearcase.ConfigSpec;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.logging.PraqmaticLogFormatter;
import net.praqma.util.test.junit.LoggingRule;
import org.apache.commons.io.FileUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.URLDecoder;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author cwolfgang
 */
public class ConfigSpecTest {

    private static Logger logger = Logger.getLogger( ConfigSpec.class.getName() );

    @ClassRule
    public static LoggingRule lrule = new LoggingRule( "net.praqma" ).setFormat( PraqmaticLogFormatter.TINY_FORMAT );

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
