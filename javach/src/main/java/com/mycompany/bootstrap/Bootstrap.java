package com.mycompany.bootstrap;

import java.io.IOException;
import java.net.InetAddress;

import com.mycompany.util.Node;
import com.mycompany.util.ServerThread;

public class Bootstrap {
    private static Node self;
    

    public static void main(String args[]) throws IOException {
        //TODO: Set up node info from config file
        //this is test info
        self = new Node(0, 3001);
        self.address = InetAddress.getByName("localhost");
        self.port = 3001;
        self.sAddress = InetAddress.getByName("localhost");
        self.sPort = 3002;
        self.pAddress = InetAddress.getByName("localhost");
        self.pPort = 3002;
        int[] range = {501, 0};
        self.keyRange = range;
        self.map.put(749, "check it is moved!");

        /* 
        //test to see if find works
        Find data = Node.find(11, self);
        System.out.println(data.ids.toString());

        //test insert
        Node.Insert(11, "new entry!", data);
        */

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

        //TODO: start server thread
        //TODO: start user thread
    }
}
