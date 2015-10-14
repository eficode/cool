package net.praqma.clearcase.exceptions;

import net.praqma.clearcase.ucm.entities.Project.PromotionLevel;
import net.praqma.clearcase.ucm.entities.UCMEntity;

public class UnableToPromoteBaselineException extends ClearCaseException {
	
	private UCMEntity entity;
	private PromotionLevel plevel;
    
    public UnableToPromoteBaselineException( String message, UCMEntity entity, PromotionLevel plevel) {
        super(message);
        this.entity = entity;
        this.plevel = plevel;
    }    
	
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