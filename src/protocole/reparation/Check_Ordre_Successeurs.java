package protocole.reparation;

import chord.Pair;

public class Check_Ordre_Successeurs implements Runnable {

	private Pair pair;
	
	public Check_Ordre_Successeurs(Pair p) {
		// TODO Auto-generated constructor stub
		this.setPair(p);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	public Pair getPair() {
		return pair;
	}

	public void setPair(Pair pair) {
		this.pair = pair;
	}

}
