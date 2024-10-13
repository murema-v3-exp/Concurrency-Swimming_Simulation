//M. M. Kuttel 2024 mkuttel@gmail.com
// GridBlock class to represent a block in the grid.
// only one thread at a time "owns" a GridBlock - this must be enforced

package medleySimulation;

import java.util.concurrent.atomic.AtomicInteger;

public class GridBlock {

	private AtomicInteger isOccupied;

	private final boolean isStart; // is this a starting block?
	private int[] coords; // the coordinate of the block.

	GridBlock(boolean startBlock) throws InterruptedException {
		isStart = startBlock;
		isOccupied = new AtomicInteger(-1);
	}

	GridBlock(int x, int y, boolean startBlock) throws InterruptedException {
		this(startBlock);
		coords = new int[] { x, y };
	}

	public synchronized int getX() {
		return coords[0];
	}

	public synchronized int getY() {
		return coords[1];
	}

	// Get a block
	public synchronized boolean get(int threadID) throws InterruptedException {
		if (isOccupied.get() == threadID)
			return true; // * thread Already in this block
		if (isOccupied.get() >= 0)
			return false; // * space is occupied
		isOccupied.set(threadID); // * set ID to thread that had block
		return true;
	}

	// release a block
	public synchronized void release() {
		isOccupied.set(-1); // * Mark the block as unoccupied
		notifyAll(); // * Notify all waiting threads that the block is now available
	}

	// * is a block already occupied?
	public synchronized boolean occupied() {
		return (isOccupied.get() != -1);

	}

	// * is a start block
	public synchronized boolean isStart() {
		return isStart;
	}

}
