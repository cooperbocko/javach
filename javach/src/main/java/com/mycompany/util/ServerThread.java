package com.mycompany.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
                        int key = Integer.parseInt(parsed[1]);
                        if (Node.inRange(key, self.keyRange)) {
                            out.println("yes " + self.id);
                        } else {
                            out.println("no " + self.id + " " + self.sAddress.getHostName() + " " + self.sPort);
                        }
                    }
                    default: {
                        out.println("Unkown Request");
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
}
