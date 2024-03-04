package com.example.fx;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.poi.xwpf.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create a VBox layout to hold the labels
        VBox vbox = new VBox();
        vbox.setSpacing(10);

        try {
            // Load the docx file
            FileInputStream fis = new FileInputStream("C:\\Java_projects\\FX\\src\\main\\resources\\com\\example\\fx\\example.docx");
            XWPFDocument doc = new XWPFDocument(fis);
            fis.close();

            // Get the paragraphs from the document
            List<XWPFParagraph> paragraphs = doc.getParagraphs();

            // Iterate through paragraphs
            for (XWPFParagraph paragraph : paragraphs) {
                // Get the text of the paragraph
                String text = paragraph.getText();

                // Get the indentation level of the paragraph
                int indentation = getIndentationLevel(paragraph);

                // Create a label with the text and apply indentation
                Label label = new Label(text);
                label.setTranslateX(indentation); // Apply indentation

                // Add the label to the VBox
                vbox.getChildren().add(label);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a scene and set it on the stage
        Scene scene = new Scene(vbox, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Indented Text");
        primaryStage.show();
    }

    private int getIndentationLevel(XWPFParagraph paragraph) {
        // Get indentation in inches
        int indentationInTwips = paragraph.getIndentationLeft();
        // 1 inch = 1440 twips
        double indentationInInches = indentationInTwips / 1440.0;
        // Convert inches to pixels (assuming 96 pixels per inch)
        int indentationInPixels = (int) (indentationInInches * 96);
        return indentationInPixels;
    }

    public static void main(String[] args) {
        launch(args);
    }
}