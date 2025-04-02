package com.mycompany.nameserver;

import java.io.IOException;

import com.mycompany.util.Node;
import com.mycompany.util.ServerThread;

public class Nameserver2 {
    private static Node self;

    public static void main(String args[]) throws IOException {
        self = new Node(3, 3003);
        int[] range = {21, 30};
        self.keyRange = range;

        ServerThread x = new ServerThread(self);
        x.run();
    }
    
}

