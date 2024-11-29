package com.example.goldfinder.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

import static com.example.goldfinder.server.AppServer.COLUMN_COUNT;
import static com.example.goldfinder.server.AppServer.ROW_COUNT;

public class MonoPlayerServer extends Server{

    OutputStream out;
    BufferedReader in;
    HashMap<String,Integer> players;
    String playerName;
    boolean gameStarted = false;


    public MonoPlayerServer(Grid grid , int port){
        super(grid,port);
        players = new HashMap<>();
    }

    public void start(){
        try {
            ServerSocket serverSocket = new ServerSocket(serverPort);
            System.out.println("en attente de connexion");
            Socket socket = serverSocket.accept();
            System.out.println("nouvelle connexion");

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = socket.getOutputStream();

            while(!isGameOver()){
                handleRequest();
            }
            gameEnd();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void handleRequest(){
        try {
            String request = in.readLine();
            if(request.startsWith("GAME_JOIN:")){
                handleGameJoin(request);
            }
            else if(request.startsWith("SURROUNDING")){

                String[] part = request.split(" ");
                int column = Integer.parseInt(part[1]);
                int row = Integer.parseInt(part[2]);
                handleSurrounding(column,row);

            } else if (request.equals("SCORE")) {
                handleScore();
            } else{
                String[] part = request.split(" ");
                String move = part[0];
                int column = Integer.parseInt(part[1]);
                int row = Integer.parseInt(part[2]);
                handleMove(move,column,row);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void handleGameJoin(String msg){
        String[] parts = msg.trim().split(":");
        playerName = parts[1];
        players.put(playerName,0);
        giveRandomPosition();
        gameStart();
    }
    public void gameStart(){
        StringBuilder sb = new StringBuilder("GAME_START ");
        int i = 0;
        for(String player : players.keySet()){
            sb.append(player).append(":").append(i).append(" ");
            i++;
        }
        sb.append("END\n");
        System.out.println(sb);
        //out.write(sb.toString().getBytes());
        gameStarted = true;

    }
    public void gameEnd(){
        StringBuilder sb = new StringBuilder("GAME_END ");
        for(String player : players.keySet()){
            sb.append(player).append(":").append(players.get(player)).append(" ");

        }
        sb.append("END\n");
        try {
            out.write(sb.toString().getBytes());
            System.out.println(sb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void handleSurrounding(int column, int row){
        StringBuilder sb = new StringBuilder("UP:");
        if(grid.upWall(column,row)){
            sb.append("WALL ");
        } else if (grid.hasGold(column, Math.max(0,row-1))) {
            sb.append("GOLD ");
        } else{
            sb.append("EMPTY ");
        }

        sb.append("RIGHT:");
        if(grid.rightWall(column,row)){
            sb.append("WALL ");
        } else if (grid.hasGold(Math.min(column+1, grid.columnCount-1), row)) {
            sb.append("GOLD ");
        } else{
            sb.append("EMPTY ");
        }

        sb.append("DOWN:");
        if(grid.downWall(column,row)){
            sb.append("WALL ");
        } else if (grid.hasGold(column, Math.min(row+1, grid.rowCount-1))) {
            sb.append("GOLD ");
        } else{
            sb.append("EMPTY ");
        }
        sb.append("LEFT:");
        if(grid.leftWall(column,row)){
            sb.append("WALL ");
        } else if (grid.hasGold(Math.max(0,column-1), row)) {
            sb.append("GOLD ");
        } else{
            sb.append("EMPTY ");
        }
        sb.append("\n");
        try {
            System.out.println(sb);
            out.write(sb.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void handleMove(String direction, int column, int row){
        StringBuilder sb = new StringBuilder();
        boolean validmove = false;
        boolean foundGold = false;
        switch (direction){
            case "UP":
                if(!grid.upWall(column, row)){
                    validmove = true;
                    grid.removePlayerAt(column, row);
                    grid.putPlayerAt(column,Math.max(0, row - 1));
                    if(grid.hasGold(column, Math.max(0, row - 1))){
                        foundGold = true;
                        grid.collectGoldAt(column,Math.max(0, row - 1));
                        incrementScore();
                    }
                }
                break;
            case "RIGHT":
                if(!grid.rightWall(column, row)){
                    validmove = true;
                    grid.removePlayerAt(column, row);
                    grid.putPlayerAt(Math.min(COLUMN_COUNT-1, column +1), row);
                    if(grid.hasGold(Math.min(COLUMN_COUNT-1, column +1), row)){
                        foundGold = true;
                        grid.collectGoldAt(Math.min(COLUMN_COUNT-1, column +1), row);
                        incrementScore();
                    }
                }
                break;
            case "DOWN":
                if(!grid.downWall(column, row)){
                    validmove = true;
                    grid.removePlayerAt(column, row);
                    grid.putPlayerAt(column, Math.min(ROW_COUNT-1, row + 1));
                    if(grid.hasGold(column, Math.min(ROW_COUNT-1, row + 1))) {
                        foundGold = true;
                        grid.collectGoldAt(column, Math.min(ROW_COUNT-1, row + 1));
                        incrementScore();
                    }
                }
                break;
            case "LEFT":
                if(!grid.leftWall(column, row)){
                    validmove = true;
                    grid.removePlayerAt(column, row);
                    grid.putPlayerAt(Math.max(0, column - 1), row);
                    if(grid.hasGold( Math.max(0, column - 1), row)) {
                        foundGold = true;
                        grid.collectGoldAt(Math.max(0, column - 1), row);
                        incrementScore();
                    }
                }
                break;
        }
        if(validmove){
            sb.append("VALIDMOVE:");
            if(foundGold){
                sb.append("GOLD");
            } else {
                sb.append("EMPTY");
            }
        } else{
            sb.append("INVALIDMOVE");
        }
        sb.append("\n");
        try {
            out.write(sb.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void giveRandomPosition(){
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int column , row;

        do{
            column = random.nextInt(grid.columnCount);
            row = random.nextInt(grid.rowCount);
        } while(grid.hasGold(column,row) || grid.hasPlayerAt(column,row));

        grid.putPlayerAt(column,row);

        sb.append(column).append(" ").append(row).append("\n");
        try {
            out.write(sb.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleScore(){
        StringBuilder sb = new StringBuilder();
        String score = players.get(playerName).toString();
        sb.append(score).append("\n");
        try {
            out.write(sb.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    boolean isGameOver(){
        return grid.areAllCellsVisited() && (!grid.hasRemainingGold());
    }

    public void incrementScore(){
        int score = players.get(playerName);
        players.put(playerName,score+1);
    }
}
