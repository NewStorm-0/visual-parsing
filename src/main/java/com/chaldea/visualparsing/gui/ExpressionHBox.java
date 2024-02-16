package com.chaldea.visualparsing.gui;

import com.chaldea.visualparsing.grammar.Expression;
import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.ProductionSymbol;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义HBox，用来表示一个表达式及头部
 */
public final class ExpressionHBox extends HBox {
    private final TextField left = new TextField();
    private final TextField right = new TextField();
    private final Button editButton = new Button("编辑");
    private final Button deleteButton = new Button("删除");

    public ExpressionHBox() {
        super();
        this.setAlignment(Pos.CENTER_LEFT);
        this.setFillHeight(false);
        this.setSpacing(15.0);
        left.setEditable(false);
        left.prefWidthProperty().bind(widthProperty().subtract(180).divide(3));
        right.setEditable(false);
        right.prefWidthProperty().bind(widthProperty().subtract(180).divide((double) 3 / 2));
        Label rightArrow = new Label("➡");
        rightArrow.setScaleX(2.0);
        rightArrow.setScaleY(1.2);
        rightArrow.setTextFill(Paint.valueOf("lawngreen"));
        rightArrow.setFont(new Font("System Bold", 20.0));
        editButton.setOnAction(this::editOrSave);
        deleteButton.setOnAction(this::delete);
        this.getChildren().addAll(left, rightArrow, right, editButton, deleteButton);
    }

    public ExpressionHBox(Nonterminal head, Expression expression) {
        this();
        setLeft(head);
        setRight(expression);
    }

    public void setLeft(Nonterminal head) {
        left.setText(head.getValue());
    }

    public void setRight(Expression expression) {
        StringBuilder sb = new StringBuilder(64);
        for (ProductionSymbol symbol : expression.getValue()) {
            String string = symbol.getValue();
            if (string.length() > 1) {
                sb.append('`');
                sb.append(string);
                sb.append('`');
            } else {
                sb.append(string);
            }
        }
        right.setText(sb.toString());
    }

    private void editOrSave(ActionEvent actionEvent) {
        if ("编辑".equals(editButton.getText())) {
            left.setEditable(true);
            right.setEditable(true);
            editButton.setText("保存");
        } else {
            // 保存文法

            left.setEditable(false);
            right.setEditable(false);
            editButton.setText("编辑");
        }
    }

    private void delete(ActionEvent actionEvent) {

    }

    /**
     * 根据``对字符串进行分割，并构造{@link com.chaldea.visualparsing.grammar.Expression}对象
     * @param input 字符串
     * @return Expression 对象
     */
    private Expression parseRight(String input) {
        List<ProductionSymbol> productionSymbolList = new ArrayList<>(input.length());
        // 定义正则表达式匹配被 `` 括住的部分
        Pattern pattern = Pattern.compile("\\[([^<>\\{\\}]+?)\\]");
        Matcher matcher = pattern.matcher(input);
        int previousEnd = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            for (int i = previousEnd; i < start; i++) {
                // TODO:
//                productionSymbolList.add()
            }
        }
        return null;
    }

}
