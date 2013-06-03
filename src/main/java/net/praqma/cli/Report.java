package net.praqma.cli;

import net.praqma.clearcase.Branch;
import net.praqma.clearcase.Find;
import net.praqma.clearcase.command.ListType;
import net.praqma.clearcase.container.LabelsForVersion;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.util.Labels;
import net.praqma.util.option.Option;
import net.praqma.util.option.Options;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author cwolfgang
 */
public class Report extends CLI {
    private static Logger logger = Logger.getLogger( Report.class.getName() );

    public static void main( String[] args ) throws Exception {
        Report s = new Report();
        s.perform( args );
    }

    private String sep = "; ";
    private File path;
    private int lengthOfPath = 0;
    private boolean showFullPath = false;

    private DateFormat dateFormatter;

    @Override
    public void perform( String[] arguments ) throws Exception {
        Options o = new Options( "1.0.0" );

        Option opath = new Option( "path", "p", false, 1, "The path to report" );
        Option osep = new Option( "separator", "s", false, 1, "The separator to use, default is \";\"" );
        Option oshowfull = new Option( "showFull", "f", false, 0, "Show full view path" );
        Option odateFormat = new Option( "dateFormat", "d", false, 1, "Date format, default is \"yyyy.MM.dd\"" );
        Option oOutput = new Option( "outputFile", "o", false, 1, "Output the result to the specified file, otherwise dump it to the console." );


        o.setOption( opath );
        o.setOption( osep );
        o.setOption( oshowfull );
        o.setOption( odateFormat );
        o.setOption( oOutput );

        o.setDefaultOptions();

        o.parse( arguments );

        try {
            o.checkOptions();
        } catch( Exception e ) {
            logger.severe( "Incorrect option: " + e.getMessage() );
            o.display();
            System.exit( 1 );
        }

        if( osep.isUsed() ) {
            sep = osep.getString() + " ";
        }

        if( oshowfull.isUsed() ) {
            showFullPath = true;
        }

        if( opath.isUsed() ) {
            path = new File( opath.getString() );
        } else {
            path = new File( System.getProperty( "user.dir" ) );
        }
        lengthOfPath = path.getAbsolutePath().length();
        logger.fine( "Path is " + path.getAbsolutePath() + ", " + lengthOfPath );

        if( odateFormat.isUsed()) {
            dateFormatter = new SimpleDateFormat( odateFormat.getString() );
        } else {
            dateFormatter = new SimpleDateFormat( "yyyy.MM.dd" );
        }

        ListType ls = new ListType().setLocal().setBranchType().setViewRoot( path );
        List<Branch> branches = ls.list();

        Map<File, Entry> map = new HashMap<File, Entry>();

        for( Branch branch : branches ) {
            findBranch( path, branch, map );
        }

        if( oOutput.isUsed() ) {
            File outputFile = new File( oOutput.getString() );
            if( outputFile.exists() ) {
                FileUtils.forceDelete( outputFile );
            }
            dump( new PrintStream( outputFile ), map );
        } else {
            dump( System.out, map );
        }
    }

    public <K, V> void dump( PrintStream out, Map<K, V> map ) {
        /* Build header */
        StringBuilder b = new StringBuilder(  ).
                append( "File" ).append( sep ).
                append( "Age in hours" ).append( sep ).
                append( "Type" ).append( sep ).
                append( "Last user" ).append( sep ).
                append( "Branch name" ).append( sep ).
                append( "Number of versions on branch" ).append( sep ).
                append( "Labeled versions" ).append( sep ).
                append( "Date" ).append( sep ).
                append( "Branches" ); // case 9236

        out.println( b.toString() );

        for( K key : map.keySet() ) {
            out.print( map.get( key ).toString() );

            /* Add the branches */
            //out.println( branches.get( key ).toString() );
            out.println( printBranches( branches.get( key ) ) );
        }
    }

    private String getVersionNumbers( List<Integer> ints ) {
        if( ints == null || ints.isEmpty() ) {
            return "";
        }

        StringBuilder sb = new StringBuilder(  );

        int i = 0;
        for( ; i < ints.size() - 1 ; ++i ) {
            int version = ints.get( i );
            sb.append( version ).append( ", " );
        }

        sb.append( ints.get( i ) );

        return sb.toString();
    }

