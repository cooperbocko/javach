package com.mycompany.nameserver;

import java.io.IOException;
import java.net.InetAddress;

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

        //TODO: start user thread immediately
        //TODO: start server thread after being entered into the system
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
        
    }
}
