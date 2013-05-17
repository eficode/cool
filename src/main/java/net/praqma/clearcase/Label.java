package net.praqma.clearcase;

import net.praqma.clearcase.api.ListVersionTree;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Version;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author cwolfgang
 */
public class Label extends Type {

    private static Logger logger = Logger.getLogger( Label.class.getName() );

    public static final Pattern rx = Pattern.compile( "^(.*)\\b\\s*\\((.*?)\\)$" );

    public Label( String name ) {
        super( name );
    }

    public static List<Label> getLabels( File pathname, Branch branch ) throws CleartoolException, UnableToInitializeEntityException {
        logger.fine( "Get labels from " + pathname );

        List<String> lines = new ListVersionTree().addPathName( pathname.toString() ).execute();

        List<Label> labels = new ArrayList<Label>( lines.size() );

        for( String line : lines ) {
            /* Determine if there is a label */
            Matcher m = Label.rx.matcher( line );
            if( m.find() ) {
                Version v = Version.get( m.group( 1 ) );

                if( branch == null || branch.equals( v.getUltimateBranch() ) ) {
                    labels.add( new Label( m.group( 2 ) ) );
                }
            }
        }

        return labels;
    }

    @Override
    public String toString() {
        return "Label " + name;
    }
}
