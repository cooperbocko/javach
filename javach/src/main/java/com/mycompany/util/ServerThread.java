package com.mycompany.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread implements Runnable {
    private Node self;
    ServerSocket server;
    Socket client;
    PrintWriter out;
    BufferedReader in;
    
    public ServerThread(Node node) throws IOException {
        this.self = node;
        server = new ServerSocket(self.port);
    }

    @Override
    public void run() {
        while (true) {
            try {
                //get connection
                client = server.accept();
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                //get request
                String request = in.readLine();
                System.out.println(request);
                String[] parsed = request.split(" ");

                //handle request
                //TODO: add request handlers
                switch(parsed[0]) {
                    case "find": {
                        handleFind(parsed, out, self);
                        break;
                    }
                    case "insert": {
                        int key = Integer.parseInt(parsed[1]);
                        String value = request.substring(request.indexOf(parsed[2])).trim();
                        Find dest = Node.find(key, self); // Find node responsible for key 

                        // If current node is responsible 
                        if (dest.address.equals(self.address) && dest.port == self.port) {
                            handleInsert(key, value, out, self);
                        } else { // Forward insert request to responsible node
                            try (
                                Socket sock = new Socket(dest.address, dest.port);
                                PrintWriter remoteOut = new PrintWriter(sock.getOutputStream(), true);
                                BufferedReader remoteIn = new BufferedReader(new InputStreamReader(sock.getInputStream()))
                            ) {
                                remoteOut.println("insert " + key + " " + value); // Send command
                                String response = remoteIn.readLine();
                                out.println(response); 
                            }
                        }                    
                        break;
                    }
                    case "lookup": {
                        int key = Integer.parseInt(parsed[1]);
                        StringBuilder pathBuilder = new StringBuilder();
                        for (int i = 2; i < parsed.length; i++) {
                            pathBuilder.append(parsed[i]).append(" ");
                        } // Tracks nodes traversed so far 
                        String path = pathBuilder.toString();
                        // handleLookup(key, out, self, path);
                        String response = handleLookup(key, out, self, path);
                        out.println(response);
                        break;
                    }
                    case "delete": {
                        //TODO: delete
                        break;
                    }
                    //contact bootstrap initally
                    case "enter-b": {
                        handleEnterB(Integer.parseInt(parsed[1]), out, self);
                        break;
                    }
                    //contacting node being entered into
                    case "enter": {
                        handleEnter(parsed, out, self);
                        break;
                    }
                    case "enter-help": {
                        handleEnterHelp(out, in, self, client);
                        break;
                    }
                    case "exit": {
                        //TODO: exit
                        break;
                    }
                    case "update-s": {
                        handleUpdateS(parsed, self);
                        break;
                    }
                    default: {
                        out.println("Unkown Request");
                        break;
                    }
                }
                //close connection
                client.close();
            } catch (IOException e) {
                System.out.println("Error in Server Thread");
                e.printStackTrace();
            } finally {
                
            }
        }
    }

    //TODO: Handlers here -> main functions should send a reponse back, any helpers should not
    //finds specific node
    //find key
    public static void handleFind(String[] parsed, PrintWriter out, Node self) {
        int key = Integer.parseInt(parsed[1]);
        if (Node.inRange(key, self.keyRange)) {
            out.println("yes " + self.id);
        } else {
            out.println("no " + self.id + " " + self.sAddress.getHostName() + " " + self.sPort);
            }
    }

    //inserts into nodes table
    //insert key value
    public static void handleInsert(int key, String value, PrintWriter out, Node self) {
        System.out.println("Inserting key: " + key + " with value: \"" + value + "\" into node " + self.id);
        self.map.put(key, value);
        // out.println(("inserted!"));
        // System.out.println(self.map.toString());
    }

    public static String handleLookup(int key, PrintWriter out, Node self, String pathSoFar) throws IOException {
        pathSoFar += self.id + " ";
        StringBuilder response = new StringBuilder();

        if (Node.inRange(key, self.keyRange)) {
            // System.out.println("Key " + key + " is in range for node " + self.id);
            if (self.map.containsKey(key)) {
                // out.println("value: " + self.map.get(key));
                response.append("value: ").append(self.map.get(key)).append("\n");
                System.out.printf("[Node %d] Key %d found with value: \"%s\"\n", self.id, key, self.map.get(key));
            } else {
                // out.println("Key not found");
                response.append("Key not found").append("\n");
                System.out.printf("[Node %d] Key %d not found in map\n", self.id, key);
            }
            // out.println("server: " + self.id);
            // out.println("path: " + pathSoFar.trim());
            response.append("server: ").append(self.id).append("\n");
            response.append("path: ").append(pathSoFar.trim());    
        } else {
            System.out.printf("[Node %d] Key %d not in range. Forwarding to successor (%s:%d)\n",
                self.id, key, self.sAddress.getHostName(), self.sPort);
            try (
                Socket nextNode = new Socket(self.sAddress, self.sPort);
                PrintWriter nextOut = new PrintWriter(nextNode.getOutputStream(), true);
                BufferedReader nextIn = new BufferedReader(new InputStreamReader(nextNode.getInputStream()))
            ) {
                nextOut.println("lookup " + key + " " + pathSoFar.trim());
                String line;
                while ((line = nextIn.readLine()) != null) {
                    response.append(line).append("\n");
                    // out.println(line);
                }
            }
        }
        return response.toString();
    }
    //send back node info that needs to entered into
    //enter-b id
    public static void handleEnterB(int id, PrintWriter out, Node self) throws IOException{
        //check if self so it does not block on its own request
        if (Node.inRange(id, self.keyRange)) {
            out.println(self.address.getHostName() + " " + self.port);
            return;
        }

        Find node = Node.find(id, self);
        out.println(node.address.getHostName() + " " + node.port);
    }

    //enters into this node
    //enter id address port
    public static void handleEnter(String[] parsed, PrintWriter out, Node self) throws IOException {
        Find address = new Find();
        address.address = InetAddress.getByName(parsed[2]);
        address.port = Integer.parseInt(parsed[3]);
        Node.enter(Integer.parseInt(parsed[1]), address, self);
        out.println("entered!");
    }

    //entering node getting info
    public static void handleEnterHelp(PrintWriter out, BufferedReader in, Node self, Socket client) throws IOException {
        String[] resp;

        //get new key range
        resp = in.readLine().split(" ");
        int[] range = {Integer.parseInt(resp[0]), Integer.parseInt(resp[1])};
        self.keyRange = range;

        //get keys
        int nKeys = Integer.parseInt(in.readLine());
        for (int i = 0; i < nKeys; i++) {
            resp = in.readLine().split(" ", 2);
            self.map.put(Integer.parseInt(resp[0]), resp[1]);
        }

        //get predecessor details
        resp = in.readLine().split(" ");
        self.pAddress = InetAddress.getByName(resp[0]);
        self.pPort = Integer.parseInt(resp[1]);

        //get sucessor
        resp = in.readLine().split(" ");
        self.sAddress = InetAddress.getByName(resp[0]);
        self.sPort = Integer.parseInt(resp[1]);
    }

    public static void handleUpdateS(String[] parsed, Node self) throws IOException{
        self.sAddress = InetAddress.getByName(parsed[1]);
        self.sPort = Integer.parseInt(parsed[2]);
    }
}
