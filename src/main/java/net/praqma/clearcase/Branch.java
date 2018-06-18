package net.praqma.clearcase;

import edu.umd.cs.findbugs.annotations.*;

/**
 * @author cwolfgang
 */
@SuppressFBWarnings("")
public class Branch extends Type {

    private String branchPath;

    public Branch() {
    }

    public Branch( String name ) {
        super( name );
    }

    public static Branch getBranchFromPath( String branchPath ) {
        String name = getBranchName( branchPath );
        Branch branch = new Branch( name );
        branch.branchPath = branchPath;

        return branch;
    }

    public String getBranchPath() {
        return branchPath;
    }

    /**
     * Get the name of the {@link Branch} from a branch path.
     * @param branchPath The relative path to the branch
     * @return The branch name
     */
    public static String getBranchName( String branchPath ) {
        String[] bs = branchPath.split( Cool.qfs );

        if( bs != null && bs.length > 0 ) {
            return bs[bs.length-1];
        } else {
            throw new IllegalArgumentException( "The branch path \"" + branchPath + "\" is not valid" );
        }
    }

    @Override
    public String toString() {
        return "Branch " + name;
    }
}
