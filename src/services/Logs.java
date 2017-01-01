package services;

public abstract class Logs {

	/** Indique si les logs dans la consoles sont activés. De base à false */
	public static boolean logsActive = false;

	/**
	 * Permet d'activer les logs dans la console
	 * 
	 * @param active
	 */
	public static void activer(boolean active) {
		logsActive = active;
	}

	/**
	 * Affiche le texte dans la console
	 * 
	 * @param text
	 */
	public static void print(String text) {
		// Si les logs dans la console sont activés
		if (logsActive) {
			System.out.println(text);
		}
	}
}