    private String printBranches( List<Branch> branches ) {

        if( branches.size() == 0 ) {
            return "NONE";
        }

        StringBuilder b = new StringBuilder(  );

        boolean printMain = branches.size() > 1 ? false : true;

        int i = 0;
        for( ; i < branches.size() ; ++i ) {
            Branch branch = branches.get( i );
            boolean isMain = branch.getName().equals( "main" );
            if( !isMain || ( isMain && printMain ) ) {
                b.append( branch.getName() );

                if( i < branches.size() - 2 ) {
                    b.append( ", " );
                }
            }
        }

        return b.toString();
    }

    /**
     * Track the branches containing a given element.
      */
    private Map<File, List<Branch>> branches = new HashMap<File, List<Branch>>();

    private void findBranch( File path, Branch branch, Map<File, Entry> map ) throws Exception {
        logger.info( "Processing " + branch );

        Find find = new Find().addPathName( "." ).setFindAll().print().setViewRoot( path ).setVersionQuery( "version(.../" + branch.getName() + "/LATEST)" );

        List<Version> versions = null;
        try {
            versions = find.find();
        } catch( Exception e ) {
            File[] fs = path.listFiles();
            for( File f : fs ) {
                logger.info( f.toString() );
            }

            throw e;
        }

        //List<Entry> rows = new ArrayList<Entry>( versions.size() );

        for( Version v : versions ) {
            StringBuilder sb = new StringBuilder();
            File file = v.getFile();

            Date now = new Date();
            long secs = now.getTime();

            logger.fine( "Version: " + v );
            logger.fine( "Version: " + v.getDate() );
            logger.fine( "Version: " + v.getRevision() );

            /* Get file */
            if( showFullPath ) {
                sb.append( "\"" + v.getFile().getAbsolutePath() + "\"" ).append( sep ); // Name
            } else {
                sb.append( "\"" + v.getFile().getAbsolutePath().substring( lengthOfPath ) + "\"" ).append( sep ); // Name
            }

            /* Get age */
            long age = secs - v.getDate().getTime();
            sb.append( age / ( 1000 * 60 * 60 ) ).append( sep ); // Age

            /* Get type */
            sb.append( v.isDirectory() ? "directory" : "file" ).append( sep ); // Absolute file

            /* Get user */
            sb.append( v.getUser() ).append( sep ); // The user

            /* Get branch name */
            sb.append( branch.getName() ).append( sep );

            /* The number of versions on the branch */
            sb.append( v.getRevision() ).append( sep );

            /* TODO Labeled versions, case 9223 */
            sb.append( getVersionNumbers( compileLabeledVersions( v.getQualifiedFilename(), branch ) ) ).append( sep );

            /* Get date */
            sb.append( dateFormatter.format( v.getDate() ) ).append( sep );

            /* Put to map */
            if( map.containsKey( file ) ) {
                logger.fine( "Ages " + age + "<" + map.get( file ).age );
                if( map.get( file ).age > age ) {
                    logger.fine( "Replaced!" );
                    map.put( file, new Entry( sb.toString(), age ) );
                }
            } else {
                map.put( file, new Entry( sb.toString(), age ) );
            }

            /**/
            if( branches.containsKey( file ) ) {
                List<Branch> b = branches.get( file );
                b.add( branch );
            } else {
                List<Branch> bs = new ArrayList<Branch>(  );
                bs.add( branch );
                branches.put( file, bs );
            }
        }
    }

    private List<Integer> compileLabeledVersions( String pathname, Branch branch ) throws UnableToInitializeEntityException, CleartoolException {
        List<LabelsForVersion> lfvs = Labels.getLabels( pathname, branch );

        List<Integer> versionNumbers = new ArrayList<Integer>( lfvs.size() );

        for( LabelsForVersion lfv : lfvs ) {
            if( branch.equals( lfv.getBranch() ) ) {
                versionNumbers.add( lfv.getRevision() );
            }
        }

        return versionNumbers;
    }

    public class Entry {
        String string;
        long age;
        public Entry( String string, long age ) {
            this.string = string;
            this.age = age;
        }

        @Override
        public String toString() {
            return string;
        }
    }
}
