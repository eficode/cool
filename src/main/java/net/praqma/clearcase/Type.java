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
}
