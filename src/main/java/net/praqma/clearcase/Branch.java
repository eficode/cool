package net.praqma.clearcase;

/**
 * @author cwolfgang
 */
public class Branch extends Type {

    private String branchPath;

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
