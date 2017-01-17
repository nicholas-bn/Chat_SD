package chord;

import java.util.ArrayList;

public class SalonInfos {

	/** Nom du salon */
	private String nom;

	/** Infos de l'Host du salon */
	private PairInfos infosHost;

	/** Liste des membrex présents dans le salon */
	private ArrayList<PairInfos> listMembres;

	public SalonInfos(String nom, PairInfos infosHost) {
		this.nom = nom;
		this.infosHost = infosHost;

		listMembres = new ArrayList<>();
	}

	/**
	 * Retourne le nom du salon
	 * 
	 * @return
	 */
	public String getNom() {
		return nom;
	}

	/**
	 * Retourne les infos de l'host du salon
	 * 
	 * @return
	 */
	public PairInfos getInfosHost() {
		return infosHost;
	}

	/**
	 * Permet d'ajouter un nouveau membre <br>
	 * (Uniquement utilisé par le Pair host)
	 * 
	 * @param infosPair
	 */
	public void addMembre(PairInfos infosPair) {

		listMembres.add(infosPair);

	}

	public boolean isHost(PairInfos infosPair) {

		if (infosHost.equals(infosPair)) {
			return true;
		} else {
			return false;
		}
	}

	public ArrayList<PairInfos> getListMembres() {

		return listMembres;
	}

}
