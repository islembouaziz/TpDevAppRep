package serverpackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Serveur de calculatrice (TCP).
 * Usage : java serverPackage.Server [adresseIP] [port]
 * Sans argument : détecte l'adresse IP réelle (réseau local) et utilise le port 1234.
 */
public class Server {

    static final int PORT_PAR_DEFAUT = 1234;
    static final int OPERANDE = 5;   // le service calcule : x <op> 5

    /** Cherche une adresse IPv4 du réseau local (différente de 127.0.0.1). */
    static InetAddress adresseReelle() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface ni = interfaces.nextElement();
            if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
            for (InetAddress a : Collections.list(ni.getInetAddresses())) {
                if (a instanceof Inet4Address && a.isSiteLocalAddress()) return a;
            }
        }
        return InetAddress.getLocalHost();
    }

    /** Calcul effectué côté serveur selon le choix du client. */
    static double calculer(int choix, int x) {
        switch (choix) {
            case 1:  return x + OPERANDE;
            case 2:  return x - OPERANDE;
            case 3:  return x * OPERANDE;
            case 4:  return (double) x / OPERANDE;
            default: return Double.NaN;
        }
    }

    public static void main(String[] args) {
        try {
            InetAddress ip = (args.length > 0) ? InetAddress.getByName(args[0]) : adresseReelle();
            int port = (args.length > 1) ? Integer.parseInt(args[1]) : PORT_PAR_DEFAUT;

            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(ip, port));
            System.out.println("Je suis un serveur en attente la connexion d'un client ");
            System.out.println("Écoute sur " + ip.getHostAddress() + ":" + port);

            Socket socket = serverSocket.accept();
            System.out.println("un client est connecté : " + socket.getInetAddress().getHostAddress());

            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            int choix;
            while ((choix = in.readInt()) != 0) {       // 0 = quitter
                int x = in.readInt();
                double resultat = calculer(choix, x);
                System.out.println("choix=" + choix + ", x=" + x + " -> " + resultat);
                out.writeDouble(resultat);
                out.flush();
            }

            System.out.println("Fin de la communication.");
            in.close();
            out.close();
            socket.close();
            serverSocket.close();
        } catch (EOFException e) {
            System.out.println("Le client s'est déconnecté brutalement.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
