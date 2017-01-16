package chord;

public class SalonInfos {

	/** Nom du salon */
	private String nom;

	/** Infos de l'Host du salon */
	private PairInfos infosHost;

	public SalonInfos(String nom, PairInfos infosHost) {
		this.nom = nom;
		this.infosHost = infosHost;
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

}
