package com.example.goldfinder.server;

import java.util.Random;

public class Grid {
    boolean[][] hWall, vWall, gold, visitedCell, player;
    int columnCount, rowCount, remainingGold, unvisitedCell;

    private final Random random;
    public Grid(int columnCount, int rowCount, Random random) {
        this.columnCount = columnCount;
        this.rowCount = rowCount;
        this.random = random;
        unvisitedCell = columnCount*rowCount;

        RandomMaze randomMaze = new RandomMaze(columnCount,rowCount,.1, random);
        randomMaze.generate();
        hWall = randomMaze.hWall;
        vWall = randomMaze.vWall;

        gold = new boolean [columnCount][rowCount];
        visitedCell = new boolean [columnCount][rowCount];
        player = new boolean [columnCount][rowCount];
        generateGold(3);

        initialiseVisitedCell();
        initialisePlayerPosition();
    }

    private void generateGold(double v) {
        for(int column=0; column<columnCount; column++)
            for(int row=0;row<rowCount; row++)
                if(random.nextInt(10)<v){
                    gold[column][row] = true;
                    remainingGold++;
                }
    }

    boolean leftWall(int column, int row){
        if (column==0) return true;
        return vWall[column][row];
    }

    boolean rightWall(int column, int row){
        if (column==columnCount-1) return true;
        return vWall[column+1][row];
    }

    boolean upWall(int column, int row){
        if (row==0) return true;
        return hWall[column][row];
    }

    boolean downWall(int column, int row){
        if (row==rowCount-1) return true;
        return hWall[column][row+1];
    }

    boolean hasGold(int column, int row){
        return gold[column][row];
    }

    public void collectGold(){
        if(hasRemainingGold()){
            remainingGold--;
        }
    }

    public void collectGoldAt(int column, int row){
        if(gold[column][row]){
            gold[column][row] = false;
            collectGold();
        }
    }

    public boolean hasRemainingGold(){
        return remainingGold != 0;
    }

    public boolean areAllCellsVisited(){
        return unvisitedCell == 0;
    }

    public void visitedCell(){
        if(!areAllCellsVisited()){
            unvisitedCell --;
        }
    }

    public void visitCellAt(int column ,int row){
        if(!visitedCell[column][row]){
            visitedCell[column][row] = true;
            visitedCell();
        }
    }

    public void initialiseVisitedCell(){
        for (int i = 0; i < columnCount; i++){
            for (int j = 0; j < rowCount; j++){
                visitedCell[i][j] = false;
            }
        }
    }

    public void initialisePlayerPosition(){
        for(int x = 0; x < columnCount; x++){
            for (int y = 0; y < rowCount; y++){
                player[x][y] = false;
            }
        }
    }

    public boolean hasPlayerAt(int column, int row){
        return player[column][row];
    }

    public void putPlayerAt(int column, int row){
        player[column][row] = true;
        visitCellAt(column,row);
    }

    public void removePlayerAt(int column, int row){
        player[column][row] = false;
    }
}
