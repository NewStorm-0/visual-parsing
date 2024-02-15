package com.chaldea.visualparsing.gui;

import org.controlsfx.dialog.ExceptionDialog;

public class ExceptionDialogUtils {
    /**
     * 展示一个异常对话框
     * @param exception 发生的异常
     */
    public static void showExceptionDialog(Throwable exception) {
        ExceptionDialog exceptionDialog = new ExceptionDialog(exception);
        exceptionDialog.setTitle("错误：");
        exceptionDialog.setHeaderText("一个异常发生了");
        exceptionDialog.showAndWait();
    }
}
