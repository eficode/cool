package net.praqma.clearcase.api;

import net.praqma.clearcase.exceptions.CleartoolException;
import net.praqma.util.execute.CmdResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cwolfgang
 */
public class ListVob extends Command<List<String>> {

    public enum ReportLength {
        /**
         *     Restricts the listing of each entry to its version-extended pathname only.
         */
        Short( "short" ),

        /**
         * For each object, lists the config spec rule that matches the object and classifies each object.
         * The classification can be one of:
         * version, directory version, file element, directory element, view-private object, derived object, derived object version, or symbolic link.
         * For each derived object, ls –long indicates whether the DO is nonshareable, unshared, promoted, or shared.
         */
        Long( "long" );

        private String value;

        private ReportLength( String value ) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }


        @Override
        public String toString() {
            return "-" + value;
        }
    }

    public enum Restriction {
        /**
         * Restricts the listing to objects in the VOB storage, including versions of elements and VOB links.
         * This may also add some entries to the listing: those for the underlying elements that are eclipsed by checked-out versions.
         */
        vob_only,

        /**
         * Restricts the listing to objects that belong logically to the view:
         * view-private files, view-private directories, and view-private links;
         * checked-out versions; and all derived objects visible in the view.
         */
        view_only;

        @Override
        public String toString() {
            return "-" + name();
        }


    }

    /**
     * Includes a listing of the entire subtree below any subdirectory included in the top-level listing.
     * VOB symbolic links are not traversed during the recursive descent.
     */
    private boolean recurse = false;

    /**
     *
     */
    private ReportLength length;

    /**
     *
     */
    private Restriction restriction;

    /**
     * Restricts the listing to objects visible to the operating system listing command.
     */
    private boolean visible;

    /**
     * Restricts the listing to the specified files, directories, and/or links.
     * pname may be a view- or VOB-extended pathname to list objects that are not in the view,
     * regardless of whether the view is a snapshot view or a dynamic view (see pathnames_ccase).
     */
    private List<String> pathNames = new ArrayList<String>(  );

    /**
     * Add a path name.
     */
    public ListVob addPathName( String pathName ) {
        this.pathNames.add( pathName );

        return this;
    }

    public ListVob recurse() {
        this.recurse = true;

        return this;
    }

    public ListVob shortReportLength() {
        this.length = ReportLength.Short;

        return this;
    }

    public ListVob restrictToViewOnly() {
        this.restriction = Restriction.view_only;

        return this;
    }

    public ListVob setViewRoot( File path ) {
        this.root = path;

        return this;
    }

    @Override
    public List<String> execute() throws CleartoolException {
        CmdResult result = runCommand();

        return result.stdoutList;

    }

    @Override
    public String getCommandLine() {
        StringBuilder b = new StringBuilder(  );

        b.append( "ls" );

        if( length != null ) {
            b.append( " " ).append( length );
        }

        if( recurse ) {
            b.append( " -recurse" );
        }

        if( restriction != null ) {
            b.append( " " ).append( restriction );
        }

        for( String pname : pathNames ) {
            b.append( " \"" ).append( pname ).append( "\"" );
        }

        return b.toString();
    }

    @Override
    public String getCommand() {
        return "ls";
    }
}
