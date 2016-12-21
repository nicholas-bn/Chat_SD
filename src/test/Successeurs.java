package test;

import java.util.ArrayList;

public class Successeurs {

	public static ArrayList<Integer> findSuccessors(int id) {
		// Nombre de pairs dans l'anneau
		int nbMax = 10;

		ArrayList<Integer> listSuccessors = new ArrayList<>();

		for (int i = 0; i < nbMax; i++) {
			// L'id du successeur
			int idSuccessor = (int) (id + Math.pow(2, i));

			if (idSuccessor >= nbMax) {
				break;
			}

			// Ajout de l'id à la liste des successeurs
			listSuccessors.add(idSuccessor);
		}

		return listSuccessors;
	}

	public static int findClosestSuccessor(int idSource, int idDest) {
		// Liste de tous les successeurs :
		ArrayList<Integer> listSuccessors = findSuccessors(idSource);

		// Si l'id destination se trouve dans la liste des successeurs
		if (listSuccessors.contains(idDest)) {
			return idDest;
		}

		int idProche = idSource;

		// On cherche le plus proche :
		for (int id : listSuccessors) {
			// Si l'id du successeur est plus proche (et inférieur à l'id dest)
			if (id > idProche && id < idDest) {
				idProche = id;
			}
		}
		// L'id du pair le plus proche de l'id destination
		return idProche;
	}

	public static void main(String[] args) {
		System.out.println("0 : " + findSuccessors(0));
		System.out.println("1 : " + findSuccessors(1));
		System.out.println("2 : " + findSuccessors(2));
		System.out.println("3 : " + findSuccessors(3));
		System.out.println("6 : " + findSuccessors(6));
		System.out.println("9 : " + findSuccessors(9) + "\n");

		//

		System.out.println(findClosestSuccessor(0, 7));

	}

}
