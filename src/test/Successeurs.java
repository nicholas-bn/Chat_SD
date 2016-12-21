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

	public static void main(String[] args) {
		System.out.println("0 : " + findSuccessors(0));
		System.out.println("1 : " + findSuccessors(1));
		System.out.println("2 : " + findSuccessors(2));
		System.out.println("3 : " + findSuccessors(3));
		System.out.println("6 : " + findSuccessors(6));
		System.out.println("9 : " + findSuccessors(9));
	}

}
