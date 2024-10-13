//M. M. Kuttel 2024 mkuttel@gmail.com
// Simple Thread class to update the display of a text field
package medleySimulation;

import java.awt.Color;

import javax.swing.JLabel;

//You don't need to change this class
public class CounterDisplay  implements Runnable {
	
	private FinishCounter results;
	private JLabel win;
   private JLabel second;
	private JLabel third;
		
	CounterDisplay(JLabel w, JLabel secondLabel, JLabel thirdLabel, FinishCounter score) {
        this.win=w;
        this.results=score;
        this.second = secondLabel;
        this.third = thirdLabel;
        this.results = score;
    }
	
	public void run() { //this thread just updates the display of a text field
        while (true) {
        
        	   //test changes colour when the race is won
           	if (results.isRaceWon()) {
           		win.setForeground(Color.RED);
               win.setText("1st Place: \t Team "+ results.getWinningTeam() + "!!");
               
               // Update second and third place labels
               second.setForeground(Color.BLUE); 
               second.setText("2nd Place: \t Team " + results.getSecondTeam() +"!!");
               
               third.setForeground(Color.GREEN);
               third.setText("3rd Place: \tTeam " + results.getThirdTeam() +"!!");
               
           	}
           	else {
           		win.setForeground(Color.BLACK);
           		win.setText("------"); 
               second.setForeground(Color.BLACK);
               second.setText("------");
               third.setForeground(Color.BLACK);
               third.setText("------");
           	}	
           }
    }
}
