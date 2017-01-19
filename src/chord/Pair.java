package chord;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
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

import javax.imageio.ImageIO;

import protocole.message.Convert_Message;
import protocole.message.Message;
import protocole.message.Parse_MessageType;
import protocole.message.TypeMessage;
import protocole.reparation.Reparation;
import services.InfosAnnuaire;
import services.Logs;

public class Pair {

	/** Infos sur le pair (ip, port, cle) */
	private PairInfos pairInfos;

	/** La socket d'écoute Pair */
	private ServerSocket server;

	/** La liste des successeurs */
	private PairInfos[] listeSuccesseurs;
	
	/** Thread de réparation */
	private Reparation reparation = null;

	/** Nombre de successeurs max */
	public final static int nbSucceseursMax = 3;

	/** Salon */
	public ArrayList<SalonInfos> listSalons;

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

		// Initialisation de la liste de nos salons
		listSalons = new ArrayList<SalonInfos>();

		// Le pair écoute
		attenteDeConnexion();

		// On contacte l'annuaire
		String[] retour = getListeClientsFromAnnuaire();

		// Si la liste retournée n'est pas vide
		if (retour != null) {
			for (String ipPortTxt : retour) {

				// On essaye de se connecter au premier de la liste
				if (joinMainChord(new PairInfos(ipPortTxt))) {
					break;
				}
			}
		}
		// Si la liste retournée est vide c'est qu'on est le 1er dans l'anneau

	}

	/**
	 * On demande à l'annuaire une liste de clients actifs dans l'anneau <br>
	 * On s'ajoute dans l'annuaire également
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

			// On lit la réponse de l'annuaire
			InputStream is = annuaire.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String msgString = br.readLine();

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

	/**
	 * On attend des connexions sur le ServerSocket
	 */
	private void attenteDeConnexion() {
		// Thread d'écoute
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					// Création du ServerSocket
					server = new ServerSocket(pairInfos.port);

					while (true) {

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

						// Si c'est un message de demande de modifs des
						// successeurs
						case ModificationSuccesseurs:
							// On lance la procédure d'ajout de nouveau pair
							modificationsSuccesseurs(message);
							break;

						// Si c'est un message pour demander les successeurs
						case GetSuccesseurs:
							getSuccesseurs(socket);
							break;

						// Si c'est un message pour demander les successeurs
						case JoinSalon:
							ajoutNouveauMembreDansSalon(infosDest, message);
							break;

						// Si c'est un message pour demander les successeurs
						case MessageSalon:
							gestionMessage(infosDest, message);
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
	public void sendMessage(PairInfos destInfos, Message msg) {

		try {
			Socket dest = new Socket(destInfos.ip, destInfos.port);

			// Envoi d'une image
			if (msg.getTypeMessage() == TypeMessage.Image) {

				// Récupération de l'image
				BufferedImage image = ImageIO.read(new File(msg.getMessage()));

				// Envoi de l'image
				ImageIO.write(image, "PNG", dest.getOutputStream());
			}

			// Envoi simple
			else {
				// Buffer de sortie
				PrintWriter out = new PrintWriter(dest.getOutputStream(), true);

				// Envoi du message au client
				out.println(Convert_Message.messageToJson(msg));

			}

			dest.close();

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
	 * Permet de demander à rejoindre l'anneau de Chord en contactant le Pair
	 * passé en paramètre
	 * 
	 * @param ip
	 *            ip du Pair à contacter
	 * @param port
	 *            port du Pair à contacter
	 * @return
	 */
	public boolean joinMainChord(PairInfos infosDest) {
		try {
			// Connexion au pair
			Socket dest = new Socket(infosDest.ip, infosDest.port);

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
	public void addPair(PairInfos nouveauPair, Message message) {

		// On compare si la clé du nouveau Pair peut être insérée entre nous et
		// nos successeurs
		for (int i = 0; i < nbSucceseursMax; i++) {
			// Récupération des infos du successeur courant
			PairInfos successeur = listeSuccesseurs[i];

			// On compare le 1er successeur avec nous même
			if (i == 0) {

				// Cas si notre 1er successeur est null (seul sur l'anneau)
				if (successeur == null) {

					// On ajoute le nouveau Pair à coté de nous (après nous)
					listeSuccesseurs[i] = nouveauPair;

					// On indique que le 1er successeur de ce nouveau pair
					// c'est nous
					Message msg = new Message(TypeMessage.ModificationSuccesseurs, pairInfos.getIpPort(),
							"0=" + pairInfos.getIpPort());
					sendMessage(nouveauPair, msg);

					// L'ajout est terminé
					return;
				}

				// Si la clé du nouveau Pair est supérieure à la notre
				if (nouveauPair.cle > pairInfos.cle) {

					// Si le nouveau a une clé supérieur à notre successeur
					if ((nouveauPair.cle > successeur.cle && pairInfos.cle > successeur.cle)
							|| nouveauPair.cle < successeur.cle) {

						// Si on est moins de 4 dans l'anneau
						if (isNombreSuccesseursMaxAtteint() == false) {

							// On décale les successeurs déja présents
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
							
							if(reparation == null){
								reparation = new Reparation(this);
								Thread t = new Thread (reparation);
								t.start();
							}

							// On informe notre nouveau successeur de ses
							// successeur
							Message msg = new Message(TypeMessage.ModificationSuccesseurs, pairInfos.getIpPort(),
									getListeSuccesseursFormatHashMap());
							sendMessage(nouveauPair, msg);

							// On décale les successeurs déja présents
							for (int k = (nbSucceseursMax - 1); k > i; k--) {
								listeSuccesseurs[k] = listeSuccesseurs[k - 1];
							}

							// On ajoute le nouveau Pair
							listeSuccesseurs[0] = nouveauPair;

						}

						// L'ajout est terminé
						return;

					}

				}
			}

			// On compare les successeurs entre eux
			else {

				// Infos du successeur précédent
				PairInfos successeurPrec = listeSuccesseurs[i - 1];

				// Cas si notre 1er successeur est null (seul sur l'anneau)
				if (successeur == null) {

					// On indique au successeur precedent de se débrouiller
					sendMessage(successeurPrec, message);

					// On ne s'occupe plus nous de l'ajout
					return;
				}

				// Si la clé du nouveau Pair est supérieure à la notre
				if (nouveauPair.cle > successeurPrec.cle) {

					// Si le nouveau a une clé supérieur à notre successeur
					if ((nouveauPair.cle > successeurPrec.cle && nouveauPair.cle > successeur.cle)
							|| nouveauPair.cle < successeur.cle) {

						// On indique au successeur precedent de se débrouiller
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
	 * Méthode qui permet de modifier ses successeurs avec ceux passé en
	 * paramètre (dans le Message)
	 * 
	 * @param message
	 */
	private void modificationsSuccesseurs(Message message) {
		// HashMap qui contient les successeurs à modifier
		HashMap<String, String> map = Parse_MessageType.parseMessageInHashMap(message.getMessage());

		// Parcours du HashMap
		for (Map.Entry<String, String> entry : map.entrySet()) {
			// Clé dans le Map = position du successeur dans le tableau
			int key = Integer.parseInt(entry.getKey());

			// Création d'un PairInfos avec ip:port
			PairInfos newSucc = new PairInfos(map.get(key + ""));

			// On modifie le successeur concerné
			listeSuccesseurs[key] = newSucc;

		}

	}

	/**
	 * Permet d'envoyer la liste de nos successeurs au Pair passé en paramère
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
		Message m = new Message(TypeMessage.GetSuccesseurs, this.pairInfos.getIpPort(), message);
		sendMessage(socket, m);
	}

	private void ajoutNouveauMembreDansSalon(PairInfos infosDest, Message message) {
		// Nom du salon
		String nomSalon = message.getMessage();

		// Parcours ma liste de salon
		for (SalonInfos salon : listSalons) {
			// Si on est dans le bon salon
			if (salon.getNom().equals(nomSalon)) {
				// On ajoute le Pair dedans
				salon.addMembre(infosDest);
			}
		}

	}

	private void gestionMessage(PairInfos infosDest, Message message) {
		// On fait une action en fonction du type du message reçu
		switch (message.getTypeMessage()) {

		case MessageChat:
			break;

		// Si c'est un message de type MessageSalon
		case MessageSalon:
			// Récupération du contenu du message
			HashMap<String, String> map = Parse_MessageType.parseMessageInHashMap(message.getMessage());

			// Récupération du nom du salon
			String salon = map.get("salon");

			// Récupération du salonInfos correspondant
			SalonInfos salonInfos = getSalonFromName(salon);

			// Si on est host du salon
			if (isHost(salon) == true) {

				// Récupération de la liste des membres du salon
				ArrayList<PairInfos> listMembres = salonInfos.getListMembres();

				// Parcours de la liste des membres
				for (PairInfos membre : listMembres) {

					// On fait attention à ne pas renvoyer le message à
					// l'envoyeur initial
					if (membre.equals(infosDest) == false) {
						sendMessage(membre, message);
					}
				}
			}

			// Affichage du message
			Logs.print(pairInfos.getIpPort() + " : (" + salon + ") " + map.get("message"));

			//
			break;

		default:
			break;

		}
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
			sendMessage(annuaire, new Message(TypeMessage.GetListeSalons, pairInfos.getIpPort(), ""));

			// On lit la réponse de l'annuaire
			InputStream is = annuaire.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String msgString = br.readLine();

			annuaire.close();

			// On transforme le message reçu
			Message message = Convert_Message.jsonToMessage(msgString);

			// Si aucun message n'est donné
			if (message.getMessage().length() == 0) {
				return null;
			} else {
				// HashMap contenant la liste des salons
				HashMap<String, String> map = Parse_MessageType.parseMessageInHashMap(message.getMessage());

				// Parcours du HashMap
				for (Map.Entry<String, String> entry : map.entrySet()) {
					// Clé dans le Map = position du successeur dans le tableau
					String nom = entry.getKey();

					// Création d'un PairInfos avec ip:port
					PairInfos infosHost = new PairInfos(map.get(nom + ""));

					// Création d'un objet SalonInfos
					SalonInfos salonInfos = new SalonInfos(nom, infosHost);

					// Ajout de cet objet à la liste
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
	 * Méthode qui permet au Pair de rejoindre un salon (si le salon n'existe
	 * pas le Pair devient host de ce nouveau salon)
	 * 
	 * @param nom
	 */
	public void joinChatRoom(String nom) {
		try {
			// Socket pour faire le lien avec l'annuaire
			Socket annuaire = new Socket(InfosAnnuaire.ip, InfosAnnuaire.port);

			// On contacte l'annuaire
			sendMessage(annuaire, new Message(TypeMessage.JoinSalon, pairInfos.getIpPort(), nom));

			// On lit la réponse de l'annuaire
			InputStream is = annuaire.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String msgString = br.readLine();

			annuaire.close();

			// On transforme le message reçu
			Message message = Convert_Message.jsonToMessage(msgString);

			// Infos du pair host du salon
			PairInfos infosHost = new PairInfos(message.getMessage());

			// Création objet infosSalon qui contient les infos sur le salon
			// qu'on a voulu join
			SalonInfos infosSalon = new SalonInfos(nom, infosHost);

			// On l'ajoute dans la liste
			listSalons.add(infosSalon);

			// On contacte l'host (si ce n'est pas notre salon)
			if (infosSalon.isHost(pairInfos) == false) {
				// Construction du message à envoyer à l'host
				Message msgToHost = new Message(TypeMessage.JoinSalon, pairInfos.getIpPort(), nom);

				// On l'envoie à l'host
				sendMessage(infosHost, msgToHost);
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	 * Retourne la liste des successeurs à partir d'une position donnée <br>
	 * Utilisé pour l'ajout quand on est en dessous de 4 personnes dans
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
			// Si on est retombé sur la pos de départ
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

	public void insertSuccesseur(PairInfos pi, int position) {

		for (int i = Pair.nbSucceseursMax - 1; i >= position; i--) {
			if (i == position) {
				this.listeSuccesseurs[i] = pi;
			} else {
				this.listeSuccesseurs[i] = this.listeSuccesseurs[i - 1];
			}
		}

	}

	public ArrayList<SalonInfos> getListSalons() {
		return listSalons;
	}

	public boolean isHost(String salon) {
		// Parcours de la liste des salons
		for (SalonInfos salonInfos : listSalons) {

			// Si on est dans le bon salon
			if (salonInfos.getNom().equals(salon)) {

				// Si on est l'host de ce salon
				if (salonInfos.isHost(pairInfos) == true) {
					return true;
				}
			}
		}

		return false;

	}

	public SalonInfos getSalonFromName(String salon) {
		// Parcours de la liste des salons
		for (SalonInfos salonInfos : listSalons) {

			// Si on est dans le bon salon
			if (salonInfos.getNom().equals(salon)) {

				return salonInfos;
			}
		}

		return null;
	}

}
