package Main;

/**
 * @author Barnini Nicholas
 * 
 * Classe permettant de gérer le contenu d'un message entre pair.
 *
 */
public class Message {
	
	/** Contient le type de message transmis ou reçu */
	private TypeMessage typeMessage;
	
	/** Emetteur du message */
	private String Sender;
	
	/** Contenu du message si il y en a un */
	private String message;
	
	public Message(TypeMessage tm, String sender, String message){
		this.setTypeMessage(tm);
		this.setSender(sender);
		this.setMessage(message);
	}

	public TypeMessage getTypeMessage() {
		return typeMessage;
	}

	public void setTypeMessage(TypeMessage typeMessage) {
		this.typeMessage = typeMessage;
	}

	public String getSender() {
		return Sender;
	}

	public void setSender(String sender) {
		Sender = sender;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
