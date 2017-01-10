package Main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
		// TODO Auto-generated method stub

		System.out.println(string);

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

				// On récupere l'IP:Port
				String ipPort = client.getInetAddress().toString() + ":" + client.getPort();

				// On envoi la liste
				OutputStream os = client.getOutputStream();
				OutputStreamWriter osw = new OutputStreamWriter(os);
				BufferedWriter bw = new BufferedWriter(osw);

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
				bw.write(json);
				System.out.println("Message sent to the client is " + json);
				bw.flush();

				// On ajout le client à la list
				Annuaire.this.addClient(ipPort);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Annuaire(50, 8000);
	}
}
