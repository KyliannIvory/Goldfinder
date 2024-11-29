package com.example.goldfinder;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class Bot extends javafx.application.Application {
    private static final String VIEW_RESOURCE_PATH = "/com/example/goldfinder/gridView.fxml";
    private static final String APP_NAME = "Gold Finder";

    private Stage primaryStage;
    private Parent view;
    private static Controller controller;
    private static Client client;

    private void initializePrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle(APP_NAME);
        this.primaryStage.setOnCloseRequest(event -> Platform.exit());
        this.primaryStage.setResizable(true);
        this.primaryStage.sizeToScene();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        initializePrimaryStage(primaryStage);
        initializeView();
        showScene();
        client = new Client(controller, 1234, "localhost", "Kyliann");
        controller.setClient(client);
        client.start();

        // Créer et démarrer le thread du bot
        Thread botThread = new Thread(() -> {
            while (true) {
                // Simuler un déplacement aléatoire
                simulateRandomMove();
                try {
                    // Attendre un court laps de temps entre chaque déplacement
                    Thread.sleep(1000); // Attendre 1 seconde entre chaque déplacement
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        botThread.setDaemon(true); // Le thread du bot est un thread daemon pour qu'il se termine lorsque l'application se ferme
        botThread.start();
    }

    // Méthode pour simuler un déplacement aléatoire
    private void simulateRandomMove() {
        // Générer un déplacement aléatoire parmi les touches disponibles
        Random random = new Random();
        int randomDirection = random.nextInt(4); // Générer un nombre aléatoire entre 0 et 3 inclus
        switch (randomDirection) {
            case 0:
              //  Platform.runLater(() -> controller.handleMove(KeyCode.Z);
                break;
            case 1:
                Platform.runLater(() -> controller.handleMove(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED, "", "", javafx.scene.input.KeyCode.DOWN, false, false, false, false)));
                break;
            case 2:
                Platform.runLater(() -> controller.handleMove(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED, "", "", javafx.scene.input.KeyCode.LEFT, false, false, false, false)));
                break;
            case 3:
                Platform.runLater(() -> controller.handleMove(new javafx.scene.input.KeyEvent(javafx.scene.input.KeyEvent.KEY_PRESSED, "", "", javafx.scene.input.KeyCode.RIGHT, false, false, false, false)));
                break;
        }
    }

    private void initializeView() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        URL location = Bot.class.getResource(VIEW_RESOURCE_PATH);
        loader.setLocation(location);
        view = loader.load();
        controller = loader.getController();
        // Ne plus lier le contrôleur à la saisie du clavier
        //view.setOnKeyPressed(controller::handleMove);
        controller.initialize();
    }

    private void showScene() {
        Scene scene = new Scene(view);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
