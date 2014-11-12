package Serveur;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import Model.Deck;
import Model.Jeu;

public class Croupier extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton Deal;
	private Deck newdeck;
	private JTextArea displayArea;
	private ExecutorService executor;
	private ServerSocket server;
	private SockServer[] sockServer;
	private int counter = 1;
	private String dcard1, dcard2;
	private ArrayList<Jeu> players;
	private Jeu dcards;
	private int playersleft;
 

	public Croupier() {

		super("Croupier");

		sockServer = new SockServer[100]; // serveur threads
		executor = Executors.newFixedThreadPool(100); // pool de threads

		Deal = new JButton("Distribuer cartes !");

		Deal.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				
				players = new ArrayList();
				Deal.setEnabled(true);
				newdeck = new Deck();
				DealCards();
				displayMessage("\n\nCartes Distribuées\n\n");

			}
		});

		add(Deal, BorderLayout.SOUTH);
		displayArea = new JTextArea();
		displayArea.setEditable(false);
		add(new JScrollPane(displayArea), BorderLayout.CENTER);

		setBounds(500, 300, 100, 100);
		setSize(300, 300);
		setVisible(true);
	}

	public void runDeal() {
		try {
			server = new ServerSocket(23555, 100);

			while (true) {
				try {

					sockServer[counter] = new SockServer(counter);
					sockServer[counter].waitForConnection();
					executor.execute(sockServer[counter]);

				} catch (EOFException eofException) {
					displayMessage("\nConnexion au serveur terminée !");
				} finally {
					++counter;
				}
			}
		} catch (IOException ioException) {
		}
	}

	private void displayMessage(final String messageToDisplay) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				displayArea.append(messageToDisplay);
			}
		});
	}

	private void DealCards() {

		try {
			
			playersleft = counter - 1;
			newdeck.shuffle();
			dcard1 = newdeck.dealCard();
			dcard2 = newdeck.dealCard();
			displayMessage("\n\n" + dcard1 + " " + dcard2);

			for (int i = 1; i <= counter; i++) {
				String c1, c2;
				c1 = newdeck.dealCard();
				c2 = newdeck.dealCard();
				Jeu p = new Jeu(c1, c2);
				players.add(p);
				sockServer[i].sendData("Vous avez :\n" + c1 + " " + c2);
				sockServer[i].sendData("Votre total est : " + p.GetCardTotal());

			}
			
		} catch (NullPointerException n) {
		}
	}

	private void Results() {

		try {
			for (int i = 1; i <= counter; i++) {
				sockServer[i].sendData("Croupier a : " + dcards.GetCardTotal());

				if ((dcards.GetCardTotal() <= 21)
						&& (players.get(i - 1).GetCardTotal() <= 21)) {

					if (dcards.GetCardTotal() > players.get(i - 1)
							.GetCardTotal()) {
						sockServer[i].sendData("\n Vous avez perdu !");
					}

					if (dcards.GetCardTotal() < players.get(i - 1)
							.GetCardTotal()) {
						sockServer[i].sendData("\n Vous avez gagné !");
					}

					if (dcards.GetCardTotal() == players.get(i - 1)
							.GetCardTotal()) {
						sockServer[i].sendData("\n Egalité !");
					}

				}

				if (dcards.CheckBust()) {

					if (players.get(i - 1).CheckBust()) {
						sockServer[i].sendData("\n Egalité!");
					}
					if (players.get(i - 1).GetCardTotal() <= 21) {
						sockServer[i].sendData("\n Vous avez gagné !");
					}
				}

				if (players.get(i - 1).CheckBust()
						&& dcards.GetCardTotal() <= 21) {
					sockServer[i].sendData("\n Vous avez perdu !");
				}
			}

		} catch (NullPointerException n) {
		}
	}

	private class SockServer implements Runnable {
		private ObjectOutputStream output;
		private ObjectInputStream input;
		private Socket connection;
		private int myConID;

		public SockServer(int counterIn) {
			myConID = counterIn;
		}

		public void run() {
			try {
				try {
					getStreams();
					processConnection();

				} // end try
				catch (EOFException eofException) {
					displayMessage("\nServeur" + myConID
							+ " connexion terminée");
				} finally {
					closeConnection();
				}
			} catch (IOException ioException) {
			}
		}

		private void waitForConnection() throws IOException {

			displayMessage("En attente de connexion" + myConID + "\n");
			connection = server.accept();
			displayMessage("connexion " + myConID + " reçue de : "
					+ connection.getInetAddress().getHostName());
		}

		private void getStreams() throws IOException {

			output = new ObjectOutputStream(connection.getOutputStream());
			output.flush();

			input = new ObjectInputStream(connection.getInputStream());

			displayMessage("\nGot I/O streams\n");
		}

		private void processConnection() throws IOException {
			String message = "Connexion  " + myConID + " réussie";
			sendData(message);

			do {
				try {
					if (message.contains("Jouer") ) {
						
						cardhit();
						
					}

					if (message.contains("Stop")) {
						this.sendData("Merci de patienter ");
						playersleft--;
						CheckDone();
					}
					 if (message.contains("Rejouer")){
						 
						players.clear();
					} 
					

					message = (String) input.readObject();

				} catch (ClassNotFoundException classNotFoundException) {
					displayMessage("\nobject inconnu");
				}

			} while (!message.equals("CLIENT>>> TERMINATE"));
		}

		private void DealerGo() {
			dcards = new Jeu(dcard1, dcard2);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (dcards.GetCardTotal() < 16) {
				while (dcards.GetCardTotal() < 16) {
					String card1 = newdeck.dealCard();
					dcards.CardHit(card1);
					displayMessage("Croupier joue..." + card1 + "\n" + "Total:"
							+ dcards.GetCardTotal() + "\n");
				}
			}
			if (dcards.CheckBust()) {
				displayMessage("Croupier saute!");
			} else {
				displayMessage("Croupier a " + " " + dcards.GetCardTotal());
			}

			Results();
		}

		private void cardhit() {

			String nextc = newdeck.dealCard();
			sendData(nextc);
			players.get(this.myConID - 1).CardHit(nextc);
			sendData("Votre Total est : "
					+ players.get(this.myConID - 1).GetCardTotal());
			if (players.get(this.myConID - 1).CheckBust()) {
				sendData("saute !\n");
				playersleft--;
				if (playersleft == 0) {
					DealerGo();
				}
			}

		}

		private void CheckDone() {

			if (playersleft == 0) {

				DealerGo();
			}
		}

		private void closeConnection() {
			displayMessage("\nConnexion terminée " + myConID + "\n");

			try {
				output.close();
				input.close();
				connection.close();
			} catch (IOException ioException) {
			}
		}

		private void sendData(String message) {
			try {
				output.writeObject(message);
				output.flush();

			} catch (IOException ioException) {
				displayArea.append("\nErreur");
			}
		}

	}

}
