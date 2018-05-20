
public class Pair<T1,T2>{

	public T1 x;
	public T2 y;
	
	public Pair (T1 x, T2 y){
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString(){
		return "("+x.toString()+","+y.toString()+")";
	}
	
	@Override
	public boolean equals(Object o){
		
		if(this.getClass().equals(o.getClass())){
			@SuppressWarnings("unchecked")
			Pair<T1,T2> p = (Pair<T1,T2>) o;
			return p.x == x && p.y == y; 
		}		
		return false;
	}
	
	@Override
	public int hashCode(){
		return x.hashCode() ^ y.hashCode();
	}
}
