package com.example.goldfinder.server;


import java.util.Random;

public class AppServer {

    public static final int  ROW_COUNT = 5;
    public static final int COLUMN_COUNT = 5;
    final static int serverPort = 1234;

    public static void main(String[] args)  {

        Grid grid = new Grid(COLUMN_COUNT, ROW_COUNT, new Random());
        System.out.println("server should be listening on port " + serverPort);
        Server server = new MonoPlayerServer(grid,serverPort);
        server.start();
    }

}