package protocole.message;

/**
 * Enumération permettant de différiencer les différents types de messages.
 * 
 * @author Barnini Nicholas
 * 
 */
public enum TypeMessage {

	// Ajout
	AjoutPair, SuppressionPair,

	// Protocole de maintenance
	Reparation, DemandeClef, CheckConnexion, ModificationSuccesseurs, GetSuccesseurs,

	// Salon
	GetListeSalons, JoinSalon,

	// Message
	MessageChat, MessageSalon, Image;
}
