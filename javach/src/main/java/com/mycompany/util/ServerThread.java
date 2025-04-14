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
                        handleInsert(Integer.parseInt(parsed[1]), request.substring(8 + parsed[1].length()),  out, self);
                        break;
                    }
                    case "lookup": {
                        //TODO: lookup
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
                        exitHelp(out, in, self, client);
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
        self.map.put(key, value);
        out.println(("inserted!"));
        System.out.println(self.map.toString());
    }

    //send back node info that needs to entered into
    //enter-b id
    public static void handleEnterB(int id, PrintWriter out, Node self) throws IOException{
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
        int[] range = {Integer.parseInt(resp[0]), Integer.parseInt(resp[0])};
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

    public static void exitHelp(PrintWriter out, BufferedReader in, Node self, Socket client) throws IOException {

    }

    public static void handleUpdateS(String[] parsed, Node self) throws IOException{
        self.sAddress = InetAddress.getByName(parsed[1]);
        self.sPort = Integer.parseInt(parsed[2]);
    }
}
