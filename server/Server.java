package com.example.goldfinder.server;

public abstract class Server {

    protected Grid grid;
    protected int serverPort;

    public Server(Grid grid, int serverPort){
        this.grid = grid;
        this.serverPort = serverPort;
    }

    public abstract void start();
}
