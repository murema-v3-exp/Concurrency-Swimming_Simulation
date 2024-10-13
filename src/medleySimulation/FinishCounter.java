// Simple class to record when someone has crossed the line first and wins
package medleySimulation;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.*;


public class FinishCounter {
	private AtomicBoolean firstAcrossLine; //flag
   
   private final List<Integer> top3;
   
   
	
	FinishCounter() { 
		firstAcrossLine= new AtomicBoolean(true);    //*v no-one has won at start
      top3 = new ArrayList<>(3);                        // Initialize the top 3 teams array
	}
	
   
   // This is called by a swimmer when they touch the finish line
   public synchronized void finishRace(int swimmer,int team) {
        if (top3.size() < 3) { // Only store up to the top 3 teams
            top3.add(team);
    
            // If all places are filled, set the flag to false
            if (top3.size() == 3) {
                firstAcrossLine.set(false);
            }
        }
   }
      
   
	//Has race been won?
	public boolean isRaceWon() {
		return !firstAcrossLine.get();   // * Return true if the flag is false, indicating the race is won
	}
   

	public int getWinner() { 
      return top3.get(0); 
   }
	
	public int getWinningTeam() { 
      return top3.get(0);
   }
   
       // Get the second team
   public int getSecondTeam() {
        return top3.get(1); // The team that finished second
   }

    // Get the third team
   public int getThirdTeam() {
        return top3.get(2); // The team that finished third
   }
   
   public List<Integer> getTopTeams () {
      return top3;
   }
   
}
