package com.mycompany.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

public class Node {
    final public static int MAX_RANGE = 1023;
    public int id;
    public int port;
    public InetAddress address;
    public HashMap<Integer, String> map = new HashMap<>();
    public int[] keyRange = new int[2]; //begin, end
    public InetAddress pAddress;//predecessor address
    public int pPort; 
    public InetAddress sAddress; //successor address
    public int sPort;

    public Node() {}
    public Node(int id, int port) /*throws IOException*/{
        this.id = id;
        this.port = port;
        this.keyRange[0] = id;
    }

    public static boolean inRange(int key, int[] keyRange) {
        if (keyRange == null) {
            return false;
        }
        //two cases -> low,high high,low 
        boolean regular = keyRange[0] < keyRange[1];
        if (regular) {
            return (key >= keyRange[0] && key <= keyRange[1]);
        }
        return (key >= keyRange[0] && key <= MAX_RANGE || key <= keyRange[1] && key >= 0);
    }

    public static int keyRange(int[] keyRange) {
        if (keyRange[0] < keyRange[1]) {
            return 1 + keyRange[1] - keyRange[0];
        } else {
            return 2 + MAX_RANGE - keyRange[0] + keyRange[1];
        }
    }

    public static Find find(int key, Node node) throws IOException{
        Find res = new Find();
        res.address = node.sAddress;
        res.port = node.sPort;
        int[] range = node.keyRange;
        Socket next;
        PrintWriter out;
        BufferedReader in;

        //if not self
        while (!inRange(key, range)){
            //contact next node
            next = new Socket(res.address, res.port);
            out = new PrintWriter(next.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(next.getInputStream()));

            //send find request to node
            out.println("find " + key);

            //get response yes: "yes id" no: "no id ip port"
            String response = in.readLine();
            System.out.println(response);
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

        //if self
        res.address = node.address;
        res.port = node.port;
        return res;
    }

    public static void sendM(Find address, String message) throws IOException{
        //contact node
        Socket node = new Socket(address.address, address.port);
        PrintWriter out = new PrintWriter(node.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(node.getInputStream()));
        out.println(message);

        //get response
        String response = in.readLine();
        System.out.println(response);
        node.close();
    }

    public static void insert(int key, String value, Find address) throws IOException{
        //contact node
        Socket node = new Socket(address.address, address.port);
        PrintWriter out = new PrintWriter(node.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(node.getInputStream()));
        out.println("insert " + key + " " + value);

        //get response ok or error
        String response = in.readLine();
        System.out.println(response);
        node.close();
    }

    public static Find enterB(int id, Find address) throws IOException{
        //contact boot
        Socket node = new Socket(address.address, address.port);
        PrintWriter out = new PrintWriter(node.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(node.getInputStream()));
        out.println("enter-b " + id);

        //get address of node to enter into
        String[] response = in.readLine().split(" ");
        node.close();
        Find enter = new Find();
        enter.address = InetAddress.getByName(response[0]);
        enter.port = Integer.parseInt(response[1]);
        return enter;
    }

    public static void enter(int key, Find address, Node self) throws IOException {
        //contact entering node
        Socket node = new Socket(address.address, address.port);
        PrintWriter out = new PrintWriter(node.getOutputStream(), true);
        out.println("enter-help");

        //send new key range-> if first = 0, that is the bootid so it is a special case
        int[] newRange = {self.keyRange[0], key};
        if (self.keyRange[0] == 0) {
            out.println("" + (self.keyRange[0] + 1) + " " + key);
            int[] temp = {self.keyRange[0] + 1, key};
            newRange = temp;
        } else {
            out.println("" + (self.keyRange[0] + " " + key));
        }

        //send keys
        int nKeys = Node.keyRange(newRange);
        out.println(nKeys);
        //two cases -> low,high high,low
        if (newRange[0] < newRange[1]) {
            for (int i = newRange[0]; i <= newRange[1]; i++) {
                out.println("" + i + " " + self.map.get(i));
                self.map.remove(i);
            }
        } else {
            for (int i = newRange[0]; i <= MAX_RANGE; i++) {
                out.println("" + i + " " + self.map.get(i));
                self.map.remove(i);
            }
            for (int i = 0; i <= newRange[1]; i++) {
                out.println("" + i + " " + self.map.get(i));
                self.map.remove(i);
            }
        }

        //send predecessor details
        out.println(self.pAddress.getHostName() + " " + self.pPort);
        //send sucessor deatils
        out.println(self.address.getHostName() + " " + self.port);
        node.close();

        //update key range of self
        int[] range = {key + 1, self.id};
        self.keyRange = range;

        //update sucessor of predecessor
        node = new Socket(self.pAddress, self.pPort);
        out = new PrintWriter(node.getOutputStream(), true);
        out.println("update-s " + address.address.getHostName() + " " + address.port);
        node.close();

        //update predecessor of self
        self.pAddress = address.address;
        self.pPort = address.port;
    }

    public static void exit(Node self) throws IOException {
        //Contact the predecessor and have it update its successor to the exiting node's successor
        Socket predSocket = new Socket(self.pAddress, self.pPort);
        PrintWriter predOut = new PrintWriter(predSocket.getOutputStream(), true);
        String update = "update-s " + self.sAddress + " " + self.sPort;
        predOut.println(update);

        //Close the predecessor connections
        predSocket.close();
        predOut.close();



        //Open a socket to communicate with the exiting node's successor
        Socket successorSocket = new Socket(self.sAddress, self.sPort);
        PrintWriter successorOut = new PrintWriter(successorSocket.getOutputStream(), true);

        //Have the exiting node's successor update its predecessor to the exiting node's predecessor
        update = "update-p " + self.pAddress + " " + self.pPort;
        successorOut.println(update);

        //Hand over the exiting node's key range to its successor
        String exitNodeRange = "exit-update-key-range " + self.keyRange[0] + " " + self.keyRange[1];
        successorOut.println(exitNodeRange);

        //Hand over the exiting node's key value pairs to its successor.
        successorOut.println(buildKeyValuePairsString(self));

        //Close the successor connections
        successorSocket.close();
        successorOut.close();
    }

    private static String buildKeyValuePairsString(Node self) {
        StringBuilder bldr = new StringBuilder("exit-update-key-values ");
        int[] keyRange = {self.keyRange[0], self.keyRange[1]};
        
        //To handle the high-low case.
        if (keyRange[0] > keyRange[1]) {
            int placeholder = keyRange[0];
            keyRange[0] = keyRange[1];
            keyRange[1] = placeholder;
        }

        for (int i = keyRange[0]; i < keyRange[1]; i++) {
            String valueToAdd = self.map.get(i);
            if (valueToAdd != null) {
                bldr.append(i + " " + valueToAdd + " ");
            }
        }

        return bldr.toString();
    }
}
