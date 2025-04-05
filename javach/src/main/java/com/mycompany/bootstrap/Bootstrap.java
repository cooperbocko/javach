package com.mycompany.bootstrap;

import java.io.IOException;
import java.net.InetAddress;

import com.mycompany.util.Find;
import com.mycompany.util.Node;

public class Bootstrap {
    private static Node self;
    

    public static void main(String args[]) throws IOException {
        //TODO: Set up node info from config file
        //this is test info
        self = new Node(1, 3001);
        self.sAddress = InetAddress.getByName("localhost");
        self.sPort = 3002;
        int[] range = {0,10};
        self.keyRange = range;

        //test to see if find works
        Find data = Node.find(11, self);
        System.out.println(data.ids.toString());

        //test insert
        Node.Insert(11, "new entry!", data);

        //TODO: start server thread
        //TODO: start user thread
    }
}
