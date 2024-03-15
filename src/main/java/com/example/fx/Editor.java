package com.example.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Editor extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("scene_menu.fxml"));
        Parent parent = loader.load();
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("logo_bw.png")));
        primaryStage.setScene(new Scene(parent));
        primaryStage.setTitle("Редактор шаблонов");
        primaryStage.show();

        Controller controller = loader.getController();
    }

}