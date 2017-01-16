package protocole.reparation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;

import chord.Pair;
import chord.PairInfos;
import protocole.message.Convert_Message;
import protocole.message.Message;
import protocole.message.Parse_MessageType;
import protocole.message.TypeMessage;

/**
 * Classe threader qui permet de tester la connexion à un pair distant
 * 
 * @author Barnini
 *
 */
public class Check_Connexion implements Runnable {

	// Permet d'avoir un lien vers le pair
	private Pair pair;

	// On connait le successeur à vérifier
	private int positionSuccesseurs;

	// Constructeur
	public Check_Connexion(int position, Pair pair) {
		// TODO Auto-generated constructor stub
		this.setPair(pair);
		this.setPositionSuccesseurs(position);
	}

	@Override
	public void run() {

		// On créé la socket
		Socket sock = new Socket();
		try {
			// On initialise l'adresse à contacter
			SocketAddress sockaddr = new InetSocketAddress(
					this.getPair().getListeSuccesseurs()[this.getPositionSuccesseurs()].ip,
					this.getPair().getListeSuccesseurs()[this.getPositionSuccesseurs()].port);

			// On choisit le timeout
			int timeoutMs = 500;

			// On se connecte en définissant le timeout
			sock.connect(sockaddr, timeoutMs);

		} catch (SocketTimeoutException e) {
			System.out.println("PAIR TIMEOUT : Le pair n'est plus présent, il faut donc chercher un remplaçant !");

			// Si ce n'est pas le dernier on doit décaler d'abord
			if (this.getPositionSuccesseurs() != Pair.nbSucceseursMax - 1) {

				// Nous allons jusqu'à 'Pair.nbSucceseursMax - 1' car on ne veut
				// pas s'occuper du dernier ne connaissant pas son successeur
				// direct pour le moment
				for (int i = this.getPositionSuccesseurs(); i < (Pair.nbSucceseursMax - 1); i++) {
					// On décale les successeurs vers la gauche
					this.getPair().getListeSuccesseurs()[i] = this.getPair().getListeSuccesseurs()[i + 1];

				}

			}

			// On doit remplacer le dernier dans tous les cas
			try {
				// On recher l'avant dernier uniquement
				PairInfos secondSuccesseur = this.getPair().getListeSuccesseurs()[Pair.nbSucceseursMax - 2];

				// On construit le message pour récuper les successeurs de notre
				// avant dernier successeur
				Message m = new Message(TypeMessage.getSuccesseurs, this.getPair().getInfos().getIpPort(), "");
				
				// On initialise la socket vers notre second successeur
				Socket recuperationSuccesseurs = new Socket(secondSuccesseur.ip, secondSuccesseur.port);
				
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
				System.out.println("Successeurs de secondSuccesseur '" + secondSuccesseur.getIpPort()+"): "+message);
				
				// On parse le message 
				HashMap<String, String> contenuMessage = new HashMap<String, String>();
				contenuMessage = Parse_MessageType.parseMessageInHashMap(message);
				
				// On remplace le dernier par le 2 successeur de notre 2eme successeur
				this.getPair().getListeSuccesseurs()[Pair.nbSucceseursMax - 1] = new PairInfos(contenuMessage.get("1"));
				
				// On ferme la socket
				recuperationSuccesseurs.close();
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				sock.close();
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

	public int getPositionSuccesseurs() {
		return positionSuccesseurs;
	}

	public void setPositionSuccesseurs(int positionSuccesseurs) {
		this.positionSuccesseurs = positionSuccesseurs;
	}

}
