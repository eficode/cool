package net.praqma.clearcase.ucm.utils;

import java.util.ArrayList;

import net.praqma.clearcase.ucm.UCMException;
import net.praqma.clearcase.ucm.entities.Activity;
import net.praqma.clearcase.ucm.entities.Baseline;
import net.praqma.clearcase.ucm.entities.UCM;
import net.praqma.clearcase.ucm.entities.Version;
import net.praqma.clearcase.ucm.view.SnapshotView;

public class BaselineDiff extends ArrayList<Activity>
{
	
	public BaselineDiff()
	{
	}
	
	public BaselineDiff( SnapshotView view, Baseline baseline ) throws UCMException
	{
		this.addAll( UCM.context.GetBaselineDiff( view, baseline ) );
	}
	
	public void Print() throws UCMException
	{
		for( Activity a : this )
		{
			System.out.println( "----- Activity:" );
			System.out.println( a.Stringify() );
			for( Version v : a.changeset.versions )
			{
				//System.out.println( v.Stringify() );
				System.out.println( v.toString() );
			}
		}
	}
	
	public ArrayList<Version> GetUniqueFiles()
	{
		return null;
	}
}
