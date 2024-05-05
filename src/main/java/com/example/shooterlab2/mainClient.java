package com.example.shooterlab2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class mainClient extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(mainClient.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 853, 566);
        stage.setResizable(false);
        stage.setTitle("Target Sniper");

        GameController controller = fxmlLoader.getController();
        stage.setOnHidden(e -> controller.shutDown());

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }



//    public static void launchIt() {
//        launch();
//    }
}
