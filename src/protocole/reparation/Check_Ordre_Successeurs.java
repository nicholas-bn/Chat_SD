package protocole.reparation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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

		try {
			// On récupére le successeur qu'on doit check
			PairInfos successeur = this.getPair().getListeSuccesseurs()[positionSuccesseur];

			// On construit le message pour récuper les successeurs de notre
			// avant dernier successeur
			Message m = new Message(TypeMessage.getSuccesseurs, this.getPair().getInfos().getIpPort(), "");

			// On initialise la socket vers le successeur
			Socket recuperationSuccesseurs = new Socket(successeur.ip, successeur.port);

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
			System.out.println("Successeurs de secondSuccesseur '" + successeur.getIpPort() + "): " + message);

			// On parse le message
			HashMap<String, String> contenuMessage = new HashMap<String, String>();
			contenuMessage = Parse_MessageType.parseMessageInHashMap(message);
			
			// TODO
			// TODO
			
			// TODO
			// TODO
			
			// On ferme la socket
			recuperationSuccesseurs.close();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
