package chord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import protocole.message.Convert_Message;
import protocole.message.Message;
import protocole.message.Parse_MessageType;
import protocole.message.TypeMessage;
import services.InfosAnnuaire;

public class Pair {

	/** Infos sur le pair (ip, port, cle) */
	private PairInfos pairInfos;

	/** La socket d'�coute Pair */
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

		// Le pair �coute
		attenteDeConnexion();

		// On contacte l'annuaire
		String[] retour = getListeClientsFromAnnuaire();

		// Si la liste retourn�e n'est pas vide
		if (retour != null) {
			for (String s : retour) {
				String[] IpPort = s.split(":");

				// On essaye de se connecter au premier de la liste
				if (joinMainChord(IpPort[0], Integer.parseInt(IpPort[1]))) {
					break;
				}
			}
		}
		// Si la liste retourn�e est vide c'est qu'on est le 1er dans l'anneau

	}

	/**
	 * On demande � l'annuaire une liste de clients actifs dans l'anneau <br>
	 * On s'ajoute dans l'annuaire �galement
	 * 
	 * @return
	 */
	private String[] getListeClientsFromAnnuaire() {

		try {
			// Socket pour faire le lien avec l'annuaire
			Socket annuaire = new Socket(InfosAnnuaire.ip, InfosAnnuaire.port);

			// On contacte l'annuaire
			sendMessage(annuaire,
					new Message(TypeMessage.AjoutPair, pairInfos.getIpPort(), pairInfos.ip + ":" + pairInfos.port));

			// On lit la r�ponse de l'annuaire
			InputStream is = annuaire.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String msgString = br.readLine();

			annuaire.close();

			// On transforme le message re�u
			Message message = Convert_Message.jsonToMessage(msgString);
			
			// Si aucun message n'est donn�
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

	/**
	 * On attend des connexions sur le ServerSocket
	 */
	private void attenteDeConnexion() {
		// Thread d'�coute
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// Cr�ation du ServerSocket
					server = new ServerSocket(pairInfos.port);

					while (true) {

						// Attente de connexion
						Socket socket = server.accept();

						// On r�cup�re le message envoy�
						InputStream is = socket.getInputStream();
						InputStreamReader isr = new InputStreamReader(is);
						BufferedReader br = new BufferedReader(isr);
						String msgString = br.readLine();

						// On ferme la socket
						socket.close();

						Message message = Convert_Message.jsonToMessage(msgString);

						// On r�cup�re les infos du destinataire
						PairInfos infosDest = message.getPairInfos();

						// Traitement du message en fonction de son type
						switch (message.getTypeMessage()) {

						// Si c'est un message d'ajout
						case AjoutPair:

							// On lance la proc�dure d'ajout de nouveau pair
							addPair(infosDest, message);
							break;

						// Si c'est un message de demande de modifs des
						// successeurs
						case ModificationSuccesseurs:
							// On lance la proc�dure d'ajout de nouveau pair
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
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}).start();

	}

	/**
	 * Permet d'envoyer un message en connaissant l'ip et le port (PairInfos)
	 * d'un Pair
	 * 
	 * @param destInfos
	 * @param msg
	 */
	private void sendMessage(PairInfos destInfos, Message msg) {

		try {
			Socket dest = new Socket(destInfos.ip, destInfos.port);

			// Buffer de sortie
			PrintWriter out = new PrintWriter(dest.getOutputStream(), true);

			// Envoi du message au client
			out.println(Convert_Message.messageToJson(msg));

			dest.close();

			// Logs.print("'" + destInfos.getIpPort() + "' : " + msg);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Permet d'envoyer un message en passant par une Socket existante
	 * 
	 * @param socket
	 * @param msg
	 */
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

	/**
	 * Permet de demander � rejoindre l'anneau de Chord en contactant le Pair
	 * pass� en param�tre
	 * 
	 * @param ip
	 *            ip du Pair � contacter
	 * @param port
	 *            port du Pair � contacter
	 * @return
	 */
	public boolean joinMainChord(String ip, int port) {
		try {

			// Connexion au pair
			Socket dest = new Socket(ip, port);

			// Si la socket n'est pas connectee
			if (!dest.isConnected()) {

				dest.close();
				return false;
			}

			// Envoi du message pour demander d'�tre ajouter
			Message msg = new Message(TypeMessage.AjoutPair, pairInfos.getIpPort(), "ALLO ?");
			sendMessage(dest, msg);

			dest.close();

			// La connexion est bien �tablie
			return true;

		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * M�thode qui permet d'ajouter un pair <br>
	 * 
	 * @param ip
	 * @param port
	 */
	public void addPair(PairInfos nouveauPair, Message message) {

		// On compare si la cl� du nouveau Pair peut �tre ins�r�e entre nous et
		// nos successeurs
		for (int i = 0; i < nbSucceseursMax; i++) {
			// R�cup�ration des infos du successeur courant
			PairInfos successeur = listeSuccesseurs[i];

			// On compare le 1er successeur avec nous m�me
			if (i == 0) {

				// Cas si notre 1er successeur est null (seul sur l'anneau)
				if (successeur == null) {

					// On ajoute le nouveau Pair � cot� de nous (apr�s nous)
					listeSuccesseurs[i] = nouveauPair;

					// On indique que le 1er successeur de ce nouveau pair
					// c'est nous
					Message msg = new Message(TypeMessage.ModificationSuccesseurs, pairInfos.getIpPort(),
							"0=" + pairInfos.getIpPort());
					sendMessage(nouveauPair, msg);

					// L'ajout est termin�
					return;
				}

				// Si la cl� du nouveau Pair est sup�rieure � la notre
				if (nouveauPair.cle > pairInfos.cle) {

					// Si le nouveau a une cl� sup�rieur � notre successeur
					if ((nouveauPair.cle > successeur.cle && pairInfos.cle > successeur.cle)
							|| nouveauPair.cle < successeur.cle) {

						// Si on est moins de 4 dans l'anneau
						if (isNombreSuccesseursMaxAtteint() == false) {

							// On d�cale les successeurs d�ja pr�sents
							for (int k = (nbSucceseursMax - 1); k > i; k--) {
								listeSuccesseurs[k] = listeSuccesseurs[k - 1];
							}

							// On ajoute le nouveau Pair
							listeSuccesseurs[0] = nouveauPair;

							for (int j = 0; j < nbSucceseursMax; j++) {

								if (listeSuccesseurs[j] == null) {
									break;
								}

								Message msg = new Message(TypeMessage.ModificationSuccesseurs, pairInfos.getIpPort(),
										getListeSuccesseursFormatHashMap(j));
								sendMessage(listeSuccesseurs[j], msg);
							}
						} else {

							// On informe notre nouveau successeur de ses
							// successeur
							Message msg = new Message(TypeMessage.ModificationSuccesseurs, pairInfos.getIpPort(),
									getListeSuccesseursFormatHashMap());
							sendMessage(nouveauPair, msg);

							// On d�cale les successeurs d�ja pr�sents
							for (int k = (nbSucceseursMax - 1); k > i; k--) {
								listeSuccesseurs[k] = listeSuccesseurs[k - 1];
							}

							// On ajoute le nouveau Pair
							listeSuccesseurs[0] = nouveauPair;

						}

						// L'ajout est termin�
						return;

					}

				}
			}

			// On compare les successeurs entre eux
			else {

				// Infos du successeur pr�c�dent
				PairInfos successeurPrec = listeSuccesseurs[i - 1];

				// Cas si notre 1er successeur est null (seul sur l'anneau)
				if (successeur == null) {

					// On indique au successeur precedent de se d�brouiller
					sendMessage(successeurPrec, message);

					// On ne s'occupe plus nous de l'ajout
					return;
				}

				// Si la cl� du nouveau Pair est sup�rieure � la notre
				if (nouveauPair.cle > successeurPrec.cle) {

					// Si le nouveau a une cl� sup�rieur � notre successeur
					if ((nouveauPair.cle > successeurPrec.cle && nouveauPair.cle > successeur.cle)
							|| nouveauPair.cle < successeur.cle) {

						// On indique au successeur precedent de se d�brouiller
						sendMessage(successeurPrec, message);

						// On ne s'occupe plus nous de l'ajout
						return;

					}

				}

			}
		}

	}

	/**
	 * Boolean qui indique si on a atteint le nombre max de successeur
	 * 
	 * @return
	 */
	public boolean isNombreSuccesseursMaxAtteint() {
		for (PairInfos successeur : listeSuccesseurs) {
			if (successeur == null) {
				return false;
			}
		}

		return true;
	}

	/**
	 * M�thode qui permet de modifier ses successeurs avec ceux pass� en
	 * param�tre (dans le Message)
	 * 
	 * @param message
	 */
	private void modificationsSuccesseurs(Message message) {
		// HashMap qui contient les successeurs � modifier
		HashMap<String, String> map = Parse_MessageType.parseMessageInHashMap(message.getMessage());

		// Parcours du HashMap
		for (Map.Entry<String, String> entry : map.entrySet()) {
			// Cl� dans le Map = position du successeur dans le tableau
			int key = Integer.parseInt(entry.getKey());

			// Cr�ation d'un PairInfos avec ip:port
			PairInfos newSucc = new PairInfos(map.get(key + ""));

			// On modifie le successeur concern�
			listeSuccesseurs[key] = newSucc;

		}

	}

	/**
	 * Permet d'envoyer la liste de nos successeurs au Pair pass� en param�re
	 * (Socket)
	 * 
	 * @param socket
	 */
	private void getSuccesseurs(Socket socket) {
		// Message qui va contenir la liste de nos successeurs
		String message = "";
		int i = 0;

		// Parcours de la liste de nos successeurs
		for (PairInfos successeurs : this.getListeSuccesseurs()) {
			if (i != 0) {
				message += "&";
			}
			message += i + "=" + successeurs.getIpPort();
			i++;
		}

		// On envoie le message au Pair (socket
		Message m = new Message(TypeMessage.getSuccesseurs, this.pairInfos.getIpPort(), message);
		sendMessage(socket, m);
	}

	/**
	 * Retourne la liste des Salons en demandant l'annuaire
	 * 
	 * @return
	 */
	public ArrayList<SalonInfos> getChatRoomsList() {
		// Liste qui contient la liste des Infos sur les salons en cours
		ArrayList<SalonInfos> listSalons = new ArrayList<SalonInfos>();

		try {
			// Socket pour faire le lien avec l'annuaire
			Socket annuaire = new Socket(InfosAnnuaire.ip, InfosAnnuaire.port);

			// On contacte l'annuaire en lui demandant la liste des salons
			sendMessage(annuaire, new Message(TypeMessage.getListeSalons, pairInfos.getIpPort(), ""));

			// On lit la r�ponse de l'annuaire
			InputStream is = annuaire.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String msgString = br.readLine();

			annuaire.close();

			// On transforme le message re�u
			Message message = Convert_Message.jsonToMessage(msgString);
			System.out.println(message.getMessage());
			// Si aucun message n'est donn�
			if (message.getMessage().length() == 0) {
				return null;
			} else {
				// HashMap contenant la liste des salons
				HashMap<String, String> map = Parse_MessageType.parseMessageInHashMap(message.getMessage());

				// Parcours du HashMap
				for (Map.Entry<String, String> entry : map.entrySet()) {
					// Cl� dans le Map = position du successeur dans le tableau
					String nom = entry.getKey();

					// Cr�ation d'un PairInfos avec ip:port
					PairInfos infosHost = new PairInfos(map.get(nom + ""));

					// Cr�ation d'un objet SalonInfos
					SalonInfos salonInfos = new SalonInfos(nom, infosHost);

					// Ajout de cet objet � la liste
					listSalons.add(salonInfos);
				}
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return listSalons;

	}

	/**
	 * Retourne la liste des successeurs
	 * 
	 * @return
	 */
	public PairInfos[] getListeSuccesseurs() {
		return listeSuccesseurs;
	}

	/**
	 * Retourne la liste des sucesseurs au format HashMap
	 * 
	 * @return
	 */
	public String getListeSuccesseursFormatHashMap() {
		String retour = "";

		retour += "0=" + listeSuccesseurs[0].getIpPort() + "&";
		retour += "1=" + listeSuccesseurs[1].getIpPort() + "&";
		retour += "2=" + listeSuccesseurs[2].getIpPort();

		return retour;

	}

	/**
	 * Retourne la liste des successeurs � partir d'une position donn�e <br>
	 * Utilis� pour l'ajout quand on est en dessous de 4 personnes dans
	 * l'anneau)
	 * 
	 * @param pos
	 * @return
	 */
	public String getListeSuccesseursFormatHashMap(int pos) {
		String retour = "";

		// Permet de parcourir nos successeurs
		int i = pos + 1;

		int nb = 0;

		while (true) {
			// Si on est retomb� sur la pos de d�part
			if (i == pos) {
				break;
			}
			if (i == nbSucceseursMax) {
				i = 0;
				retour += "&" + nb + "=" + pairInfos.getIpPort();
				nb++;
				continue;
			}
			if (listeSuccesseurs[i] == null) {
				retour += "&" + nb + "=" + pairInfos.getIpPort();
				nb++;
				i = 0;
				continue;
			}

			retour += "&" + nb + "=" + listeSuccesseurs[i].getIpPort();
			nb++;
			i++;
		}

		return retour.substring(1);

	}

	/**
	 * Retourne les infos sur le Pair
	 * 
	 * @return
	 */
	public PairInfos getInfos() {
		return pairInfos;
	}

	public String toString() {

		String msg = "[" + pairInfos.getIpPort() + "] " + pairInfos.cle + " : ";

		for (int i = 0; i < nbSucceseursMax; i++) {
			if (listeSuccesseurs[i] != null) {
				msg += listeSuccesseurs[i].cle + " ";
			}
		}
		return msg;
	}

}
