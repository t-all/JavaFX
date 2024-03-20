package com.example.fx;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Пример JavaFX");

        String text = "Этот  Tp-книга адресована всем Dp, кто изучает русский язык Tp.";

        // Замены для меток
        String dpReplacement = "Олегу"; // Замена для метки Dp
        String tpReplacement = "молотком"; // Замена для метки Tp

        // Замена меток на значения
        text = text.replace("Dp", dpReplacement);
        text = text.replace("Tp", tpReplacement);

        TextFlow textFlow = new TextFlow(new Text(text));

        VBox vBox = new VBox(textFlow);
        Scene scene = new Scene(vBox, 600, 400);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

