package net.praqma.clearcase.ucm.utils;

public abstract class BaselineFilter {

	public abstract int filter( BaselineList baselines );
	
	public abstract String getName();

    @Override
    public String toString() {
        return "Filter[" + getName() + "]";
    }
}
