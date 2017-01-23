package chord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import protocole.message.Message;
import protocole.message.TypeMessage;
import services.InfosAnnuaire;
import services.Logs;

public class ChordTest {

	Annuaire annuaire;
	ArrayList<Pair> pairs;
	static BufferedReader br;

	public ChordTest() {
		pairs = new ArrayList<Pair>();
	}

	public static void main(String[] args) {
		// On active les logs dans la console
		Logs.activer(true);

		br = new BufferedReader(new InputStreamReader(System.in));

		ChordTest ct = new ChordTest();
		try {
			ct.start();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Scénario de test
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void start() throws IOException, InterruptedException {
		// Lancement de l'annuaire
		annuaire = new Annuaire(10, InfosAnnuaire.port);

		// Création des Pairs
		for (int i = 1; i <= 15; i++) {
			pairs.add(new Pair("127.0.0.1", InfosAnnuaire.port + i));
		}

		TimeUnit.SECONDS.sleep(3);

		// Affichage des Pairs et de leurs successeurs
		for (Pair pair : pairs) {
			System.out.println(pair);
		}
		System.out.println();
		
		br.readLine();

		// Création d'un salon
		System.out.println("Le Pair 1 crée un salon..");
		pairs.get(0).joinChatRoom("MON SALON");

		br.readLine();

		TimeUnit.SECONDS.sleep(1);

		// On demande à l'annuaire la liste des salons
		ArrayList<SalonInfos> list = pairs.get(0).getChatRoomsList();

		// Affichage de la liste des salons actifs
		System.out.println("Liste des salons actifs (annuaire) :");
		for (SalonInfos salonInfos : list) {
			System.out.println(" - " + salonInfos.getNom());
		}

		System.out.println();
		// br.readLine();
		TimeUnit.SECONDS.sleep(1);
		
		br.readLine();
		
		System.out.println("Deux Pairs rejoignent le salon..");
		// Deux pairs essayent de se connecter à ce salon
		pairs.get(1).joinChatRoom("MON SALON");
		pairs.get(2).joinChatRoom("MON SALON");

		TimeUnit.SECONDS.sleep(2);

		// On regarde la liste des membres du salon
		for (SalonInfos salon : pairs.get(0).getListSalons()) {
			System.out.println("'"+salon.getNom() + "' hosté par " + salon.getInfosHost().getIpPort());

			System.out.println("	Liste des membres : ");
			for (PairInfos infos : salon.getListMembres()) {
				System.out.println(" 	 - " + infos.getIpPort());
			}
		}
		
		System.out.println();
		
		br.readLine();
		
		// Un Pair envoie un message au salon
		System.out.println("Le Pair 2 envoie un message au salon..");
		Message msgSalon = new Message(TypeMessage.MessageSalon, pairs.get(1).getInfos().getIpPort(),
				"salon=MON SALON&message=Coucou");
		pairs.get(1).sendMessage(pairs.get(0).getInfos(), msgSalon);

		System.out.println();

		// pairs.get(1).sendImage(pairs.get(0).getInfos(),
		// "C:/Users/Karl/Desktop/chat.jpg");
	}
}
