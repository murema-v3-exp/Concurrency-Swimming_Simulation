//M. M. Kuttel 2024 mkuttel@gmail.com
//Class to represent a swim team - which has four swimmers
package medleySimulation;

import medleySimulation.Swimmer.SwimStroke;

public class SwimTeam extends Thread {

	public static StadiumGrid stadium; // shared stadium grid
	private Swimmer[] swimmers; // array of swimmers in the team
	private int teamNo; // team number

	public static final int sizeOfTeam = 4; // size of each team is fixed to 4 swimmers

	SwimTeam(int ID, FinishCounter finish, PeopleLocation[] locArr) {
		this.teamNo = ID;

		swimmers = new Swimmer[sizeOfTeam];
		SwimStroke[] strokes = SwimStroke.values(); // Get all enum constants
		stadium.returnStartingBlock(ID);

		for (int i = teamNo * sizeOfTeam, s = 0; i < ((teamNo + 1) * sizeOfTeam); i++, s++) { // initialise swimmers in
																								// team
			locArr[i] = new PeopleLocation(i, strokes[s].getColour());
			int speed = (int) (Math.random() * (3) + 30); // range of speeds
			swimmers[s] = new Swimmer(i, teamNo, locArr[i], finish, speed, strokes[s]); // create new swimmer with their
																						// attributes
		}
	}

	// Run method (called when SwimTeam thread starts)
	public void run() {
		try {
			for (int s = 0; s < sizeOfTeam; s++) { // start swimmer threads

				// Synchronize to ensure each swimmer starts properly before waiting
				synchronized (swimmers[s]) {
					swimmers[s].start(); // start swimmer thread
					swimmers[s].wait(); // wait for the swimmer to signal they're ready
				}
			}

			for (int s = 0; s < sizeOfTeam; s++)
				swimmers[s].join(); // don't really need to do this;

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
