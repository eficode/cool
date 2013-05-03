package net.praqma.clearcase;

import java.util.List;

/**
 * @author cwolfgang
 */
public class Branch extends Type {

    public Branch( String name ) {
        super( name );
    }

    @Override
    public String toString() {
        return "Branch " + name;
    }
}
