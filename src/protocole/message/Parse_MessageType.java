package protocole.message;

import java.util.HashMap;

/**
 * Classe permettant de parser des messages
 * 
 * @author Barnini
 *
 */
public abstract class Parse_MessageType {

	/**
	 * Parse le message et renvoi un hashmap avec ses
	 * attributs
	 * 
	 * @param message
	 * @return
	 */
	public static HashMap<String, String> parseMessageInHashMap(String message) {
		HashMap<String, String> retour = new HashMap<String, String>();

		String[] parse = message.split("&");

		for (String attrVal : parse) {
			String[] parseAttrVal = attrVal.split("=");
			retour.put(parseAttrVal[0], parseAttrVal[1]);
		}

		return retour;
	}

	public static void main(String[] args) {
		String messageAParser = "attr1=val1&attr2=val2&attr3=val3";
		HashMap<String, String> hashmap = parseMessageInHashMap(messageAParser);
		System.out.println("attr1: "+hashmap.get("attr1"));
		System.out.println("attr2: "+hashmap.get("attr2"));
		System.out.println("attr3: "+hashmap.get("attr3"));
	}
}
