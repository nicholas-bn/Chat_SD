package Main;

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

	public static void main(String[] args) {
		// On cree un ID referent qui est egal a l'ID du 1er pair
		String idReferent = "0210df6cd869c217af4ac03664c330d9c9841926";
		// On cree un autre ID qui est different
		String autreID = "4c38d8705ff98c8c1e89b6694b1281e22f63ee084";
		Pair pair1 = new Pair("127.01.01", 15);
		// On observe l'identifiant cree avant hashage
		System.out.println("Identifiant avant hashage: " + pair1.getId());
		// On hache l'identifiant
		pair1.HashId(pair1.getId());
		System.out.println("Identifiant apres hashage: " + pair1.getId());
		System.out.println(pair1.CompareID(idReferent));
		System.out.println(pair1.CompareID(autreID));
	}

}
