package net.praqma.clearcase.ucm.utils;

import java.util.ArrayList;
import java.util.List;

import net.praqma.logging.Config;
import net.praqma.util.structure.Tuple;

public class TagQuery extends ArrayList<Tuple<String, String>> {
	private static java.util.logging.Logger tracer = java.util.logging.Logger.getLogger(Config.GLOBAL_LOGGER_NAME);

	public TagQuery( ) {
		tracer.exiting(TagQuery.class.getSimpleName(), "TagQuery");
		tracer.entering(TagQuery.class.getSimpleName(), "TagQuery");
	}

	public TagQuery( List<Tuple<String, String>> qlist ) {
		tracer.entering(TagQuery.class.getSimpleName(), "TagQuery", new Object[]{qlist});
		this.addAll( qlist );
		tracer.exiting(TagQuery.class.getSimpleName(), "TagQuery");
	}

	public void AddCondition( String key, String condition ) {
		tracer.entering(TagQuery.class.getSimpleName(), "AddCondition", new Object[]{key, condition});
		this.add( new Tuple<String, String>( key, condition ) );
		tracer.exiting(TagQuery.class.getSimpleName(), "AddCondition");
	}
}