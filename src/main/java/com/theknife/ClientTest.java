package com.theknife;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ClientTest {

    void exec() throws IOException, ClassNotFoundException {
        InetAddress addr = InetAddress.getByName(null); // Localhost
        System.out.println("addr = " + addr);
        int port = 2345;

        Socket socket = new Socket(addr, port);

        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Scanner scanner = new Scanner(System.in)
        ) {

            String command = "";

            while (!command.equals("exit")) {

                System.out.println("\nComandi disponibili:");
                System.out.println("registerUser, login, searchRestaurants, addRestaurant, getRestaurantDetails, addReview, viewReviews, exit");
                command = scanner.nextLine();

                out.writeObject(command); // invio comando

                switch (command) {

                    //REGISTER
                    case "registerUser":
                        System.out.println("Nome:");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Cognome:");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Username:");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Password:");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Ruolo (cliente/ristoratore):");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Domicilio:");
                        out.writeObject(scanner.nextLine());
                        break;

                    //LOGIN
                    case "login":
                        System.out.println("Username:");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Password:");
                        out.writeObject(scanner.nextLine());
                        break;

                    //SEARCH RESTAURANTS

                    case "searchRestaurants":
                        System.out.println("Inserisci filtro:");
                        out.writeObject(scanner.nextLine());
                        break;

                    //ADD RESTAURANT
                    case "addRestaurant":
                        System.out.println("Owner username:");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Nome ristorante:");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Nazione:");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Città:");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Indirizzo:");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Latitudine (double):");
                        out.writeObject(Double.parseDouble(scanner.nextLine()));

                        System.out.println("Longitudine (double):");
                        out.writeObject(Double.parseDouble(scanner.nextLine()));

                        System.out.println("Prezzo medio:");
                        out.writeObject(Integer.parseInt(scanner.nextLine()));

                        System.out.println("Delivery (true/false):");
                        out.writeObject(Boolean.parseBoolean(scanner.nextLine()));

                        System.out.println("Prenotazione online (true/false):");
                        out.writeObject(Boolean.parseBoolean(scanner.nextLine()));

                        System.out.println("Tipo cucina:");
                        out.writeObject(scanner.nextLine());
                        break;

                    //GET RESTAURANT DETAILS
                    case "getRestaurantDetails":
                        System.out.println("ID ristorante:");
                        out.writeObject(Integer.parseInt(scanner.nextLine()));
                        break;

                    //ADD REVIEW
                    case "addReview":
                        System.out.println("ID ristorante:");
                        out.writeObject(Integer.parseInt(scanner.nextLine()));

                        System.out.println("Username recensore:");
                        out.writeObject(scanner.nextLine());

                        System.out.println("Stelle (1-5):");
                        out.writeObject(Integer.parseInt(scanner.nextLine()));

                        System.out.println("Testo recensione:");
                        out.writeObject(scanner.nextLine());
                        break;

                    //VIEW REVIEWS
                    case "viewReviews":
                        System.out.println("ID ristorante:");
                        out.writeObject(Integer.parseInt(scanner.nextLine()));
                        break;

                    case "exit":
                        System.out.println("Uscita...");
                        continue;

                    default:
                        System.out.println("Comando non riconosciuto.");
                        continue;
                }

                //RICEZIONE RISPOSTA
                ServerResponse response = (ServerResponse) in.readObject();

                System.out.println("\n--- SERVER RESPONSE ---");
                System.out.println("STATUS: " + response.getStatus());

                Object payload = response.getPayload();

                if (payload == null) {
                    System.out.println("NO PAYLOAD");
                }
                else if (payload instanceof List<?>) {
                    System.out.println("LISTA:");
                    List<?> list = (List<?>) payload;
                    for (Object item : list) {
                        System.out.println(item);
                    }
                }
                else {
                    System.out.println("DATI:");
                    System.out.println(payload);
                }

                System.out.println("-----------------------\n");

            }

        } finally {
            socket.close();
        }
    }

    public static void main(String[] args) {
        try {
            new ClientTest().exec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
