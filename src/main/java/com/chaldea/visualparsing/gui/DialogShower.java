package com.chaldea.visualparsing.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import org.controlsfx.dialog.ExceptionDialog;

import java.util.Optional;

/**
 * The type Dialog shower.
 * <P>用来展示常用的对话窗口</P>
 */
public class DialogShower {
    /**
     * 展示一个异常对话框
     *
     * @param exception 发生的异常
     */
    public static void showExceptionDialog(Throwable exception) {
        ExceptionDialog exceptionDialog = new ExceptionDialog(exception);
        exceptionDialog.setTitle("错误：");
        exceptionDialog.setHeaderText("一个异常发生了");
        exceptionDialog.showAndWait();
    }

    /**
     * Show error dialog.
     *
     * @param contentText the content text
     */
    public static void showErrorDialog(String contentText) {
        showErrorDialog("发生错误！", contentText);
    }

    /**
     * Show error dialog.
     *
     * @param headerText  the header text
     * @param contentText the content text
     */
    public static void showErrorDialog(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    public static Optional<String> showInputDialog(String headerText, String contentText) {
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("提示");
        inputDialog.setHeaderText(headerText);
        inputDialog.setContentText(contentText);
        return inputDialog.showAndWait();
    }
}
