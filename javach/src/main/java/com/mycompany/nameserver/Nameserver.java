package com.mycompany.nameserver;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

import com.mycompany.util.Find;
import com.mycompany.util.Node;
import com.mycompany.util.ServerThread;

public class Nameserver {
    private static Node self;
    private static String bsIP;
    private static int bsPort;

    public static void main(String args[]) throws IOException {
        //reading config
        if (args.length != 1) {
            System.err.println("Requires nsConfigFile.");
            System.exit(1);
        }

        String configPath = args[0];
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            System.err.println("File not found.");
            System.exit(1);
        }

        try (Scanner scanner = new Scanner(configFile)) {
            int id = Integer.parseInt(scanner.nextLine().trim());
            int port = Integer.parseInt(scanner.nextLine().trim());
            String[] bsInfo = scanner.nextLine().trim().split("\\s+");
            bsIP = bsInfo[0];
            bsPort = Integer.parseInt(bsInfo[1]);

            self = new Node(id, port);
            self.address = InetAddress.getLocalHost();
            self.port = port;

            System.out.println("Nameserver initialized:");
            System.out.println(" - ID: " + id);
            System.out.println(" - Listening on port: " + port);
            System.out.println(" - Bootstrap at: " + bsIP + ":" + bsPort);

        } catch (Exception e) {
            System.err.println("Error parsing.");
            e.printStackTrace();
            System.exit(1);
        }

        //start server thread to listen for commands
        ServerThread x = new ServerThread(self);
        Thread s = new Thread(x);
        s.start();

        //TODO: handle user input to call functions
        //I think I did this the way you guys wanted, if not feel free to delete it or rewrite it
        //Open a socket to send commands to the server thread
        //No need to open a new socket -> just use Node.sendM()

        Scanner input = new Scanner(System.in);
        String cmd = "";        
        while (!cmd.equalsIgnoreCase("quit")) {
            System.out.print("Enter a command: ");
            cmd = input.nextLine().trim();
            
            switch (cmd) {
                case "enter": {
                    //Handles enter, other commands should use Node.sendM to send messages to the specified node
                    Find boot = new Find();
                    boot.address = InetAddress.getByName(bsIP);
                    boot.port = bsPort;
                    Find node = Node.enterB(self.id, boot);
                    Node.sendM(node, "enter " + self.id + " " + self.address.getHostName() + " " + self.port);
                    System.out.println("Updated keyrange: " + self.keyRange[0] + "-" + self.keyRange[1]);
                    break;
                }
                case "exit": {
                    String response = Node.exit(self);
                    System.out.println(response);
                    break;
                }
            }
        }
        input.close();
    }
}
