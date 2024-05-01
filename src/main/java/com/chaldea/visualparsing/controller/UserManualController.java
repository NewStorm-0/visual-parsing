package com.chaldea.visualparsing.controller;

import com.chaldea.visualparsing.gui.DialogShower;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class UserManualController {
    @FXML
    private Text text;
    private static final Logger logger =
            LoggerFactory.getLogger(UserManualController.class);

    public UserManualController() {
        ControllerMediator.getInstance().setUserManualController(this);
        logger.debug("已经注册UserManualController");
    }

    @FXML
    private void initialize() {
        readFromManual();
    }

    private void readFromManual() {
        try (InputStream input = getClass()
                .getResourceAsStream("/com/chaldea/visualparsing/manual.txt");
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(input, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder(128);
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            text.setText(sb.toString());
        } catch (IOException e) {
            logger.error("", e);
            DialogShower.showExceptionDialog(e);
        }
    }
}

