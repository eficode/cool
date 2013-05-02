package net.praqma.clearcase;

import net.praqma.clearcase.cleartool.Cleartool;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.util.execute.AbnormalProcessTerminationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class ConfigSpec {

    private static Logger logger = Logger.getLogger( ConfigSpec.class.getName() );

    private static final String UCMCustomLoadBegin = "#UCMCustomLoadBegin - DO NOT REMOVE - ADD CUSTOM LOAD RULES AFTER THIS LINE";

    private List<String> loadRules = new ArrayList<String>();
    private List<String> currentLoadRules = null;

    private File viewRoot;
    private File temporaryCSFile;

    public ConfigSpec( File viewRoot ) {
        this.viewRoot = viewRoot;
    }

    public ConfigSpec addLoadRule( Component component ) throws CleartoolException {
        loadRules.add( component.getRootDir() );

        return this;
    }

    public ConfigSpec addLoadRule( String loadRule ) throws CleartoolException {
        loadRules.add( loadRule );

        return this;
    }

    public List<String> getLoadRules() {
        return loadRules;
    }

    public List<String> getCurrentLoadRules() throws CleartoolException {
        if( currentLoadRules == null ) {
            loadCurrentLoadRules();
        }

        return currentLoadRules;
    }

    public File getTemporaryConfigSpecFile() {
        return temporaryCSFile;
    }

    public ConfigSpec addLoadRulesFromBaseline( Baseline baseline ) throws CleartoolException {
        addLoadRule( baseline.getComponent() );

        return this;
    }

    public ConfigSpec addLoadRulesFromBaselines( List<Baseline> baselines ) throws CleartoolException {
        for( Baseline b : baselines ) {
            addLoadRule( b.getComponent() );
        }

        return this;
    }

    /**
     * Generate the new config spec
     */
    public ConfigSpec generate() throws CleartoolException, IOException {
        List<String> csLines = catcs();

        /* Remove custom load rules */
        removeLoadRules( csLines );

        /* Add new custom load rules */
        logger.fine( "Add new custom load rules" );
        logger.fine( "Load rules: " + loadRules );
        csLines.addAll( addLoad( loadRules ) );

        temporaryCSFile = File.createTempFile( "ClearCase", "configspec" );
        FileUtils.writeLines( temporaryCSFile, csLines );

        return this;
    }

    private List<String> addLoad( List<String> rules ) {
        List<String> rs = new ArrayList<String>( rules.size() );
        for( String r :rules ) {
            rs.add( "load " + r );
        }

        return rs;
    }

    /**
     * Apply the generated config spec
     */
    public ConfigSpec appy() throws CleartoolException {
        if( temporaryCSFile == null || !temporaryCSFile.exists() ) {
            throw new IllegalStateException( "New config spec not generated yet" );
        }

        String cmd = "setcs -force " + temporaryCSFile.getAbsolutePath();
        try {
            Cleartool.run( cmd, viewRoot );
        } catch( AbnormalProcessTerminationException e ) {
            try {
                logger.severe( FileUtils.readFileToString( temporaryCSFile ) );
            } catch( IOException e1 ) {
                logger.severe( "Unable to dump config spec, " + e1.getMessage() );
            }
            throw new CleartoolException( "Unable to set the config spec", e );
        }

        return this;
    }

    private void loadCurrentLoadRules() throws CleartoolException {
        logger.fine( "Loading current load rules" );
        currentLoadRules = new ArrayList<String>();

        List<String> csLines = catcs();

        logger.fine( "Getting custom load rules" );
        boolean after = false;
        Iterator<String> it = csLines.iterator();
        while( it.hasNext() ) {
            String next = it.next();
            if( next.equals( UCMCustomLoadBegin ) ) {
                logger.fine( "Found BEGIN LINE" );
                after = true;
                continue;
            }

            if( after && next.startsWith( "load" ) ) {
                currentLoadRules.add( next.substring( 5 ) );
            }
        }
    }

    /**
     * Remove custom load rules from config specs IN-PLACE!!!!
     * @return
     */
    private void removeLoadRules( List<String> configSpec ) {
        logger.fine( "Remove custom load rules" );

        currentLoadRules = new ArrayList<String>();

        boolean remove = false;
        Iterator<String> it = configSpec.iterator();
        while( it.hasNext() ) {
            String next = it.next();
            if( next.equals( UCMCustomLoadBegin ) ) {
                logger.fine( "Found BEGIN LINE" );
                remove = true;
                continue;
            }

            if( remove && next.startsWith( "load" ) ) {
                logger.fine( "Removing " + next );
                currentLoadRules.add( next.substring( 5 ) );
                it.remove();
            }
        }
    }

    public static String getLoadRules( File viewRoot ) {

        return null;
    }

    public List<String> catcs() throws CleartoolException {
        String cmd = "catcs";
        try {
            return Cleartool.run( cmd, viewRoot ).stdoutList;
        } catch( AbnormalProcessTerminationException e ) {
            throw new CleartoolException( "Unable to get the config spec", e );
        }
    }
}
