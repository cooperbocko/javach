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
                switch(parsed[0]) {
                    case "find": {
                        handleFind(parsed, out, self);
                        break;
                    }
                    case "insert": {
                        handleInsert(Integer.parseInt(parsed[1]), request.substring(8 + parsed[1].length()),  out, self);
                        break;
                    }
                    case "lookup": {
                        handleLookup(parsed, out, self);
                        break;
                    }
                    case "delete": {
                        handleDelete(parsed, out, self);
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
                    case "exit-update-key-range": {
                        updateKeyRange(parsed, self);
                        out.println("updated!");
                        break;
                    }
                    case "exit-update-key-values": {
                        updateKeyValues(parsed, self);
                        out.println("updated");
                        break;
                    }
                    case "update-s": {
                        handleUpdateS(parsed, self);
                        break;
                    }
                    case "update-p": {
                        handleUpdateP(parsed, self);
                        out.println("updated!");
                        break;
                    }
                    default: {
                        out.println("Unkown Request");
                        break;
                    }
                }
                Node.printNodeInfo(self);
                //close connection
                client.close();
            } catch (IOException e) {
                System.out.println("Error in Server Thread");
                e.printStackTrace();
            } finally {
                
            }
        }
    }

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
        self.map.put(key, value);
        out.println(("inserted!"));
        System.out.println(self.map.toString());
    }

    private void handleDelete(String[] parsed, PrintWriter out, Node self) {
        int key = Integer.parseInt(parsed[1]);
        if (self.map.get(key) != null) {
            self.map.remove(key);
            System.out.println(self.map.toString());
            out.println("Key-value successfully deleted!");
        } else {
            out.println("Key was not present.");
        }
    }
    
    public static void handleLookup(String[] parsed, PrintWriter out, Node self) {
        try {
            int key = Integer.parseInt(parsed[1]);
            String value = self.map.get(key);
            String response;
            if (value != null) {
                response = "Value: " + value;
            } else {
                response = "Key not found";
            }    
            out.println("Key: " + key + ", " + response);
        } catch (Exception e) {
            out.println("Error during lookup");
            e.printStackTrace();
        }
    
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
            //TODO: bad way to check null but oh well
            if (resp[1].equals("null")) {
                continue;
            }
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

    /**
     * When a node exits the system, update its successor's key range
     * @param parsed The exiting node's key range
     * @param self The exiting node's successor
     */
    private static void updateKeyRange(String[] parsed, Node self) {
        int b = Integer.parseInt(parsed[1]);

        //check if bootstrap
        if (self.keyRange[0] == 0 || self.keyRange[1] == 0) {
            if (b == 1) {
                //only node left is bootstrap -> reset
                self.keyRange[0] = 0;
                self.keyRange[1] = Node.MAX_RANGE;
            } else {
                //not only node left
                self.keyRange[0] = b;
            }
        } else {
            //normal node
            //I think only the first value in the key range needs to be updated
            self.keyRange[0] = b;
        }
    }

    /**
     * When a node exits the system, take the key value pairs from the exiting node 
     * and add them to its successor's map.
     * 
     * @param parsed The key value pairs from the exiting node.
     * @param self The exiting node's successor.
     */
    private static void updateKeyValues(String[] parsed, Node self) {
        //I wrote i += 2 instead of i++ because the value at i is the key
        //and the value at i + 1 is the value associated with the key
        for (int i = 1; i < parsed.length; i += 2) {
            self.map.put(Integer.parseInt(parsed[i]), parsed[i + 1]);
        }
    }

    public static void handleUpdateS(String[] parsed, Node self) throws IOException{
        self.sAddress = InetAddress.getByName(parsed[1]);
        self.sPort = Integer.parseInt(parsed[2]);
    }

    public static void handleUpdateP(String[] parsed, Node self) throws IOException {
        self.pAddress = InetAddress.getByName(parsed[1]);
        self.pPort = Integer.parseInt(parsed[2]);
    }
}
