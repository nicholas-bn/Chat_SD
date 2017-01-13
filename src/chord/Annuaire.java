package chord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import protocole.message.Convert_Message;
import protocole.message.Message;
import protocole.message.TypeMessage;
import services.Logs;

public class Annuaire {

	public ArrayList<PairInfos> listPairRecent;
	public int maxPairSvg;
	public int port;

	public Annuaire(int max, int port) {
		this.maxPairSvg = max;
		this.listPairRecent = new ArrayList<PairInfos>();
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

	private void addClient(PairInfos pairInfos) {
		// Si on a atteint la taille max, on supprime le plus ancien
		if (this.listPairRecent.size() > this.maxPairSvg) {
			this.listPairRecent.remove(0);
		}
		this.listPairRecent.add(pairInfos);
	}

	private void acceptNewClient() {
		try {
			@SuppressWarnings("resource")
			// On écoute sur le port donné
			ServerSocket ecoute = new ServerSocket(port);
			while (true) {
				// On attend une connexion client
				Socket client = ecoute.accept();

				// Logs.print("Un client demande la liste");

				InputStream is = client.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String msgString = br.readLine();
				
				Message messageClient = Convert_Message.jsonToMessage(msgString);

				// On récupere les infos du dest
				PairInfos destInfos = messageClient.getPairInfos();

				// On concatene le message
				String message = "";
				for (int i = 0; i < listPairRecent.size(); i++) {
					message += listPairRecent.get(i).getIpPort();
					if (i < listPairRecent.size() - 1)
						message += ";";
				}

				// Création du message
				Message m = new Message(TypeMessage.AjoutPair, destInfos.getIpPort(), message);
				String json = Convert_Message.messageToJson(m);

				// Buffer de sortie
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);

				// Envoi du message au client
				out.println(json);

				// On ajout le client à la liste
				addClient(destInfos);

				client.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
