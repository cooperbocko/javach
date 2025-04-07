package com.mycompany.nameserver;

import java.io.IOException;
import java.net.InetAddress;

import com.mycompany.util.Find;
import com.mycompany.util.Node;
import com.mycompany.util.ServerThread;

//THIS IS A TEST FILE TO RUN MULTIPLE NODES AT ONCE
public class Nameserver2 {
    private static Node self;

    public static void main(String args[]) throws IOException {
        self = new Node(750, 3003);
        self.address = InetAddress.getByName("localhost");
        self.port = 3003;

        self.pPort = -1;
        self.sPort = -1;

        Find boot = new Find();
        boot.address = InetAddress.getByName("localhost");
        boot.port = 3001;
        
        System.out.println("start thread");
        ServerThread x = new ServerThread(self);
        Thread s = new Thread(x);
        s.start();
        
        System.out.println("buut");

        Find node = Node.enterB(self.id, boot);
        Node.sendM(node, "enter " + self.id + " " + self.address.getHostAddress() + " " + self.port);

        try {
            Thread.sleep(5000);
        } catch (Exception e) {

        }
        
        System.out.println(self.pPort);
        System.out.println(self.sPort);
        System.out.println(self.map.get(749));
    }
    
}

