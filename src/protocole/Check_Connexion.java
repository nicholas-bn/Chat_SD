package protocole;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

import Main.Convert_Message;
import Main.Message;
import Main.TypeMessage;

/**
 * Classe threader qui permet de tester la connexion à un pair distant
 * 
 * @author Barnini
 *
 */
public class Check_Connexion implements Runnable {

	private Socket socketSuccesseur;

	public Check_Connexion(Socket socket) {
		// TODO Auto-generated constructor stub
		this.setSocketSuccesseur(socket);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		// On assemble l'IP du destinataire pour que le receveur sache qui lui parle
		String sender = this.getSocketSuccesseur().getInetAddress().getHostAddress() + ":"
				+ this.getSocketSuccesseur().getPort();
		
		// On créé un objet Message de type CheckConnexion
		Message message = new Message(TypeMessage.CheckConnexion, sender, "chemin=aller");

		PrintWriter out;
		try {
			
			// Buffer de sortie
			out = new PrintWriter(this.getSocketSuccesseur().getOutputStream(), true);
			
			// Envoi du message au client
			out.println(Convert_Message.messageToJson(message));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Socket getSocketSuccesseur() {
		return socketSuccesseur;
	}

	public void setSocketSuccesseur(Socket socketSuccesseur) {
		this.socketSuccesseur = socketSuccesseur;
	}
}
