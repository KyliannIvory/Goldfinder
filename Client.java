package com.example.goldfinder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Client {

    Controller controller;
    int serverPort;
    String serverAddress;
    BufferedReader in;
    OutputStream out;
    String name;

    public Client (Controller controller, int serverPort, String serverAddress, String name){
        this.controller = controller;
        this.serverPort = serverPort;
        this.serverAddress = serverAddress;
        this.name = name;
    }

    public void sendGameJoin(String name){
        try {
            String s = "GAME_JOIN:"+ name + "\n";
            out.write(s.getBytes());
            System.out.println("on est dans sendGameJoin");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void sendSurrounding(int column, int row){
        try {
            StringBuilder sb = new StringBuilder("SURROUNDING");
            sb.append(" ").append(column).append(" ").append(row).append("\n");
            out.write(sb.toString().getBytes());
            System.out.println("surrounding");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMove(String move ,int column, int row){
        try {

            StringBuilder sb = new StringBuilder(move);
            sb.append(" ").append(column).append(" ").append(row).append("\n");
            out.write(sb.toString().getBytes());
            System.out.println(sb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void start(){
        try {
            Socket socket = new Socket(serverAddress,serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = socket.getOutputStream();
            sendGameJoin(name);
            String position = getServerResponse();
            String [] part = position.split(" ");
            int column = Integer.parseInt(part[0]);
            int row = Integer.parseInt(part[1]);
            controller.column = column;
            controller.row = row;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getServerResponse(){
        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void score(){
        StringBuilder sb = new StringBuilder("SCORE\n");
        try {
            out.write(sb.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
