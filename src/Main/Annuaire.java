package Main;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import services.Logs;

public class Annuaire {

	public ArrayList<String> listPairRecent;
	public int maxPairSvg;
	public int port;

	public Annuaire(int max, int port) {
		this.maxPairSvg = max;
		this.listPairRecent = new ArrayList<String>();
		this.port = port;

		listen();
	}

	public void listen() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				acceptNewClient();
			}

		}).start();
	}

	private void addClient(String string) {
		// Si on a atteint la taille max, on supprime le plus ancien
		if (this.listPairRecent.size() > this.maxPairSvg) {
			this.listPairRecent.remove(0);
		}
		this.listPairRecent.add(string);
	}

	private void acceptNewClient() {
		try {
			@SuppressWarnings("resource")
			// On écoute sur le port donné
			ServerSocket ecoute = new ServerSocket(port);
			while (true) {
				// On attend une connexion client
				Socket client = ecoute.accept();

				Logs.print("Un client demande la liste");

				// On récupere l'IP:Port
				String ipPort = client.getInetAddress().getHostAddress() + ":" + client.getPort();

				// On concatene le message
				String message = "";
				for (int i = 0; i < Annuaire.this.listPairRecent.size(); i++) {
					message += Annuaire.this.listPairRecent;
					if (i < Annuaire.this.listPairRecent.size() - 1)
						message += ";";
				}

				// Création du message
				Message m = new Message(TypeMessage.AjoutPair, ipPort, message);
				String json = Convert_Message.messageToJson(m);

				// Buffer de sortie
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);

				// Envoi du message au client
				out.println(json);

				// On ajout le client à la list
				addClient(ipPort);

				client.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
