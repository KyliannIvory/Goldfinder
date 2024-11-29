package com.example.goldfinder.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.function.DoubleToIntFunction;

import static com.example.goldfinder.server.AppServer.COLUMN_COUNT;
import static com.example.goldfinder.server.AppServer.ROW_COUNT;

public class MultiPlayerServer extends Server{


    HashMap<SocketChannel,Player> players;
    boolean gameStarted = false;
    int numberOfPlayer = 0;
    int numberOfGameJoin = 0;
    static final int MAXPLAYERS = 2;




    public MultiPlayerServer(Grid grid, int serverPort){
        super(grid, serverPort);
        players = new HashMap<>();
    }

    public boolean isGameOver(){
        return grid.areAllCellsVisited() && (!grid.hasRemainingGold());
    }

    @Override
    public void start() {
        try {

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress("localhost", 1234));
            Selector selector = Selector.open();
            serverSocketChannel.configureBlocking(false);

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("Serveur multi-joueur démarré...");

            while(!isGameOver()){

                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {

                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if(key.isAcceptable() && (numberOfPlayer != MAXPLAYERS) ){

                        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                        SocketChannel clientChannel = serverChannel.accept();
                        clientChannel.configureBlocking(false);
                        clientChannel.register(selector, SelectionKey.OP_READ);
                        System.out.println("Nouvelle connexion acceptée.");
                        numberOfPlayer++;

                    } else if (key.isReadable()) {
                        readClientMessage(key);
                    }
                }
            }
            gameEnd();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readClientMessage(SelectionKey key) throws IOException {

        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {

            System.out.println("Connexion fermée par le client.");
            clientChannel.close();
        }

        else{

            buffer.flip();
            byte[] data = new byte[bytesRead];
            buffer.get(data);
            String request = new String(data);
            if (request.endsWith("\n")) {
                request = request.substring(0, request.length() - 1);
            }
            handleRequest(clientChannel,request);

        }
    }

    public void handleRequest(SocketChannel clientChannel, String request){
        if(!gameStarted){
            if(request.startsWith("GAME_JOIN:")){
                handleGameJoin(clientChannel,request);
                if(numberOfGameJoin == MAXPLAYERS)
                    gameStart();
            }
        } else {

            if (request.startsWith("SURROUNDING")) {

                String[] part = request.split(" ");
                int column = Integer.parseInt(part[1]);
                int row = Integer.parseInt(part[2]);
                handleSurrounding(clientChannel, column, row);
            }
            else if (request.equals("SCORE")){
                handleScore(clientChannel);
            }
            else{
                String[] part = request.split(" ");
                String move = part[0];
                System.out.println(part[0]);
                System.out.println(part[1]);
                System.out.println(part[2]);
                int column = Integer.parseInt(part[1]);
                int row = Integer.parseInt(part[2]);
                handleMove(clientChannel,move,column,row);
            }
        }
    }

    public void handleGameJoin(SocketChannel clientChannel, String msg){
        numberOfGameJoin++;
        String[] parts = msg.trim().split(":");
        String name = parts[1];
        players.put(clientChannel,new Player(name));
        giveRandomPosition(clientChannel);
       // if(numberOfGameJoin == numberOfPlayer)
        //    gameStart();
    }

    private void giveRandomPosition(SocketChannel clientChannel) {

        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int column , row;

        do{
            column = random.nextInt(grid.columnCount);
            row = random.nextInt(grid.rowCount);
        } while( grid.hasGold(column,row) || grid.hasPlayerAt(column,row));

        grid.putPlayerAt(column,row);
        sb.append(column).append(" ").append(row).append(" ").append("\n");
        try {
            clientChannel.write(ByteBuffer.wrap(sb.toString().getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void gameStart(){
        StringBuilder sb = new StringBuilder("GAME_START ");
        int i = 0;
        for(Player player : players.values()){
            sb.append(player.getName()).append(":").append(i).append(" ");
            i++;
        }
        sb.append("END\n");
        System.out.println(sb);
        gameStarted = true;

    }

    public void gameEnd(){
        StringBuilder sb = new StringBuilder("GAME_END ");
        for(Player player : players.values()){
            sb.append(player.getName()).append(":").append(player.getScore()).append(" ");
        }
        sb.append("END\n");
        System.out.println(sb);
    }

    public void handleSurrounding(SocketChannel clientChannel, int column, int row){

        StringBuilder sb = new StringBuilder("UP:");
        if(grid.upWall(column, row)){
            sb.append("WALL ");
        } else if (grid.hasGold(column, Math.max(0,row-1))) {
            sb.append(("GOLD "));
        } else if (grid.hasPlayerAt(column, Math.max(0,row-1))) {
            sb.append("PLAYER ");
        } else{
            sb.append("EMPTY ");
        }

        sb.append("RIGHT:");
        if(grid.rightWall(column,row)){
            sb.append("WALL ");
        } else if (grid.hasGold(Math.min(column+1, grid.columnCount-1), row)) {
            sb.append("GOLD ");
        } else if(grid.hasPlayerAt(Math.min(column+1, grid.columnCount-1), row)) {
            sb.append("PLAYER ");
        } else{
            sb.append("EMPTY ");
        }


        sb.append("DOWN:");
        if(grid.downWall(column, row)){
            sb.append("WALL ");
        } else if (grid.hasGold(column, Math.min(row+1, grid.rowCount-1))) {
            sb.append("GOLD ");
        } else if (grid.hasPlayerAt(column, Math.min(row+1, grid.rowCount-1))) {
            sb.append("PLAYER ");
        } else{
            sb.append("EMPTY ");
        }

        sb.append("LEFT:");
        if(grid.leftWall(column,row)){
            sb.append("WALL ");
        } else if (grid.hasGold(Math.max(0,column-1), row)) {
            sb.append("GOLD ");
        } else if (grid.hasPlayerAt(Math.max(0,column-1), row)) {
            sb.append("PLAYER ");
        } else{
            sb.append("EMPTY ");
        }

        sb.append("\n");
        System.out.println(sb);
        try {
            clientChannel.write(ByteBuffer.wrap(sb.toString().getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleMove(SocketChannel clientChannel, String direction, int column, int row){
        StringBuilder sb = new StringBuilder();
        Player player = players.get(clientChannel);
        boolean validmove = false;
        boolean foundGold = false;
        boolean foundPlayer = false;
        switch(direction){
            case "UP":
                if(grid.hasPlayerAt(column,Math.max(0, row - 1))){
                    foundPlayer = true;
                }
                else{
                    if(!grid.upWall(column, row)){
                        validmove = true;
                        grid.removePlayerAt(column, row);
                        grid.putPlayerAt(column,Math.max(0, row - 1));
                        if(grid.hasGold(column, Math.max(0, row - 1))){
                            foundGold = true;
                            grid.collectGoldAt(column,Math.max(0, row - 1));
                            player.incrementScore();
                        }
                    }

                }
                break;
            case "RIGHT":
                if(grid.hasPlayerAt(Math.min(COLUMN_COUNT-1, column +1), row)){
                    foundPlayer = true;
                }
                else{
                    if(!grid.rightWall(column, row)){
                        validmove = true;
                        grid.removePlayerAt(column, row);
                        grid.putPlayerAt(Math.min(COLUMN_COUNT-1, column +1), row);
                        if(grid.hasGold(Math.min(COLUMN_COUNT-1, column +1), row)){
                            foundGold = true;
                            grid.collectGoldAt(Math.min(COLUMN_COUNT-1, column +1), row);
                            player.incrementScore();
                        }
                    }
                }
                break;
            case "DOWN":
                if(grid.hasPlayerAt(column, Math.min(ROW_COUNT-1, row + 1))){
                    foundPlayer = true;
                }
                else{
                    if(!grid.downWall(column, row)){
                        validmove = true;
                        grid.removePlayerAt(column, row);
                        grid.putPlayerAt(column, Math.min(ROW_COUNT-1, row + 1));
                        if(grid.hasGold(column, Math.min(ROW_COUNT-1, row + 1))) {
                            foundGold = true;
                            grid.collectGoldAt(column, Math.min(ROW_COUNT-1, row + 1));
                            player.incrementScore();
                        }
                    }
                }
                break;
            case "LEFT":
                if(grid.hasPlayerAt(Math.max(0, column - 1), row)){
                    foundPlayer = true;
                }
                else{
                    if(!grid.leftWall(column, row)){
                        validmove = true;
                        grid.removePlayerAt(column, row);
                        grid.putPlayerAt(Math.max(0, column - 1), row);
                        if(grid.hasGold( Math.max(0, column - 1), row)) {
                            foundGold = true;
                            grid.collectGoldAt(Math.max(0, column - 1), row);
                            player.incrementScore();
                        }
                    }
                }
                break;
        }
        if(validmove){
            sb.append("VALIDMOVE:");
            if(foundGold){
                sb.append("GOLD");
            }else{
                sb.append("EMPTY");
            }
        } else{
            sb.append("INVALIDMOVE");
        }
        sb.append("\n");
        try {
            clientChannel.write(ByteBuffer.wrap(sb.toString().getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void handleScore(SocketChannel clientChannel){
        StringBuilder sb = new StringBuilder();
        Integer score = players.get(clientChannel).getScore();
        String response = score.toString();
        sb.append(response).append("\n");
        try {
            clientChannel.write(ByteBuffer.wrap(sb.toString().getBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
