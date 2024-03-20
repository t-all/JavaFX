package com.example.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

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

        String nameDp = getNameDp(lastCharacter, lastCharacterY, firstName, middleName, lastName);
        String nameVp = getNameVp(lastCharacter, lastCharacterY, firstName, middleName, lastName);

        printName(nameVp, nameDp);
    }

    private static String getNameDp(boolean lastCharacter, boolean lastCharacterY, String firstName, String middleName, String lastName) {
        String nameDp;

        if (lastCharacter && lastCharacterY) {
            nameDp = firstName.substring(0, firstName.length() - 1) + "ю";
        } else {
            nameDp = firstName + "у";
        }

        if (!middleName.isEmpty()) {
            nameDp += " " + middleName + "у";
        }

        if (!lastName.isEmpty()) {
            nameDp += " " + lastName + "у";
        }
        return nameDp;
    }

    private static String getNameVp(boolean lastCharacter, boolean lastCharacterY, String firstName, String middleName, String lastName) {
        String nameVp;

        if (lastCharacter && lastCharacterY) {
            nameVp = firstName.substring(0, firstName.length() - 1) + "я";
        } else {
            nameVp = firstName + "а";
        }

        if (!middleName.isEmpty()) {
            nameVp += " " + middleName + "а";
        }

        if (!lastName.isEmpty()) {
            nameVp += " " + lastName + "а";
        }
        return nameVp;
    }

    private void printName(String nameVp, String nameDp) {
        String text = "Отредактированный текст: " +
                "Разрешение выдано: Dp, для проведения работ Tp. После окончания работ принять у Vp выданный инвентарь";

        text = text.replace("Vp", nameVp);
        text = text.replace("Dp", nameDp);
        text = text.replace("Tp", "молоток");

        editedTextLabel.setText(text);
        editedTextLabel.setVisible(true);
    }

}
