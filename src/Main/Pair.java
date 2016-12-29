package Main;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.codec.digest.DigestUtils;

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

		// Thread d'écoute
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// Création du ServerSocket
					server = new ServerSocket(port);

					// Attente de connexion
					Socket socket = server.accept();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});

	}

	/**
	 * Méthode qui permet d'ajouter un pair <br>
	 * TODO Modifier quand on passera à Chord
	 * 
	 * @param ip
	 * @param port
	 */
	public void addPair(String ip, int port) {
		
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
	public String toString()
	{
		return ("IP et Port: "+ip + ":" + port);
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

	public static void main(String[] args) {
		// On cree un ID referent qui est egal a l'ID du 1er pair
		String idReferent = "2ffb317eeba53b25423c16d5a4f054221f628268";
		// On cree un autre ID qui est different
		String autreID = "4c38d8705ff98c8c1e89b6694b1281e22f63ee084";
		Pair pair1 = new Pair("127.01.01", 15);
		Socket pairPrev = new Socket();
		Socket pairNext = new Socket();
		// On observe l'identifiant cree avant hashage
		System.out.println("Identifiant apres hashage: " + pair1.getId());
		System.out.println(pair1.CompareID(idReferent));
		System.out.println(pair1.CompareID(autreID));
		
}
}


