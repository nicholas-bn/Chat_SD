package protocole.reparation;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

import chord.Pair;
import chord.PairInfos;

/**
 * Classe threader qui permet de tester la connexion à un pair distant
 * 
 * @author Barnini
 *
 */
public class Check_Connexion implements Runnable {

	private Pair pair;

	private int positionSuccesseurs;

	public Check_Connexion(int position, Pair pair) {
		// TODO Auto-generated constructor stub
		this.setPair(pair);
		this.setPositionSuccesseurs(position);
	}

	@Override
	public void run() {

		// On créé la socket
		Socket sock = new Socket();
		try {
			// On initialise l'adresse à contacter
			SocketAddress sockaddr = new InetSocketAddress(
					this.getPair().getListeSuccesseurs()[this.getPositionSuccesseurs()].ip,
					this.getPair().getListeSuccesseurs()[this.getPositionSuccesseurs()].port);

			// On choisit le timeout
			int timeoutMs = 500;

			// On se connecte en définissant le timeout
			sock.connect(sockaddr, timeoutMs);

		} catch (SocketTimeoutException e) {
			System.out.println("PAIR TIMEOUT : Le pair n'est plus présent, il faut donc chercher un remplaçant !");
			// TODO
			// On cherche le remplaçant
			// Si le 1er successeur dead, on demande au 2eme ces successeurs,
			// idem pour le 2eme on demande au 3eme
			// Mais si le dernier (3eme) est dead on demande au 2eme pour
			// compléter.
			// TODO
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				sock.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// On ferme dans tous les cas
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

	public int getPositionSuccesseurs() {
		return positionSuccesseurs;
	}

	public void setPositionSuccesseurs(int positionSuccesseurs) {
		this.positionSuccesseurs = positionSuccesseurs;
	}

}
