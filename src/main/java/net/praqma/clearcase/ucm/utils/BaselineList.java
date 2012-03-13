package net.praqma.clearcase.ucm.utils;

import java.util.ArrayList;
import java.util.List;

import net.praqma.clearcase.exceptions.UCMException;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.Component;
import net.praqma.clearcase.ucm.entities.Project;
import net.praqma.clearcase.ucm.entities.Stream;
import net.praqma.clearcase.ucm.entities.Tag;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.util.debug.Logger;

/**
 * 
 * @author wolfgang
 * @deprecated
 */
public class BaselineList extends ArrayList<Baseline> {

	private static final long serialVersionUID = 7674824124243453983L;

	transient private static Logger logger = Logger.getLogger();
	
	private Stream stream = null;
	private Component component = null;

	/* Default constructor */
	public BaselineList() {

	}

	private BaselineList( BaselineList bls ) {
		this.stream = bls.stream;
		this.component = bls.component;
		this.addAll( bls );
	}

	private BaselineList( Component component, Stream stream ) {
		this.stream = stream;
		this.component = component;
	}

	public BaselineList( Component component, Stream stream, Project.PromotionLevel plevel ) throws UCMException {
		logger.debug( "Getting Baselines from " + stream.getFullyQualifiedName() + " and " + component.getFullyQualifiedName() + " with plevel " + plevel );

		this.stream = stream;
		this.component = component;

		this.addAll( UCM.context.getBaselines( stream, component, plevel, component.getPvobString() ) );
	}

	public BaselineList( List<Baseline> bls ) {
		this.addAll( bls );
	}

	public BaselineList Filter( TagQuery tq, String tagType, String tagID ) throws UCMException {
		BaselineList bls = new BaselineList( this );

		for( Baseline b : this ) {
			Tag tag = b.getTag( tagType, tagID );

			logger.debug( "BASELINE=" + b.toString() + ". tag=" + tag.toString() );

			if( tag.queryTag( tq ) ) {
				bls.add( b );
			}
		}

		return bls;
	}

	public BaselineList NewerThanRecommended() throws UCMException {
		BaselineList bls = new BaselineList( this );
		List<Baseline> recommended = this.stream.getRecommendedBaselines();

		if( recommended.size() != 1 ) {
			logger.warning( "Only one baseline can be recommended simultaneously, " + recommended.size() + " found." );
			throw new UCMException( "Only one baseline can be recommended simultaneously, " + recommended.size() + " found." );
		}

		Baseline recbl = recommended.get( 0 );
		logger.debug( "The recommended=" + recbl.toString() );
		logger.debug( "REC COMP=" + recbl.getComponent().getFullyQualifiedName() );
		logger.debug( "THIS COM=" + component.getFullyQualifiedName() );

		if( !recbl.getComponent().getFullyQualifiedName().equals( component.getFullyQualifiedName() ) ) {
			logger.warning( component.getFullyQualifiedName() + " is not represented in " + stream.getFullyQualifiedName() + " Recommended baseline" );
			throw new UCMException( component.getFullyQualifiedName() + " is not represented in " + stream.getFullyQualifiedName() + " Recommended baseline" );
		}

		boolean match = false;
		while( !bls.isEmpty() && !match ) {
			Baseline b = bls.remove( 0 );
			match = b.getFullyQualifiedName().equals( recbl.getFullyQualifiedName() );
			logger.debug( "Matching: " + b.toString() + " == " + recbl.getFullyQualifiedName() );
		}

		return bls;
	}

}