package chord;

import java.math.BigInteger;

import org.apache.commons.codec.digest.DigestUtils;

public class PairInfos {

	/** Adresse IP du pair */
	public String ip;

	/** Port du pair */
	public int port;

	/** Cle du pair */
	public long cle;

	public PairInfos(String pairIP, int pairPORT) {
		ip = pairIP;
		port = pairPORT;

		// Récupération de la clé du Pair
		cle = getIdFromIpPort(ip, port);
	}

	public PairInfos(String ipPort) {
		ip = ipPort.split(":")[0];
		port = Integer.parseInt(ipPort.split(":")[1]);

		// Récupération de la clé du Pair
		cle = getIdFromIpPort(ip, port);
	}

	/**
	 * Méthode qui permet de Hash un String en SHA-1
	 * 
	 * @param toHash
	 * @return
	 */
	private String HashId(String toHash) {
		return DigestUtils.sha1Hex(toHash);
	}

	/**
	 * Méthode qui retourne une clé en fonction d'un hash donné
	 * 
	 * @param hash
	 * @return
	 */
	private Long getIdFromHash(String hash) {
		// Identifiant en fonction du hash
		BigInteger value = new BigInteger(hash.substring(0, 5), 16);
		return value.longValue();
	}

	/**
	 * Méthode qui retourne une clé en fonction d'un ip et d'un port donné
	 * 
	 * @param ip
	 * @param port
	 * @return
	 */
	private Long getIdFromIpPort(String ip, int port) {
		// Construction de l'identifiant du pair (id:port)
		String identifiant = (ip + ":" + port);

		// On Hash l'identifiant
		String hash = HashId(identifiant);

		return getIdFromHash(hash);
	}

	public String getIpPort() {
		return ip + ":" + port;
	}

}
