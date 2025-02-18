//M. M. Kuttel 2024 mkuttel@gmail.com
//Class representing the grid for the simulation, made up of grid blocks.

package medleySimulation;

import java.util.concurrent.CountDownLatch;

//This class represents the club as a grid of GridBlocks
public class StadiumGrid {
	private GridBlock[][] Blocks;
	private final int x; // maximum x value
	private final int y; // maximum y value
	public static int start_y; // where the starting blocks are

	private GridBlock entrance; // hard coded entrance

	public CountDownLatch latch = new CountDownLatch(10); // * Latch to synchronize race start
	private boolean teams[] = new boolean[10];

	private GridBlock startingBlocks[]; // hard coded starting blocks
	private final static int minX = 5;// minimum x dimension
	private final static int minY = 5;// minimum y dimension

	StadiumGrid(int x, int y, int nTeams, FinishCounter c) throws InterruptedException {
		if (x < minX)
			x = minX; // minimum x
		if (y < minY)
			y = minY; // minimum x
		this.x = x;
		this.y = y;
		start_y = y - 20; // start row hard-coded
		Blocks = new GridBlock[x][y]; // set up the array grid
		startingBlocks = new GridBlock[nTeams];
		this.initGrid();
		entrance = Blocks[0][y - 5];
	}

	// initialise the grid, creating all the GridBlocks, marking the starting blocks
	private void initGrid() throws InterruptedException {
		int startBIndex = 0;
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				boolean start_block = false;
				if ((i % 5 == 1) && (j == start_y)) {
					start_block = true;
				}
				Blocks[i][j] = new GridBlock(i, j, start_block);
				if (start_block) {
					this.startingBlocks[startBIndex] = Blocks[i][j];
					startBIndex++;
				}
			}
		}
	}

	// Set participation status for a team
	public void setTeam(int team, boolean b) {
		teams[team] = b;
	}

	// Get participation status for a team
	public boolean getTeam(int team) {
		return teams[team];
	}

	public int getMaxX() {
		return x;
	}

	public int getMaxY() {
		return y;
	}

	public GridBlock whereEntrance() {
		return entrance;
	}

	// is this a valid grid reference?
	public boolean inGrid(int i, int j) {
		if ((i >= x) || (j >= y) || (i < 0) || (j < 0))
			return false;
		return true;
	}

	// is this a valid grid reference?
	public boolean inStadiumArea(int i, int j) {
		return inGrid(i, j);
	}

	// Method for entering the stadium
	// * Synchronization ensures only one person can enter at a time
	public GridBlock enterStadium(PeopleLocation myLocation) throws InterruptedException {

		synchronized (entrance) {
			while (!(entrance.get(myLocation.getID()))) {
				entrance.wait();
			} // wait at entrace until entrance is free

			// Once acquired, set the person's location to the entrance
			myLocation.setLocation(entrance);
			myLocation.setInStadium(true);

			entrance.notify();// * Notify others that the entrance is free
		}
		return entrance;

	}

	// returns starting block for a team (the lane)
	public GridBlock returnStartingBlock(int team) {
		return startingBlocks[team];
	}

	// Make a one block move in a direction
	public GridBlock moveTowards(GridBlock currentBlock, int xDir, int yDir, PeopleLocation myLocation)
			throws InterruptedException { // try to move in

		int c_x = currentBlock.getX();
		int c_y = currentBlock.getY();

		int add_x = Integer.signum(xDir - c_x);// -1,0 or 1
		int add_y = Integer.signum(yDir - c_y);// -1,0 or 1

		if ((add_x == 0) && (add_y == 0)) {// not actually moving
			return currentBlock;
		}
		// restrict i and j to grid
		if (!inStadiumArea(add_x + c_x, add_y + c_y)) {
			System.out.println("Invalid move");
			// Invalid move to outside - ignore
			return currentBlock;
		}

		GridBlock newBlock;
		if (add_x != 0)
			newBlock = whichBlock(add_x + c_x, c_y); // try moving x only first
		else
			newBlock = whichBlock(add_x + c_x, add_y + c_y);// try diagonal or y

		synchronized (newBlock) {
			while ((!newBlock.get(myLocation.getID()))) {
				newBlock.wait();
			} // wait until block is free

			myLocation.setLocation(newBlock); // Once acquired, update the person's location
			synchronized (currentBlock) {
				currentBlock.release(); // * must release current block
				currentBlock.notify(); // * Notify others that the block is free
			}
			newBlock.notify(); // * Notify others that the new block is occupied
		}
		return newBlock;

	}

	// levitate to a specific block -
	public GridBlock jumpTo(GridBlock currentBlock, int x, int y, PeopleLocation myLocation)
			throws InterruptedException {
		// restrict i and j to grid
		if (!inStadiumArea(x, y)) {
			System.out.println("Invalid move");
			// Invalid move to outside - ignore
			return currentBlock;
		}

		GridBlock newBlock = whichBlock(x, y);// try diagonal or y

		// Wait until the new block is free
		synchronized (newBlock) {
			while ((!newBlock.get(myLocation.getID()))) {
				newBlock.wait();

				// Once acquired, update the person's location
				myLocation.setLocation(newBlock);
				newBlock.notifyAll();

			} // wait until block is free

			synchronized (currentBlock) {
				currentBlock.release(); // * Release the previous block

				currentBlock.notifyAll(); // * Notify all threads waiting on the old block
			}
			return newBlock;
		}
	}

	// x and y actually correspond to the grid pos, but this is for generality.
	public GridBlock whichBlock(int xPos, int yPos) {
		if (inGrid(xPos, yPos)) {
			return Blocks[xPos][yPos];
		}
		System.out.println("block " + xPos + " " + yPos + "  not found");
		return null;
	}
}
