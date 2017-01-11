package Main;

import java.util.ArrayList;

public class ChordTest {
	
	Annuaire a;
	ArrayList<Pair> arrayListPair;
	
	public ChordTest() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		ChordTest ct = new ChordTest();
		ct.start();
	}

	private void start() {
		// TODO Auto-generated method stub
		a = new Annuaire(10, 8000);
		arrayListPair = new ArrayList<Pair>();
		for(int i = 1; i<=10 ; i++){
			arrayListPair.add(new Pair("127.0.0.1", 8000+i));
		}
	}
}
