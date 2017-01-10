package protocole;

import Main.Pair;

/**
 * Classe permettant au Pair de réparer ces pairs successeurs.
 * 
 * @author Barnini Nicholas
 *
 */
public class Reparation implements Runnable {

	private Pair pair;
	
	public Reparation (Pair pair){
		this.setPair(pair);
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
