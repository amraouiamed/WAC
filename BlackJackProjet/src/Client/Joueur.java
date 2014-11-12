package Client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Joueur extends JFrame {
	private JButton Jouer;
	private JButton Stop;
	private JPanel buttons;
	private JTextArea displayArea;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String chatServer;
	private Socket client;

	public Joueur(String host) {
		super("Joueur");

		chatServer = host;

		buttons = new JPanel();
		buttons.setLayout(new GridLayout(1, 2));
		Jouer = new JButton("Jouer");
		Stop = new JButton("Stop");

		Jouer.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				sendData("Jouer");
			}
		});

		Stop.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				sendData("Stop");
			}
		});

		buttons.add(Jouer, BorderLayout.SOUTH);
		buttons.add(Stop, BorderLayout.SOUTH);
		buttons.setVisible(true);
		add(buttons, BorderLayout.SOUTH);
		displayArea = new JTextArea();
		add(new JScrollPane(displayArea), BorderLayout.CENTER);

		setBounds(900, 300, 100, 100);
		setSize(300, 300);
		setVisible(true);
	}

	public void runClient() {
		try {
			connectToServer();
			getStreams();
			processConnection();
		} catch (EOFException eofException) {
			displayMessage("\nConnexion client terminée");
		} catch (IOException ioException) {
		} finally {
			closeConnection();
		}
	}

	// connection au serveur
	private void connectToServer() throws IOException {
		displayMessage("En attente de connexion\n");

		// Socket pour connection
		client = new Socket(InetAddress.getByName(chatServer), 23555);

		displayMessage("Connecté au : " + client.getInetAddress().getHostName());
	}

	private void getStreams() throws IOException {

		output = new ObjectOutputStream(client.getOutputStream());
		output.flush();

		input = new ObjectInputStream(client.getInputStream());

		displayMessage("\nGot I/O streams\n");
	}

	private void processConnection() throws IOException {

		do {
			try {
				message = (String) input.readObject();
				displayMessage("\n" + message);
				if (message.contains("saute!")
						|| message.contains("Merci de patienter")) {
					sendData("Rejouer");
					
				}

			} catch (ClassNotFoundException classNotFoundException) {
				displayMessage("\nObjet inconnu");
			}

		} while (!message.equals("SERVER>>> TERMINATE"));
	}

	private void closeConnection() {
		displayMessage("\nConnexion fermée");

		try {
			output.close();
			input.close();
			client.close();
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

	private void displayMessage(final String messageToDisplay) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				displayArea.append(messageToDisplay);
			}
		});
	}

}
