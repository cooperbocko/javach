package com.mycompany.bootstrap;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Scanner;

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

        // just quick user interaction thing 
        Scanner userInput = new Scanner(System.in);
        while (true) {
            System.out.print(">> ");
            String command = userInput.nextLine().trim();

            if (command.isEmpty()) continue;
            if (command.equalsIgnoreCase("exit")) {
                System.out.println("Shutting down...");
                System.exit(0);
            }

            try (var sock = new java.net.Socket(self.address, self.port);
                var out = new java.io.PrintWriter(sock.getOutputStream(), true);
                var in = new java.io.BufferedReader(new java.io.InputStreamReader(sock.getInputStream()))) {

                out.println(command);
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.err.println("Error processing command: " + e.getMessage());
            }
        }
    }
}
