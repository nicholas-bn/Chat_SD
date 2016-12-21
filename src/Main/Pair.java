package Main;

import org.apache.commons.codec.digest.DigestUtils;

import sun.applet.Main;

public class Pair {
	String identifiantNonHache;
	String identifiantHache;

	public Pair(String identifiantNonHache) {
		this.identifiantNonHache = identifiantNonHache;
	}

	public String getHashSHA1(String toHash) {
		return DigestUtils.sha1Hex(toHash);
	}

	public void HasheIdentifiant(String identifiantNonHache) {
		getHashSHA1(identifiantNonHache);
	}

	public static void main(String[] args) {
		Pair pair1 = new Pair("Michel");
		System.out.println(pair1.getHashSHA1(pair1.identifiantNonHache));
	}
}
