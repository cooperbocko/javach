package com.mycompany.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

import com.mycompany.util.Find;
import com.mycompany.util.Node;
import com.mycompany.util.ServerThread;

public class Bootstrap {
    private static Node self;

    public static void main(String args[]) throws IOException {
        //reading config
        if (args.length != 1) {
            System.err.println("Requires bnConfigFile.");
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
            self = new Node(id, port);
            self.address = InetAddress.getLocalHost();
            self.port = port;
            self.pAddress = self.address;
            self.pPort = port;
            self.sAddress = self.address;
            self.sPort = port;
            self.keyRange[0] = 0;
            self.keyRange[1] = Node.MAX_RANGE; 
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+", 2);
                int key = Integer.parseInt(parts[0]);
                String value = parts[1];
                self.map.put(key, value);
            }

            System.out.println("Bootstrap server initialized:");
            System.out.println(" - ID: 0");
            System.out.println(" - Listening Port: " + port);
            System.out.println(" - Loaded keys: " + self.map.size());
        } catch (Exception e) {
            System.err.println("Error reading config file: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        //start server thread to listen for commands
        ServerThread x = new ServerThread(self);
        Thread s = new Thread(x);
        s.start();

        //TODO: get user input to call functions 
        Scanner input = new Scanner(System.in);
        String cmd = "";        
        while (!cmd.equalsIgnoreCase("quit")) {
            System.out.print("Enter a command: ");
            cmd = input.nextLine().trim();
            String[] parsed = cmd.split(" ");
            switch (parsed[0]) {
                case "insert": {
                    //Handles insert, other commands should use Node.sendM to send messages to the specified node
                    System.out.println("Inserting!");
                    int key = Integer.parseInt(parsed[1]);
                    Find node = Node.find(key, self);
                    Node.sendM(node, "insert " + key + " " + parsed[2]);
                    break;
                }
                case "lookup": {
                    break;
                }
                case "delete": {
                    break;
                }
                default: {
                    break;
                }
            }
        }
        input.close();
    }
}
