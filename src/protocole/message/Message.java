package protocole.message;

import chord.PairInfos;

/**
 * Classe permettant de g�rer le contenu d'un message entre pair.
 *
 * @author Barnini Nicholas
 * 
 */
public class Message {

	/** Contient le type de message transmis ou re�u */
	private TypeMessage typeMessage;

	/** Emetteur du message */
	private String sender;

	/** Contenu du message si il y en a un */
	private String message;

	public Message(TypeMessage tm, String sender, String message) {
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
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getMessage() {
		return message.replace("[", "").replace("]", "");
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "[" + sender + "] " + typeMessage + " : '" + message + "'";
	}

	public PairInfos getPairInfos() {
		String ip = sender.split(":")[0];
		int port = Integer.parseInt(sender.split(":")[1]);

		return new PairInfos(ip, port);
	}

}
