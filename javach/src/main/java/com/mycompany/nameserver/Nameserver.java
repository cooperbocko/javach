package com.mycompany.nameserver;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import com.mycompany.util.Node;
import com.mycompany.util.ServerThread;

public class Nameserver {
    private static Node self;

    public static void main(String args[]) throws IOException {
        //TODO: set up node info from config file
        //this is test info
        self = new Node(500, 3002);
        int[] range = {1, 500};
        self.address = InetAddress.getByName("localhost");
        self.port = 3002;
        self.keyRange = range;
        self.sAddress = InetAddress.getByName("localhost");
        self.sPort = 3001;
        self.pAddress = InetAddress.getByName("localhost");
        self.pPort = 3001;

        //TODO: start server thread
        //test running of thread
        ServerThread x = new ServerThread(self);
        Thread s = new Thread(x);
        s.start();

        try {
            Thread.sleep(10000);
        } catch (Exception e) {

        }
        
        System.out.println(self.pPort);
        System.out.println(self.sPort);


        //TODO: handle user input to call functions
        //I think I did this the way you guys wanted, if not feel free to delete it or rewrite it

        //Open a socket to send commands to the server thread
        Socket socket = new Socket("localhost", self.port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        Scanner input = new Scanner(System.in);
        String cmd = "";        
        while (!cmd.equalsIgnoreCase("quit")) {
            System.out.print("Enter a command: ");
            cmd = input.nextLine().trim();

            //Send the command to the server thread
            out.println(cmd);
        }

        socket.close();
        out.close();
        input.close();
    }
}
