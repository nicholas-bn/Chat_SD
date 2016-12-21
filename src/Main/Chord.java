package Main;

import java.util.ArrayList;

public class Chord {

	public int sizeRing;
	public ArrayList<Pair> listPair;

	public Chord(int size) {
		this.sizeRing = size;
		this.listPair = new ArrayList<Pair>();
	}

	public void initialize() {
		for (int i = 0; i < this.sizeRing; i++) {
			this.listPair.add(new Pair(""));
		}
	}

	public static void main(String[] args) {
		Chord c = new Chord(16);
		c.initialize();
	}
}
