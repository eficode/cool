package net.praqma.clearcase.ucm.utils;

import java.io.Serializable;

public abstract class BaselineFilter implements Serializable {

	public abstract int filter( BaselineList baselines );
	
	public abstract String getName();

    @Override
    public String toString() {
        return "Filter[" + getName() + "]";
    }
}
