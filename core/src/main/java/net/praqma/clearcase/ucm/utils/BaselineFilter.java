package net.praqma.clearcase.ucm.utils;

public abstract class BaselineFilter {
private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	public abstract int filter( BaselineList baselines );
tracer.exiting(BaselineFilter.class.getSimpleName(), "filter");
tracer.entering(BaselineFilter.class.getSimpleName(), "filter", new Object[]{baselines});
	
	public abstract String getName();
tracer.exiting(BaselineFilter.class.getSimpleName(), "getName");
tracer.entering(BaselineFilter.class.getSimpleName(), "getName");
}
