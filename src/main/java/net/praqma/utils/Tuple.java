package net.praqma.utils;

public class Tuple<T1, T2>
{
	public T1 t1 = null;
	public T2 t2 = null;
	
	public Tuple( T1 t1, T2 t2 )
	{
		this.t1 = t1;
		this.t2 = t2;
	}
	
	public Tuple(){}
	
	public String toString()
	{
		return "(" + t1.toString() + ", " + t2.toString() + ")";
	}
}
