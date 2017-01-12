package protocole;

import java.net.Socket;

import Main.Pair;

/**
 * Classe permettant au Pair de réparer ces pairs successeurs.
 * 
 * @author Barnini Nicholas
 *
 */
public class Reparation implements Runnable {

	/**
	 * Lien vers le pair pour récupérer les successeurs par exemple
	 */
	private Pair pair;

	public Reparation(Pair pair) {
		this.setPair(pair);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			try {

				// Attendre un certain temps entre chaque test
				Thread.sleep(600);
				
				// On parcours la liste de successeurs
				for (Socket socketSuccesseur : this.getPair().getListeSuccesseurs()) {
					
					// On lance le thread dédié
					Check_Connexion cc = new Check_Connexion(socketSuccesseur);
					Thread t = new Thread(cc);
					t.start();
					
				}

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Pair getPair() {
		return pair;
	}

	public void setPair(Pair pair) {
		this.pair = pair;
	}

	// public static void main(String[] args) {
	// for (int i = 0; i<10; i++){
	// Thread t = new Thread(new test_threads());
	// t.start();
	// }
	//
	// for (int i = 0; i< 100; i++){
	// System.out.println(i);
	// }
	// }

}
