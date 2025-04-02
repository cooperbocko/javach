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
        self = new Node(2, 3002);
        int[] range = {11, 20};
        self.keyRange = range;
        self.sAddress = InetAddress.getByName("localhost");
        self.sPort = 3003;

        //TODO: start user thread immediately
        //TODO: start server thread after being entered into the system
        //test running of thread
        ServerThread x = new ServerThread(self);
        x.run();
        
    }
}
