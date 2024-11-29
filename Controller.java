package com.example.goldfinder;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;

import static com.example.goldfinder.server.AppServer.COLUMN_COUNT;
import static com.example.goldfinder.server.AppServer.ROW_COUNT;

public class Controller {

    @FXML
    Canvas gridCanvas;
    @FXML
    Label score;

    GridView gridView;
    int column, row;
    Client client;

    public void initialize() {
        this.gridView = new GridView(gridCanvas, COLUMN_COUNT, ROW_COUNT);
        score.setText("0");
        gridView.repaint();
        column = 2; row = 2; // plutot avoir une methode qui renvoi une position aleatoire
        gridView.paintToken(column, row);
    }

    public void pauseToggleButtonAction(ActionEvent actionEvent) {
    }

    public void playToggleButtonAction(ActionEvent actionEvent) {
    }

    public void oneStepButtonAction(ActionEvent actionEvent) {
    }

    public void restartButtonAction(ActionEvent actionEvent) {
    }

    public void handleMove(KeyEvent keyEvent) {
            switch (keyEvent.getCode()) {
                case J -> {
                    client.sendSurrounding(column, row);
                    String response = client.getServerResponse();
                    updateGridView(response, column, row);
                }
                case Z -> {
                    client.sendMove("UP",column,row);
                    String response = client.getServerResponse();
                    if(response.startsWith("VALID")){
                        row = Math.max(0, row - 1);
                        removeGoldAt(column,row);
                    }

                } // en appuyant ça , le client dans envoyer (up, column, row)
                case Q -> {
                    client.sendMove("LEFT",column,row);
                    String response = client.getServerResponse();
                    if(response.startsWith("VALID")){
                        column = Math.max(0, column - 1);
                        removeGoldAt(column,row);
                    }
                }
                case S -> {
                    client.sendMove("DOWN",column,row);
                    String response = client.getServerResponse();
                    if(response.startsWith("VALID")){
                        row = Math.min(ROW_COUNT-1, row + 1);
                        removeGoldAt(column,row);
                    }
                }
                case D -> {
                    client.sendMove("RIGHT",column,row);
                    String response = client.getServerResponse();
                    if(response.startsWith("VALID")){
                        column = Math.min(COLUMN_COUNT-1, column +1);
                        removeGoldAt(column,row);
                    }
                }
            }
            client.sendSurrounding(column,row);
            String surrounding = client.getServerResponse();
            updateGridView(surrounding,column,row);
            client.score();
            String playerScore = client.getServerResponse();
            score.setText(playerScore);
            gridView.repaint();
            gridView.paintToken(column, row);
    }

    public void setClient(Client client){
        this.client = client;
    }


    public void updateGridView(String response, int x, int y){
        String[] parts = response.split(" ");
        for (String dirItem : parts){
            String[] str = dirItem.split(":");
            String dir = str[0];
            switch (dir){
                case "UP" :
                    if (str[1].trim().equals("WALL")){
                        gridView.hWall[x][y] = true;
                    }
                    else if(str[1].trim().equals("GOLD")){
                        gridView.goldAt[x][y-1] = true;
                    }
                    else if (str[1].trim().equals("PLAYER")){
                        gridView.putOppsAt(x,y-1);
                    }
                    else{
                        if(gridView.opps[x][y-1]){
                            gridView.removeOppsAt(x,y-1);
                        }
                    }
                    break;
                case "RIGHT" :
                    if (str[1].trim().equals("WALL")){
                        gridView.vWall[x+1][y] = true;
                    }
                    else if(str[1].trim().equals("GOLD")){
                        gridView.goldAt[x+1][y] = true;
                    }
                    else if(str[1].trim().equals("PLAYER")){
                        gridView.putOppsAt(x+1,y);
                    }
                    else {
                        if(gridView.opps[x+1][y]){
                            gridView.removeOppsAt(x+1,y);
                        }
                    }
                    break;
                case "DOWN" :
                    if (str[1].trim().equals("WALL")){
                        gridView.hWall[x][y+1] = true;
                    }
                    else if(str[1].trim().equals("GOLD")){
                        gridView.goldAt[x][y+1] = true;
                    }
                    else if(str[1].trim().equals("PLAYER")) {
                        gridView.putOppsAt(x, y + 1);
                    }
                    else{
                        if(gridView.opps[x][y+1]){
                            gridView.removeOppsAt(x,y+1);
                        }
                    }
                    break;
                case "LEFT" :
                    if (str[1].trim().equals("WALL")){
                        gridView.vWall[x][y] = true;
                    }
                    else if(str[1].trim().equals("GOLD")){
                        gridView.goldAt[x-1][y] = true;
                    }
                    else if(str[1].trim().equals("PLAYER")){
                        gridView.putOppsAt(x-1,y);
                    }
                    else{
                        if(gridView.opps[x-1][y]){
                            gridView.removeOppsAt(x-1,y);
                        }
                    }
                    break;
            }
        }
    }

    public void removeGoldAt(int column, int row){
        if(gridView.goldAt[column][row]){
            gridView.goldAt[column][row] = false;
        }
    }

}

