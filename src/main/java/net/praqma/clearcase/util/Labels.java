package net.praqma.clearcase.util;

import net.praqma.clearcase.Branch;
import net.praqma.clearcase.Label;
import net.praqma.clearcase.api.ListVersionTree;
import net.praqma.clearcase.container.LabelsForVersion;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;

/**
 * @author cwolfgang
 */
public class Labels {

    private static Logger logger = Logger.getLogger( Labels.class.getName() );

    private Labels() {

    }

    public static List<LabelsForVersion> getLabels( File pathname, Branch branch ) throws CleartoolException, UnableToInitializeEntityException {
        return getLabels( pathname.toString() + "@@", branch );
    }

    public static List<LabelsForVersion> getLabels( String pathname, Branch branch ) throws CleartoolException, UnableToInitializeEntityException {
        logger.fine( "Get labels from " + pathname );

        List<String> lines = new ListVersionTree().addPathName( pathname.toString() ).execute();

        List<LabelsForVersion> labels = new ArrayList<LabelsForVersion>( lines.size() );

        for( String line : lines ) {
            /* Determine if there is a label */
            Matcher m = Label.rx.matcher( line );
            if( m.find() ) {
                Version v = Version.get( m.group( 1 ) );

                if( branch == null || branch.equals( v.getUltimateBranch() ) ) {
                    labels.add( new LabelsForVersion( v ).addLabels( Label.getLabels( m.group( 2 ) ) ) );
                }
            }
        }

        return labels;
    }
}
