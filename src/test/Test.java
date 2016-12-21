package test;
import org.apache.commons.codec.digest.DigestUtils;

public class Test {

	public static void main(String[] args) {
		System.out.println("test");
		System.out.println("Allo?");
		System.out.println("Test de hash SHA-1: "+DigestUtils.sha1Hex("test1"));	
	}

}
