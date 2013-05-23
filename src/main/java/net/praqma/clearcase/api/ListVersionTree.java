package net.praqma.clearcase.api;

import net.praqma.clearcase.Branch;
import net.praqma.clearcase.Label;
import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.util.execute.CmdResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cwolfgang
 */
public class ListVersionTree extends Command<List<String>> {

    /**
     * Omits all subbranches from the listing, showing only versions on a single branch.
     */
    private boolean nrecurse = false;

    /**
     * Includes all versions that are at the from-end of one or more merge arrows (hyperlinks of type Merge).
     * Annotations on each such version indicate the corresponding to-objects.
     */
    private boolean merge = false;

    /**
     * Excludes checked-out versions from the listing or display.
     * The predecessor of a checked-out version is also excluded, unless there is another reason to include it (for example, it has a version label).
     */
    private boolean nco = false;

    /**
     * Restricts the listing to version-extended pathnames. Version labels, merge annotations, and checkout annotations are omitted.
     */
    private boolean shortFlag = false;

    /**
     * Lists all versions on a branch, not the selected versions only;
     * annotates each version with all of its version labels.
     */
    private boolean all = false;

    /**
     * Lists obsolete branches and instances of obsolete branch types.
     */
    private boolean obsolete = false;

    /**
     * Starts the version tree listing at the specified branch.
     * You can also use an extended name as the pname argument (for example, foo.c@@\main\bug405) to start the listing at a particular branch.
     */
    private Branch branch;

    /**
     * One or more pathnames, specifying elements or branches of elements.
     * (Alternatively, use the –branch option to specify a branch of an element.)
     */
    private List<String> pathNames = new ArrayList<String>(  );

    public ListVersionTree addPathName( String pathName ) {
        pathNames.add( pathName );

        return this;
    }

    @Override
    public List<String> execute() throws CleartoolException {
        CmdResult result = runCommand();

        return result.stdoutList;
    }

    @Override
    public String getCommand() {
        return "lsvtree";
    }

    @Override
    public String getCommandLine() {

        if( pathNames.isEmpty() ) {
            throw new IllegalArgumentException( "No path names given" );
        }

        StringBuilder sb = new StringBuilder();
        sb.append( "lsvtree" );

        if( nrecurse ) {
            sb.append( " -nrecurse" );
        }

        if( shortFlag ) {
            sb.append( " -short" );
        }

        if( all ) {
            sb.append( " -all" );
        }

        if( merge ) {
            sb.append( " -merge" );
        }

        if( nco ) {
            sb.append( " -nco" );
        }

        if( branch != null ) {
            sb.append( " -branch " ).append( branch.getName() );
        }

        for( String pname : pathNames ) {
            sb.append( " \"" ).append( pname ).append( "\"" );
        }

        return sb.toString();
    }
}
