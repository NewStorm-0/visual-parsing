package com.chaldea.visualparsing;

import com.chaldea.visualparsing.gui.ExceptionDialogUtils;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class UserManualController {
    @FXML
    protected Text text;
    private static final Logger logger =
            LoggerFactory.getLogger(UserManualController.class);

    @FXML
    public void initialize() {
        readFromManual();
    }

    private void readFromManual() {
        try (InputStream input = getClass().getResourceAsStream("manual.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            StringBuilder sb = new StringBuilder(128);
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            text.setText(sb.toString());
        } catch (IOException e) {
            logger.error("", e);
            ExceptionDialogUtils.showExceptionDialog(e);
        }
    }
}

