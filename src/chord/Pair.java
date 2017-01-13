package chord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import protocole.message.Convert_Message;
import protocole.message.Message;
import protocole.message.Parse_MessageType;
import protocole.message.TypeMessage;
import services.InfosAnnuaire;
import services.Logs;

@SuppressWarnings("unused")
public class Pair {

	/** Infos sur le pair (ip, port, cle) */
	private PairInfos pairInfos;

	/** La socket d'écoute Pair */
	private ServerSocket server;

	/** La liste des successeurs */
	private PairInfos[] listeSuccesseurs;

	/** Nombre de successeurs max */
	public final static int nbSucceseursMax = 3;

	/**
	 * Constructeur de la classe {@link Pair}
	 * 
	 * @param ip
	 *            ip du pair
	 * @param port
	 *            port du pair
	 */
	public Pair(String ip, int port) {
		// Sauvegarde des infos
		pairInfos = new PairInfos(ip, port);

		// Instantiation de la liste des successeurs
		listeSuccesseurs = new PairInfos[nbSucceseursMax];

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
		// Si la liste retournée est vide c'est qu'on est le 1er dans l'anneau

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
			sendMessage(annuaire, new Message(TypeMessage.AjoutPair, pairInfos.ip + ":" + pairInfos.port,
					pairInfos.ip + ":" + pairInfos.port));

			// On lit la réponse de l'annuaire
			InputStream is = annuaire.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String msgString = br.readLine();

			Logs.print("Message reçu de l'annuaire : " + msgString);

			annuaire.close();

			// On transforme le message reçu
			Message message = Convert_Message.jsonToMessage(msgString);

			// Si aucun message n'est donné
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
					server = new ServerSocket(pairInfos.port);

					// Attente de connexion
					Socket socket = server.accept();

					// On récupère le message envoyé
					InputStream is = socket.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					String msgString = br.readLine();

					// On ferme la socket
					socket.close();

					Message message = Convert_Message.jsonToMessage(msgString);

					// On récupére les infos du destinataire
					PairInfos infosDest = message.getPairInfos();

					// Traitement du message en fonction de son type
					switch (message.getTypeMessage()) {

					// Si c'est un message d'ajout
					case AjoutPair:
						// On lance la procédure d'ajout de nouveau pair
						addPair(infosDest, message);
						break;

					// Si c'est un message de demande de modifs des successeurs
					case ModificationSuccesseurs:
						// On lance la procédure d'ajout de nouveau pair
						modificationsSuccesseurs(message);
						break;

					case getSuccesseurs:
						getSuccesseurs(socket);
						break;

					default:
						break;
					}
					// On ferme la socket
					socket.close();

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}).start();

	}

	private void sendMessage(PairInfos destInfos, Message msg) {

		try {
			Socket dest = new Socket(destInfos.ip, destInfos.port);

			// Buffer de sortie
			PrintWriter out = new PrintWriter(dest.getOutputStream(), true);

			// Envoi du message au client
			out.println(Convert_Message.messageToJson(msg));

			dest.close();

			Logs.print("Message envoyé à '" + destInfos.getIpPort() + "' : " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendMessage(Socket socket, Message msg) {

		try {
			// Buffer de sortie
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			// Envoi du message au client
			out.println(Convert_Message.messageToJson(msg));

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
			Message msg = new Message(TypeMessage.AjoutPair, pairInfos.getIpPort(), "ALLO ?");
			sendMessage(dest, msg);

			dest.close();

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
	public void addPair(PairInfos destInfos, Message message) {

		// Comparaison des clés des successeurs et cordes (TODO)
		for (int i = 0; i < nbSucceseursMax; i++) {
			// Cas où on est seul
			if (listeSuccesseurs[i] == null) {
				// Envoi du message pour demander d'être ajouter
				Message msg = new Message(TypeMessage.ModificationSuccesseurs, pairInfos.ip + ":" + pairInfos.port,
						"0=" + pairInfos.ip + ":" + pairInfos.port);

				sendMessage(destInfos, msg);

				listeSuccesseurs[0] = destInfos;

				System.out.println("connecté");

				break;
			}

			// Infos du successeur
			PairInfos succInfos = listeSuccesseurs[0];

			// Comparaison des clés :

			if (i == 0) {
				// On compare notre clé avec le successeur 1 :

				// Si la clé du nouveau pair est comprise entre notre clé et
				// celle du successeur 1
				if (pairInfos.cle < destInfos.cle && destInfos.cle < succInfos.cle || listeSuccesseurs[i] == null) {
					// On lui envoie sa liste de successeurs (la notre)
					String listSuccesseursTxt = "";

					for (int k = 0; k < nbSucceseursMax; k++) {

						if (listeSuccesseurs[k] == null) {
							break;
						} else {
							if (k > 0) {
								listSuccesseursTxt += "&";
							}

							listSuccesseursTxt += k + "=" + listeSuccesseurs[k].ip + ":" + listeSuccesseurs[k].port;
						}
					}

					// Envoi du message pour demander d'être ajouter
					Message msg = new Message(TypeMessage.ModificationSuccesseurs, pairInfos.ip + ":" + pairInfos.port,
							listSuccesseursTxt);

					sendMessage(listeSuccesseurs[i], msg);

					// On l'ajoute dans notre liste de successeurs
					for (int j = (nbSucceseursMax - 1); j >= 0; j--) {

						if (j == 0) {
							// On place le nouveau pair
							listeSuccesseurs[j] = destInfos;
						} else {
							// On décale les successeurs
							listeSuccesseurs[j] = listeSuccesseurs[j - 1];
						}
					}
					System.out.println("connecté");

					break;
				}

			} else {

				// On compare la clé du nouveau pair avec le successeur courant
				// -1 et le successeur courant

				// Récupération des infos du successeur précédent
				PairInfos precInfos = listeSuccesseurs[i - 1];

				if (precInfos.cle < destInfos.cle && destInfos.cle < succInfos.cle) {
					// On demande au successeur précédent de l'ajouter
					sendMessage(listeSuccesseurs[i - 1], message);
				} else
				// Si on est arrivé au dernier successeur
				if (i == (nbSucceseursMax - 1)) {
					// On demande au dernier successeur de l'ajouter
					sendMessage(listeSuccesseurs[i], message);
				}
			}

		}

	}

	private void modificationsSuccesseurs(Message message) {
		// HashMap qui contient les successeurs à modifier
		HashMap<String, String> map = Parse_MessageType.parseMessageInHashMap(message.getMessage());

		// Parcours du HashMap
		for (Map.Entry<String, String> entry : map.entrySet()) {

			int key = Integer.parseInt(entry.getKey());
			String value = entry.getValue();

			PairInfos newSucc = PairInfos(map.get(key + ""));

			listeSuccesseurs[key] = newSucc;

		}
		for (int i = 0; i < map.size(); i++) {

		}

	}

	private void getSuccesseurs(Socket socket) {
		// TODO Auto-generated method stub
		String message = "";
		int i = 0;
		for (PairInfos successeurs : this.getListeSuccesseurs()) {
			if (i != 0) {
				message += "&";
			}
			message += i + "=" + successeurs.getIpPort();
			i++;
		}
		Message m = new Message(TypeMessage.getSuccesseurs, this.pairInfos.getIpPort(), message);
		this.sendMessage(socket, m);
	}

	private PairInfos PairInfos(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public PairInfos[] getListeSuccesseurs() {
		return listeSuccesseurs;
	}

	public PairInfos getInfos() {
		return pairInfos;
	}

	public String toString() {

		String msg = "(" + pairInfos.getIpPort() + ")" + pairInfos.cle + "";

		if (listeSuccesseurs[0] != null) {
			msg += " -> " + listeSuccesseurs[0].cle;
		}
		return msg;
	}

}
