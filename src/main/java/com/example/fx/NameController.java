package com.example.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class NameController {

    private final String soglasnye = "бвгджзйклмнпpcтфxцчшщ";
    private final String glasnye = "ayoыэяюёиe";

    @FXML
    private TextField inputTextField;

    @FXML
    private Button editButton;

    @FXML
    private Label editedTextLabel;

    @FXML
    private void changeNameMethod() {
        String inputText = inputTextField.getText();

        String[] splitName = inputText.trim().split(" ");
        String firstName = splitName[0];
        String middleName = (splitName.length > 1 ? splitName[1] : "");
        String lastName = (splitName.length > 2 ? splitName[2] : "");

        System.out.println(lastName);

        boolean lastCharacter = soglasnye.contains(String.valueOf(firstName.charAt(firstName.length() - 1)));
        boolean lastCharacterY = firstName.endsWith("й");

        String endName = "";

        if (lastCharacter && lastCharacterY) {
            endName = firstName.substring(0, firstName.length() - 1) + "ю";
        } else {
            endName = firstName + "у";
        }

        if (!middleName.isEmpty()) {
            endName += " " + middleName + "у";
        }

        if (!lastName.isEmpty()) {
            endName += " " + lastName + "у";
        }

        printName(endName);
    }

    private void printName(String endName) {
        editedTextLabel.setText("Отредактированное имя: " + endName);
        editedTextLabel.setVisible(true);
    }

}
