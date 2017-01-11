package Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.digest.DigestUtils;

import services.InfosAnnuaire;
import services.Logs;

public class Pair {

	/** IP du pair */
	private String ip;

	/** Port du pair */
	private int port;

	/** Identifiant du pair */
	private Long id;

	/** La socket d'écoute Pair */
	private ServerSocket server;

	/** La liste des successeurs */
	private Socket[] listeSuccesseurs;

	/** Nombre de successeurs max */
	private final int nbSucceseursMax = 3;

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
		String hash = HashId(identifiant);

		// Identifiant en fonction du hash
		BigInteger value = new BigInteger(hash.substring(0, 8), 16);
		id = value.longValue();

		// Instantiation de la liste des successeurs
		listeSuccesseurs = new Socket[nbSucceseursMax];

		// Le pair écoute
		attenteDeConnexion();

		// On contacte l'annuaire
		String[] retour = getListeFromAnnuaire();

		// Si la liste retournée n'est pas vide
		if (retour != null) {
			for (String s : retour) {
				String[] IpPort = s.split(":");

				// On essaye de se connecter au premier de la liste
				if (join(IpPort[0], Integer.parseInt(IpPort[1]))) {
					break;
				}
			}
		}

	}

	/**
	 * On demande à l'annuaire une liste de clients actifs dans l'anneau
	 * 
	 * @return
	 */
	private String[] getListeFromAnnuaire() {

		try {
			// Socket pour faire le lien avec l'annuaire
			Socket annuaire = new Socket(InfosAnnuaire.ip, InfosAnnuaire.port);

			// On contacte l'annuaire
			sendMessage(annuaire, "Allo ?");

			// On lit la réponse de l'annuaire
			InputStream is = annuaire.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String msgString = br.readLine();
			Logs.print("Message reçu de l'annuaire : " + msgString);

			// On transforme le message reçu
			Message message = Convert_Message.jsonToMessage(msgString);

			if (message.getMessage().length() == 0) {
				return null;
			} else {
				return message.getMessage().split(";");
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
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

					Logs.print("Demande de connexion de '" + socket.getInetAddress().getHostAddress() + ":"
							+ socket.getPort() + "'..");

					// On attend de recevoir le message "Ajoute moi"
					InputStream is = socket.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					String msgString = br.readLine();

					Logs.print("Message reçu de '" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort()
							+ "' : " + msgString);

					Message message = Convert_Message.jsonToMessage(msgString);

					// Si c'est un message d'ajout
					if (message.getTypeMessage() == TypeMessage.AjoutPair) {
						// On ajoute ce nouveau client à l'anneau
						addPair(socket);
					} else {
						socket.close();
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}).start();

	}

	private void enAttenteDeMessage(Socket socket) {
		// Thread d'écoute
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						InputStream is = socket.getInputStream();
						InputStreamReader isr = new InputStreamReader(is);
						BufferedReader br = new BufferedReader(isr);
						String msgString = br.readLine();
						Logs.print("Message reçu de '" + socket.getInetAddress().getHostAddress() + ":"
								+ socket.getPort() + "' : " + msgString);

						Message message = Convert_Message.jsonToMessage(msgString);
						System.out.println(message);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private void sendMessage(Socket socket, String message) {

		try {
			// Buffer de sortie
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			// Envoi du message au client
			out.println(message);

			Logs.print("Message envoyé à '" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "' : "
					+ message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean join(String ip, int port) {
		try {
			Logs.print("Demande de connexion à '" + ip + ":" + port + "'..");

			// Connexion au pair
			Socket dest = new Socket(ip, port);

			// Si la socket n'est pas connectee
			if (!dest.isConnected()) {
				dest.close();
				return false;
			}

			// Envoi du message pour demander d'être ajouter
			Message msg = new Message(TypeMessage.AjoutPair, "ip:port et numero", "ALLO ?");
			sendMessage(dest, Convert_Message.messageToJson(msg));

			// La connexion est bien établie
			return true;

		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Méthode qui permet d'ajouter un pair <br>
	 * 
	 * @param ip
	 * @param port
	 */
	public void addPair(Socket socket) {
		// Construction de la clé avec ip et port
		String cIP = socket.getInetAddress().getHostAddress();
		int cPORT = socket.getPort();

		// Construction de l'identifiant du pair (id:port)
		String identifiant = (cIP + ":" + cPORT);

		// On Hash l'identifiant
		String hash = HashId(identifiant);

		// Identifiant en fonction du hash
		BigInteger value = new BigInteger(hash.substring(0, 8), 16);
		long cle = value.longValue();

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
	public Long getId() {
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
		return "[IP=" + ip + ";PORT=" + port + "ID=" + id + "]";
	}

	public static void main(String[] args) {
		// On active les logs dans la console
		Logs.activer(true);

		Pair pair1 = new Pair("localhost", 7777);

		Pair pair2 = new Pair("localhost", 7879);

		pair2.join("localhost", 7777);
	}
}
