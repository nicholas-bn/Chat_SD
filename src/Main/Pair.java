package Main;

import org.apache.commons.codec.digest.DigestUtils;

import sun.applet.Main;

public class Pair {
	private int numero;
	private String ip;
	private int port;
	private String identifiant;

	public Pair(int numero,String ip, int port) {
		this.ip = ip;
		this.port = port;
		identifiant = (this.ip + ":" + this.port);
	}

	public void HashachageId(String toHash) {
		identifiant = DigestUtils.sha1Hex(toHash);
	}

	public boolean CompareID(String idReferent) {
		if (idReferent.equals(this.identifiant)) {
			return true;
		}
		return false;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getIdentifiant() {
		return identifiant;
	}

	public void setIdentifiant(String identifiant) {
		this.identifiant = identifiant;
	}

	public static void main(String[] args) {
		// On cree un ID referent qui est egal a l'ID du 1er pair
		String idReferent = "0210df6cd869c217af4ac03664c330d9c9841926";
		// On cree un autre ID qui est different
		String autreID = "4c38d8705ff98c8c1e89b6694b1281e22f63ee084";
		Pair pair1 = new Pair(1,"127.01.01", 88);
		//On observe l'identifiant cree avant hashage
		System.out.println("Identifiant avant hashage: "+ pair1.getIdentifiant());
		// On hache l'identifiant
		pair1.HashachageId(pair1.identifiant);
		System.out.println("Identifiant apres hashage: "+ pair1.getIdentifiant());
		System.out.println(pair1.CompareID(idReferent));
		System.out.println(pair1.CompareID(autreID));
	}

	public int getNumero() {
		return numero;
	}

	public void setNumero(int numero) {
		this.numero = numero;
	}
}
