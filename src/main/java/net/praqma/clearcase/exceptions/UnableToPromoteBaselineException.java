package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.UCMEntity;

public class UnableToPromoteBaselineException extends CleartoolException {
	
	private UCMEntity entity;
	private PromotionLevel plevel;
	
	public UnableToPromoteBaselineException( UCMEntity entity, PromotionLevel plevel ) {
		this.entity = entity;
		this.plevel = plevel;
	}
	
	public PromotionLevel getPromotionLevel() {
		return plevel;
	}

	public UCMEntity getEntity() {
		return entity;
	}
	
}