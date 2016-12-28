package Main;

/**
 * @author Barnini Nicholas
 * 
 * Classe abstraite permettant de convertir un objet Message en JSON, et vice-versa. 
 * 
 */
public abstract class Convert_Message {

	/**
	 * Méthode renvoyant un objet message rempli vià le JSON 
	 * 
	 * @param String - formaté en JSON
	 * @return Message
	 */
	public Message jsonToMessage(String json){
		return null;
	}
	
	/**
	 * Méthode renvoyant un String au format JSON grâce à un objet Message
	 * 
	 * @param Message
	 * @return String - formaté en JSON
	 */
	public String messageToJson(Message message){
		return null;
	}
	
}
