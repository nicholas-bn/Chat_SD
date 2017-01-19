package protocole.reparation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

import chord.Pair;
import chord.PairInfos;
import protocole.message.Convert_Message;
import protocole.message.Message;
import protocole.message.Parse_MessageType;
import protocole.message.TypeMessage;

public class Check_Ordre_Successeurs implements Runnable {

	// Permet d'avoir un lien vers le pair
	private Pair pair;

	// On connait le successeur à vérifier
	private int positionSuccesseur;

	// Constructeur
	public Check_Ordre_Successeurs(Pair p, int positionSuccesseur) {
		this.setPair(p);
		this.setPositionSuccesseur(positionSuccesseur);
	}

	@Override
	public void run() {

		Socket recuperationSuccesseurs = new Socket();
		try {
			// On récupére le successeur qu'on doit check
			PairInfos successeur = this.getPair().getListeSuccesseurs()[positionSuccesseur];

			// On construit le message pour récuper les successeurs de notre
			// avant dernier successeur
			Message m = new Message(TypeMessage.GetSuccesseurs, this.getPair().getInfos().getIpPort(), "");

			// On initialise l'adresse à contacter
			SocketAddress sockaddr = new InetSocketAddress(successeur.ip, successeur.port);

			// On initialise la socket vers le successeur
			recuperationSuccesseurs.connect(sockaddr);

			// Buffer de sortie
			PrintWriter out = new PrintWriter(recuperationSuccesseurs.getOutputStream(), true);

			// Envoi du message au client
			out.println(Convert_Message.messageToJson(m));
			out.flush();
			out.close();

			// On attend maintenant le retour
			InputStream is = recuperationSuccesseurs.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String msgJSON = br.readLine();

			// On passe de JSON à Message
			Message retour = Convert_Message.jsonToMessage(msgJSON);
			String message = retour.getMessage();

			// On l'affiche
			System.out.println("Successeurs de successeur (" + successeur.getIpPort() + "): " + message);

			// On parse le message
			HashMap<String, String> contenuMessage = new HashMap<String, String>();
			contenuMessage = Parse_MessageType.parseMessageInHashMap(message);

			// On convertit le hashmap en arraylist
			ArrayList<String> arrayListSuccesseursAVerifier = new ArrayList<String>();
			for (int i = 0; i < contenuMessage.size(); i++) {
				arrayListSuccesseursAVerifier.add(contenuMessage.get("" + i));
			}

			// On compare de notre 2eme pair jusqu'au dernier avec son 1er pair
			// jusqu'à son avant-dernier
			boolean isOffSet = false;
			for (int incNotrePair = 1, incAutrePair = 0; incNotrePair < Pair.nbSucceseursMax
					|| incAutrePair < Pair.nbSucceseursMax - 1; incNotrePair++, incAutrePair++) {
				// Si les pairs sont différents on met isOffSet à true
				if (this.getPair().getListeSuccesseurs()[incNotrePair].getIpPort() != arrayListSuccesseursAVerifier
						.get(incAutrePair)) {
					isOffSet = true;
				}
			}

			// Si on est décalé entre nos pairs
			if (isOffSet) {
				// On reparcours
				for (int incNotrePair = 1, incAutrePair = 0; incNotrePair < Pair.nbSucceseursMax
						|| incAutrePair < Pair.nbSucceseursMax - 1; incNotrePair++, incAutrePair++) {
					// Si les pairs sont différents on met isOffSet à true
					if (this.getPair().getListeSuccesseurs()[incNotrePair].getIpPort() != arrayListSuccesseursAVerifier
							.get(incAutrePair)) {
						// Si la clef de notre successeur est inférieur on
						// change
						PairInfos pi = new PairInfos(arrayListSuccesseursAVerifier.get(incAutrePair));
						if (this.getPair().getListeSuccesseurs()[incNotrePair].getCle() > pi.getCle()) {

							this.getPair().insertSuccesseur(pi, incNotrePair);
						}
					}
				}
			}

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				// On ferme la socket
				recuperationSuccesseurs.close();
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

	public int getPositionSuccesseur() {
		return positionSuccesseur;
	}

	public void setPositionSuccesseur(int positionSuccesseur) {
		this.positionSuccesseur = positionSuccesseur;
	}

}
