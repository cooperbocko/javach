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


        //TODO: Set up node info from config file
        //this is test info
        // self = new Node(0, 3001);
        // self.address = InetAddress.getByName("localhost");
        // self.port = 3001;
        // self.sAddress = InetAddress.getByName("localhost");
        // self.sPort = 3002;
        // self.pAddress = InetAddress.getByName("localhost");
        // self.pPort = 3002;
        // int[] range = {501, 0};
        // self.keyRange = range;
        // self.map.put(749, "check it is moved!");

        /* 
        //test to see if find works
        Find data = Node.find(11, self);
        System.out.println(data.ids.toString());

        //test insert
        Node.Insert(11, "new entry!", data);
        */

        //TODO start server thread on diff thread

        ServerThread x = new ServerThread(self);
        Thread s = new Thread(x);
        s.start();

        try {
            Thread.sleep(10000);
        } catch (Exception e) {

        }
        
        System.out.println(self.pPort);
        System.out.println(self.sPort);
        System.out.println(self.map.get(749));

        
        //TODO: get user input to call functions 
    }
}
