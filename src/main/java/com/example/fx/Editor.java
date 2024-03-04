package com.example.fx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

public class Editor extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("scene_menu.fxml"));
        Parent parent = loader.load();

        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Редактор шаблонов");
        primaryStage.show();
        Controller controller = loader.getController();
    }

    public static void hideHTMLEditorToolbars(final HTMLEditor editor) {
        editor.setVisible(false);
        Platform.runLater(() -> {
            Node[] nodes = editor.lookupAll(".tool-bar").toArray(new Node[0]);
            for (Node node : nodes) {
                node.setVisible(false);
                node.setManaged(false);
            }
            editor.setVisible(true);
        });
    }

}
