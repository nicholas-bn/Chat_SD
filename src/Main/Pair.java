package Main;

import org.apache.commons.codec.digest.DigestUtils;

import sun.applet.Main;

public class Pair {
	String identifiant;

	public Pair(String identifiantNonHache) {
		this.identifiant = identifiantNonHache;
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

	public static void main(String[] args) {
		// On cree un ID referent qui est egal a l'ID du 1er pair
		String idReferent = "4c38d8705ff98c8c1e89b6694b128122f63ee084";
		// On cree un autre ID qui est different
		String autreID = "4c38d8705ff98c8c1e89b6694b1281e22f63ee084";
		Pair pair1 = new Pair("Michel");
		pair1.HashachageId(pair1.identifiant);
		System.out.println(pair1.CompareID(idReferent));
		System.out.println(pair1.CompareID(autreID));
	}
}
