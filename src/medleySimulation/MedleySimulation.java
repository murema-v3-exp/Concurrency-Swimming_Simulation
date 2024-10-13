//M. M. Kuttel 2024 mkuttel@gmail.com
// MedleySimulation main class, starts all threads
package medleySimulation;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import java.io.IOException;

import java.util.*;

public class MedleySimulation {
   static final int numTeams = 10;

   static int frameX = 300; // frame width
   static int frameY = 600; // frame height
   static int yLimit = 400;
   static int max = 5;

   static int gridX = 50; // number of x grid points
   static int gridY = 120; // number of y grid points

   static SwimTeam[] teams; // array for team threads
   static PeopleLocation[] peopleLocations; // array to keep track of where people are
   static StadiumView stadiumView; // threaded panel to display stadium
   static StadiumGrid stadiumGrid; // stadium on a discrete grid

   static FinishCounter finishLine; // records who won
   static CounterDisplay counterDisplay; // threaded display of counter

   static final AtomicBoolean start = new AtomicBoolean(false); // * Atomic boolean to signal start of the simulation

   private static int betAmount = 0; // User's bet amount
   private static int chosenTeam = -1; // Team the user bets on

   private static JPanel bettingPanel; // Add this line

   // Method to setup all the elements of the GUI
   public static void setupGUI(int frameX, int frameY) {
      // Frame initialize and dimensions
      JFrame frame = new JFrame("Swim medley relay animation");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(frameX, frameY);

      JPanel g = new JPanel();
      g.setLayout(new BoxLayout(g, BoxLayout.Y_AXIS));
      g.setSize(frameX, frameY);

      stadiumView = new StadiumView(peopleLocations, stadiumGrid);
      stadiumView.setSize(frameX, frameY);
      g.add(stadiumView);

      // * Text panel for race results
      JPanel txt = new JPanel();
      txt.setLayout(new BoxLayout(txt, BoxLayout.Y_AXIS));
      JLabel winnerLabel = new JLabel("Waiting for results...");
      JLabel secondPlaceLabel = new JLabel("Second place: TBD");
      JLabel thirdPlaceLabel = new JLabel("Third place: TBD");

      txt.add(winnerLabel);
      txt.add(secondPlaceLabel);
      txt.add(thirdPlaceLabel);
      g.add(txt);

      // Add start and exit buttons
      JPanel b = new JPanel();
      b.setLayout(new BoxLayout(b, BoxLayout.LINE_AXIS));

      JButton startB = new JButton("Start");

      // add the listener to the jbutton to handle the "pressed" event
      startB.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {

            synchronized (start) {
               start.set(true); // * Set the start signal to true
               start.notifyAll(); // * Notify all threads waiting on the start object

               // Remove the bettingPanel
               g.remove(bettingPanel);
               g.revalidate(); // Revalidate the panel to apply the changes
               g.repaint(); // Repaint the panel to update the UI
            }

         }
      });

      JButton endB = new JButton("Quit");
      // add the listener to the jbutton to handle the "pressed" event
      endB.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            System.exit(0);
         }
      });

      // * Betting Components
      bettingPanel = new JPanel();
      bettingPanel.setLayout(new BoxLayout(bettingPanel, BoxLayout.Y_AXIS));

      JLabel teamLabel = new JLabel("Enter the team number (0-" + (numTeams - 1) + "):");
      JTextField teamField = new JTextField(5);
      JLabel betLabel = new JLabel("Enter your bet amount:");
      JTextField betField = new JTextField(5);
      JButton betButton = new JButton("Place Bet");

      bettingPanel.add(teamLabel);
      bettingPanel.add(teamField);
      bettingPanel.add(betLabel);
      bettingPanel.add(betField);
      bettingPanel.add(betButton);

      // Add action listener for the betting button
      betButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            try {
               chosenTeam = Integer.parseInt(teamField.getText());
               betAmount = Integer.parseInt(betField.getText());
               if (chosenTeam < 0 || chosenTeam >= numTeams || betAmount <= 0) {
                  JOptionPane.showMessageDialog(frame, "Invalid team number or bet amount.");
               } else {
                  JOptionPane.showMessageDialog(frame, "Bet placed: Team " + chosenTeam + " with amount " + betAmount);
               }
            } catch (NumberFormatException ex) {
               JOptionPane.showMessageDialog(frame, "Please enter valid numbers.");
            }
         }
      });

      b.add(startB);
      b.add(endB);
      g.add(b);
      g.add(bettingPanel);

      frame.setLocationRelativeTo(null); // Center window on screen.
      frame.add(g); // add contents to window
      frame.setContentPane(g);
      frame.setVisible(true);

      // Pass labels to CounterDisplay to update them dynamically
      counterDisplay = new CounterDisplay(winnerLabel, secondPlaceLabel, thirdPlaceLabel, finishLine);
      Thread resultsThread = new Thread(counterDisplay);
      resultsThread.start();

   }

   // Method to place a bet
   private static void placeBet() {
      Scanner scanner = new Scanner(System.in);
      System.out.println("Enter the team number you want to bet on (0-" + (numTeams - 1) + "): ");
      chosenTeam = scanner.nextInt();
      System.out.println("Enter your bet amount: ");
      betAmount = scanner.nextInt();
   }

   private static void notifyUser() {
      List<Integer> winners = finishLine.getTopTeams();

      String message;
      double winnings = 0.0;

      if (chosenTeam == -1) {
         message = "No bet was placed, what if you were going to win!!! Never be afraid to take risks.";
      }

      int teamPosition = winners.indexOf(chosenTeam);
      switch (teamPosition) {
         case 0:
            winnings = betAmount * 3.0; // winnings for 1st place
            message = "Congratulations! Your chosen team " + chosenTeam + " won. You win " + winnings + "!";
            break;
         case 1:
            winnings = betAmount * 1.8; // winnings for 2nd place
            message = "Your team " + chosenTeam
                  + " did not win, but it is not the end of the world, they got 2nd place and you won " + winnings
                  + "!";
            break;
         case 2:
            winnings = betAmount * 1.3; // winnings for 3rd place
            message = "Your team " + chosenTeam
                  + " did not win, but it is not the end of the world, they got 3rd place and you won " + winnings
                  + "!";
            break;
         default:
            message = "Sorry, your chosen team " + chosenTeam + " did not win. Better luck next time!";
            break;
      }

      JOptionPane.showMessageDialog(null, message);
   }

   // Main method - starts it all
   public static void main(String[] args) throws InterruptedException {

      // Initialize the finish line counter
      finishLine = new FinishCounter(); // counters for people inside and outside club

      stadiumGrid = new StadiumGrid(gridX, gridY, numTeams, finishLine); // setup stadium with size
      SwimTeam.stadium = stadiumGrid; // grid shared with class
      Swimmer.stadium = stadiumGrid; // grid shared with class
      peopleLocations = new PeopleLocation[numTeams * SwimTeam.sizeOfTeam]; // four swimmers per team

      teams = new SwimTeam[numTeams]; // Array to hold team threads

      for (int i = 0; i < numTeams; i++) {
         teams[i] = new SwimTeam(i, finishLine, peopleLocations);
      }
      setupGUI(frameX, frameY); // Start Panel thread - for drawing animation

      // placeBet();

      // * Wait for the start signal before proceeding
      synchronized (start) {
         while (!start.get()) {
            try {
               start.wait(); // * Wait until notified to start the simulation
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
      }

      // start viewer thread
      Thread view = new Thread(stadiumView);
      view.start();

      // Start counter thread - for updating results
      Thread results = new Thread(counterDisplay);
      results.start();

      // start teams, which start swimmers.
      for (int i = 0; i < numTeams; i++) {
         teams[i].start();
      }

      // Wait for the race to finish
      while (!finishLine.isRaceWon()) {
         Thread.sleep(1000);
      }
      notifyUser(); // Add this to notify the user of the results
   }
}
