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

	public static boolean CompareID(String idReferent, String identifant) {
		if (idReferent.equals(identifant)) {
			return true;
		}
		return false;
	}

	public static void main(String[] args) {
		String idReferent = "4c38d8705ff98c8c1e89b6694b128122f63ee084";
		Pair pair1 = new Pair("Michel");
		pair1.HashachageId(pair1.identifiant);
		CompareID(idReferent, pair1.identifiant);
	}
}
