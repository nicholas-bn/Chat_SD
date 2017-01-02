package Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;

import services.Logs;

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

					Logs.print("Demande de connexion de '" + socket.getInetAddress() + ":" + socket.getPort() + "'..");

					// TODO en cours

					// On ajoute ce nouveau client à l'anneau
					addPair(socket);

					// Si on est que 2 dans l'anneau
					if (prev.equals(next)) {
						// On attend les messages de prev
						enAttenteDeMessage(prev);
					} else {
						// On attend les messages de prev et next
						enAttenteDeMessage(prev);
						enAttenteDeMessage(next);
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}).start();

	}

	private void enAttenteDeMessage(Socket socket) {
		try {

			// Buffer d'entree
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			// Buffer de sortie
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			// Send a welcome message to the client.
			out.println("Hello, you are client #.");
			out.println("Enter a line with only a period to quit\n");

			// Get messages from the client, line by line; return them
			// capitalized
			while (true) {
				String input = in.readLine();
				if (input == null || input.equals(".")) {
					break;
				}
				out.println(input.toUpperCase());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendMessage(String message) {

	}

	private void sendMessage(Socket socket, String message) {

		try {
			// Buffer de sortie
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			// Envoi du message au client
			out.println(message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void join(String ip, int port) {
		try {
			Logs.print("Demande de connexion à '" + ip + ":" + port + "'..");

			// Connexion au pair
			Socket dest = new Socket(ip, port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Méthode qui permet d'ajouter un pair <br>
	 * TODO Modifier quand on passera à Chord
	 * 
	 * @param ip
	 * @param port
	 */
	public void addPair(Socket socket) {
		// Cas où on était seul dans l'anneau (lancement)
		if (prev == null && next == null) {
			// On met à jour prev et next
			prev = socket;
			next = socket;

			// On demande à l'autre client de mettre à jour ses prev et next
			sendMessage(socket, "MAJ prev");
			sendMessage(socket, "MAJ next");
		}

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

		Pair pair1 = new Pair("localhost", 4545);

		Pair pair2 = new Pair("localhost", 887);

		try {
			TimeUnit.SECONDS.sleep(2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pair2.join("localhost", 4545);
	}
}
