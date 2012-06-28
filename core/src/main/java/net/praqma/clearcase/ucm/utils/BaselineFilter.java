package net.praqma.clearcase.ucm.utils;

public abstract class BaselineFilter {

	public abstract int filter( Baselines2 baselines );
	
	public abstract String getName();
}
