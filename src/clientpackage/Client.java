package clientpackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

/**
 * Client de calculatrice (TCP).
 * Usage : java clientPackage.Client [adresseIP_serveur] [port]
 * Par défaut : localhost:1234.
 */
public class Client {

    static final String[] SYMBOLES = {"", "+", "-", "*", "/"};
    static final int OPERANDE = 5;

    static int lireEntier(Scanner sc, String message) {
        while (true) {
            System.out.print(message);
            String ligne = sc.nextLine().trim();
            try {
                return Integer.parseInt(ligne);
            } catch (NumberFormatException e) {
                System.out.println("Saisie invalide : entrez un entier.");
            }
        }
    }

    public static void main(String[] args) {
        String hote = (args.length > 0) ? args[0] : "localhost";
        int port = (args.length > 1) ? Integer.parseInt(args[1]) : 1234;

        try {
            System.out.println("Je suis un client pas encore connecté…");
            InetSocketAddress adresse = new InetSocketAddress(InetAddress.getByName(hote), port);
            Socket socket = new Socket();
            socket.connect(adresse, 5000);               // timeout 5 s
            System.out.println("je suis un client connecté à " + adresse);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            Scanner sc = new Scanner(System.in);

            int choix;
            do {
                System.out.println("\n===== CALCULATRICE =====");
                System.out.println("1. Addition        (x + " + OPERANDE + ")");
                System.out.println("2. Soustraction    (x - " + OPERANDE + ")");
                System.out.println("3. Multiplication  (x * " + OPERANDE + ")");
                System.out.println("4. Division        (x / " + OPERANDE + ")");
                System.out.println("0. Quitter");
                choix = lireEntier(sc, "Votre choix : ");

                if (choix < 0 || choix > 4) {
                    System.out.println("Choix invalide.");
                    continue;
                }
                out.writeInt(choix);
                if (choix != 0) {
                    int x = lireEntier(sc, "Entrez un entier x : ");
                    out.writeInt(x);
                    out.flush();
                    double resultat = in.readDouble();
                    System.out.println("Résultat : " + x + " " + SYMBOLES[choix] + " " + OPERANDE + " = " + resultat);
                }
                out.flush();
            } while (choix != 0);

            System.out.println("Fin de la communication.");
            sc.close();
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }
}
