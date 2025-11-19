package com.theknife;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
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
    Scanner scanner = new Scanner(System.in)) {

            String command = "";
            while (!command.equals("exit")) {
                System.out.println("Enter command (register, login, searchRestaurant, addRestaurant, addReview, viewReviews, exit):");
                command = scanner.nextLine();

                switch (command) {
                    case "register":
                        System.out.println("Enter first name:");
                        String firstName = scanner.nextLine();
                        System.out.println("Enter last name:");
                        String lastName = scanner.nextLine();
                        System.out.println("Enter username:");
                        String username = scanner.nextLine();
                        System.out.println("Enter password:");
                        String password = scanner.nextLine();
                        System.out.println("Enter role (cliente/ristoratore):");
                        String role = scanner.nextLine();

                        out.writeObject("register");
                        out.writeObject(firstName);
                        out.writeObject(lastName);
                        out.writeObject(username);
                        out.writeObject(password);
                        out.writeObject(role);

                        String response = (String) in.readObject();
                        System.out.println("Server response: " + response);
                        break;

                    case "login":
                        System.out.println("Enter username:");
                        String user = scanner.nextLine();
                        System.out.println("Enter password:");
                        String pass = scanner.nextLine();

                        out.writeObject("login");
                        out.writeObject(user);
                        out.writeObject(pass);

                        boolean isValid = (boolean) in.readObject();
                        System.out.println("Login result: " + isValid);
                        break;

                    case "searchRestaurant":
                        System.out.println("Enter location (city/country):");
                        String location = scanner.nextLine();
                        System.out.println("Enter cuisine type (optional):");
                        String cuisine = scanner.nextLine();

                        out.writeObject("searchRestaurant");
                        out.writeObject(location);
                        out.writeObject(cuisine);

                        Object[] restaurants = (Object[]) in.readObject();
                        System.out.println("Restaurants found:");
                        for (Object r : restaurants) {
                            System.out.println(r);
                        }
                        break;

                    case "addRestaurant":
                        System.out.println("Enter restaurant name:");
                        String rName = scanner.nextLine();
                        System.out.println("Enter country:");
                        String country = scanner.nextLine();
                        System.out.println("Enter city:");
                        String city = scanner.nextLine();
                        System.out.println("Enter address:");
                        String address = scanner.nextLine();
                        System.out.println("Enter price range (e.g., 20-50):");
                        String priceRange = scanner.nextLine();
                        System.out.println("Delivery available? (yes/no):");
                        String delivery = scanner.nextLine();
                        System.out.println("Online booking available? (yes/no):");
                        String booking = scanner.nextLine();
                        System.out.println("Cuisine type:");
                        String cType = scanner.nextLine();

                        out.writeObject("addRestaurant");
                        out.writeObject(rName);
                        out.writeObject(country);
                        out.writeObject(city);
                        out.writeObject(address);
                        out.writeObject(priceRange);
                        out.writeObject(delivery);
                        out.writeObject(booking);
                        out.writeObject(cType);

                        response = (String) in.readObject();
                        System.out.println("Server response: " + response);
                        break;

                    case "addReview":
                        System.out.println("Enter restaurant name:");
                        String restName = scanner.nextLine();
                        System.out.println("Enter rating (1-5):");
                        int rating = scanner.nextInt();
                        scanner.nextLine(); // consume newline
                        System.out.println("Enter review text:");
                        String reviewText = scanner.nextLine();

                        out.writeObject("addReview");
                        out.writeObject(restName);
                        out.writeObject(rating);
                        out.writeObject(reviewText);

                        response = (String) in.readObject();
                        System.out.println("Server response: " + response);
                        break;

                    case "viewReviews":
                        System.out.println("Enter restaurant name:");
                        String restView = scanner.nextLine();

                        out.writeObject("viewReviews");
                        out.writeObject(restView);

                        Object[] reviews = (Object[]) in.readObject();
                        System.out.println("Reviews:");
                        for (Object rev : reviews) {
                            System.out.println(rev);
                        }
                        break;

                    case "exit":
                        System.out.println("Exiting...");
                        break;

                    default:
                        System.out.println("Unknown command.");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            socket.close(); // Ensure the socket is closed
        }
    }

    public static void main(String[] args) {
        try {
            new ClientTest().exec();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            e.getMessage();
        }
    }
}
