package Main;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.codec.digest.DigestUtils;

import services.Logs;
import view.TextAreaOutputStream;

public class Pair {

	/** IP du pair */
	private String ip;

	/** Port du pair */
	private int port;

	/** Identifiant du pair (ip:port) */
	private String id;

	/** La socket d'écoute Pair */
	private ServerSocket server;

	/** Le prédecesseur du Pair */
	private Socket prev;

	/** Le successeur du Pair */
	private Socket next;

	/**
	 * Constructeur de la classe {@link Pair}
	 * 
	 * @param ip
	 *            ip du pair
	 * @param port
	 *            port du pair
	 */
	public Pair(String ipPair, int portPair) {
		ip = ipPair;
		port = portPair;

		// Construction de l'identifiant du pair (id:port)
		String identifiant = (ip + ":" + port);

		// On Hash l'identifiant
		id = HashId(identifiant);

		// Le pair écoute
		attenteDeConnexion();

	}

	private void attenteDeConnexion() {
		// Thread d'écoute
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// Création du ServerSocket
					server = new ServerSocket(port);

					Logs.print("Client '" + ip + ":" + port + "' en marche..");

					// Attente de connexion
					Socket socket = server.accept();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
		;

	}

	/**
	 * Méthode qui permet d'ajouter un pair <br>
	 * TODO Modifier quand on passera à Chord
	 * 
	 * @param ip
	 * @param port
	 */
	public void addPair(Socket socket) {
		// On indique à notre successeur actuel son nouveau prédecesseur

		// On indique au nouveau Pair son prédecesseur et son successeur
		// (prev = this et next = this.next)

		// On met à jour le successeur acteur
		next = socket;
	}

	/**
	 * Méthode qui permet de Hash un String en SHA-1
	 * 
	 * @param toHash
	 * @return
	 */
	private String HashId(String toHash) {
		return DigestUtils.sha1Hex(toHash);
	}

	/**
	 * Méthode qui permet de comparer un identifiant avec celui du pair actuel
	 * 
	 * @param idReferent
	 * @return
	 */
	public boolean CompareID(String idReferent) {
		if (idReferent.equals(id)) {
			return true;
		}
		return false;
	}

	/**
	 * Retourne l'ip du pair
	 * 
	 * @return
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Retourne le port du pair
	 * 
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Retourne l'identifiant du pair
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * Retourne le prédecesseur du pair
	 * 
	 * @return
	 */
	public Socket getPrev() {
		return prev;
	}

	/**
	 * Retourne le successeur du pair
	 * 
	 * @return
	 */
	public Socket getNext() {
		return next;
	}

	/**
	 * Modifie le prédeccesseur du Pair
	 * 
	 * @param prev
	 */
	public void setPrev(Socket prev) {
		this.prev = prev;
	}

	/**
	 * Modifie le successeur du Pair
	 * 
	 * @param prev
	 */
	public void setNext(Socket next) {
		this.next = next;
	}

	public String toString() {
		return ("IP et Port: " + ip + ":" + port);
	}

	public static void main(String[] args) {
		// On active les logs dans la console
		Logs.activer(true);

		Pair pair1 = new Pair("128.0.0.1", 4545);
	}
}
