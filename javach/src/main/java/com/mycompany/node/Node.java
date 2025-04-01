package com.mycompany.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Node {
    public int id;
    public int port;
    public ServerSocket sock;
    public HashMap<Integer, String> map = new HashMap<>();
    public int[] keyRange = new int[2]; //begin, end
    public InetAddress pAddress;//predecessor address
    public int pPort; 
    public InetAddress sAddress; //successor address
    public int sPort;

    public Node() {}
    public Node(int id, int port) throws IOException{
        this.id = id;
        this.port = port;
        this.sock = new ServerSocket(port);
    }

    public boolean inRange(int key, int[] keyRange) {
        if (keyRange == null) {
            return false;
        }
        if (key <= keyRange[1] && key >= keyRange[0]) {
            return true;
        }
        return false;
    }

    public Find find(int key) throws IOException{
        Find res = new Find();
        res.address = sAddress;
        res.port = sPort;
        int[] range = new int[2];
        Socket next;
        PrintWriter out;
        BufferedReader in;
        
        while (!inRange(key, range)){
            //contact next node
            next = new Socket(res.address, res.port);
            out = new PrintWriter(next.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(next.getInputStream()));

            //send find request to node
            out.println("find " + key);

            //get response yes: "yes id" no: "no id ip port"
            String response = in.readLine();
            next.close(); //only need to get response
            String[] resp = response.split(" ");
            res.ids.add(Integer.parseInt(resp[1]));
            if (resp[0].equals("yes")) {
                //node found
                return res;
            } 
            //node not found
            res.address = InetAddress.getByName(resp[2]);
            res.port = Integer.parseInt(resp[3]);
        }
        return null;
    }
}
