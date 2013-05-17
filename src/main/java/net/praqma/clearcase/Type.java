package net.praqma.clearcase;

/**
 * @author cwolfgang
 */
public abstract class Type {
    protected String name;

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
