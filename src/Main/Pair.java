package Main;

import org.apache.commons.codec.digest.DigestUtils;

import sun.applet.Main;

public class Pair {
	String ip;
	int port;
	String identifiant;

	public Pair(String ip, int port) {
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
		String idReferent = "e98b1de3c972ca554569b28357c407c807690e13";
		// On cree un autre ID qui est different
		String autreID = "4c38d8705ff98c8c1e89b6694b1281e22f63ee084";
		Pair pair1 = new Pair("1270101", 88);
		//On observe l'identifiant cree avant hashage
		System.out.println("Identifiant avant hashage: "+ pair1.getIdentifiant());
		// On hache l'identifiant
		pair1.HashachageId(pair1.identifiant);
		System.out.println("Identifiant apres hashage: "+ pair1.getIdentifiant());
		System.out.println(pair1.CompareID(idReferent));
		System.out.println(pair1.CompareID(autreID));
	}
}
