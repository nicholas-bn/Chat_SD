package Main;
import org.apache.commons.codec.digest.DigestUtils;

import sun.applet.Main;
public class Pair {
	
	 String identifiantHache;
	
	public Pair(String identifiantNonHache)
	{
		this.identifiantHache = DigestUtils.sha1Hex(identifiantNonHache);
	}

	public static void main(String[] args) {
		Pair pair1 = new Pair("Michel");
		
		System.out.println(pair1.toString());
	}
}
