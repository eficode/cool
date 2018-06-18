package net.praqma.clearcase;

import edu.umd.cs.findbugs.annotations.*;

import java.io.Serializable;

/**
 * @author cwolfgang
 */
@SuppressFBWarnings("")
public abstract class Type implements Serializable {
    protected String name;

    protected Type() {
        name = "NA";
    }

    public Type( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals( Object o ) {
        if( o == this ) {
            return true;
        }

        if( o instanceof Type ) {
            Type other = (Type) o;
            return other.getName().equals( this.name );
        } else {
            return false;
        }
    }
}
