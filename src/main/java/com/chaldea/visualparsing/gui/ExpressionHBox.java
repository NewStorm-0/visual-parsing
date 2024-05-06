package com.chaldea.visualparsing.gui;

import com.chaldea.visualparsing.controller.ControllerMediator;
import com.chaldea.visualparsing.exception.grammar.*;
import com.chaldea.visualparsing.grammar.*;
import javafx.geometry.Insets;
import javafx.util.Callback;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义HBox，用来表示一个表达式及头部
 */
public class ExpressionHBox extends HBox {
    public AutoCompletionBinding<Nonterminal> leftAutoCompletionBinding;
    @Deprecated
    public AutoCompletionBinding<ProductionSymbol> rightAutoCompletionBinding;
    private final Label numberLabel = new Label();
    private final TextField leftTextField = new TextField();
    private final TextField rightTextField = new TextField();
    private final Button editButton = new Button("编辑");
    private final Button deleteButton = new Button("删除");

    /**
     * The Expression head. 表达当前文法中这个表达式对应的头部
     * 在点击保存按钮后，会改变
     * <p>用于记录在创建时或用户最近保存时的头和体，防止用户在编辑一半时
     * 且没有保存的情况下，对表达式进行删除而发生错误</p>
     */
    private Nonterminal expressionHead;

    /**
     * The Expression body. 表达当前文法中这个表达式对应的体
     * 在点击保存按钮后会改变
     */
    private Expression expressionBody;

    private static final Logger logger = LoggerFactory.getLogger(ExpressionHBox.class);

    public ExpressionHBox() {
        super();
        this.setAlignment(Pos.CENTER_LEFT);
        this.setFillHeight(false);
        this.setSpacing(15.0);
        numberLabel.setFont(new Font("System Regular", 18.0));
        numberLabel.getStyleClass().add("production-number");
        HBox.setMargin(numberLabel, new Insets(0, -10, 0, 0));
        // 设置 头输入框 与 体输入框 自动提示
        leftAutoCompletionBinding = TextFields.bindAutoCompletion(leftTextField,
                ControllerMediator.getInstance().getNonterminalCopy());
//        rightAutoCompletionBinding = TextFields.bindAutoCompletion(right,
//                rightSuggestionProvider());

        leftTextField.setEditable(false);
        leftTextField.prefWidthProperty().bind(widthProperty().subtract(260).divide(3));
        leftTextField.setPromptText("输入产生式头(非终结符)");
        leftTextField.getStyleClass().add("production-text-field");
        rightTextField.setEditable(false);
        rightTextField.prefWidthProperty().bind(widthProperty().subtract(260).divide((double) 3 / 2));
        rightTextField.setPromptText("输入产生式体");
        rightTextField.getStyleClass().add("production-text-field");

        Label rightArrow = new Label("➡");
        rightArrow.setScaleX(2.0);
        rightArrow.setScaleY(1.2);
        rightArrow.setTextFill(Paint.valueOf("lawngreen"));
        rightArrow.setFont(new Font("System Bold", 20.0));
        editButton.setOnAction(this::editOrSave);
        editButton.getStyleClass().add("production-button");
        deleteButton.setOnAction(this::delete);
        deleteButton.getStyleClass().add("production-button");
        this.getChildren().addAll(numberLabel, leftTextField, rightArrow, rightTextField,
                editButton, deleteButton);
    }

    public ExpressionHBox(Nonterminal head, Expression expression) {
        this();
        setLeftTextField(head);
        setRightTextField(expression);
        setNumberLabel();
    }

    public ExpressionHBox(int number) {
        this();
        numberLabel.setText(number + ".");
    }

    /**
     * 判断当前左部与右边内容是否为空
     *
     * @return 当左部或右边内容为空则返回 true，否则返回 false
     */
    public boolean isEmpty() {
        return leftTextField.getText().isEmpty() || rightTextField.getText().isEmpty();
    }

    /**
     * Sets left.
     *
     * @param head the head
     */
    public void setLeftTextField(Nonterminal head) {
        expressionHead = head;
        leftTextField.setText(head.getValue());
    }

    /**
     * To warning state.
     */
    public void toWarningState() {
        leftTextField.getStyleClass().add("warning");
        rightTextField.getStyleClass().add("warning");
    }

    /**
     * To normal state.
     */
    public void toNormalState() {
        leftTextField.getStyleClass().remove("warning");
        rightTextField.getStyleClass().remove("warning");
    }

    /**
     * Update left auto completion binding.
     */
    public void updateLeftAutoCompletionBinding() {
        if (leftAutoCompletionBinding != null) {
            leftAutoCompletionBinding.dispose();
        }
        leftAutoCompletionBinding = TextFields.bindAutoCompletion(leftTextField,
                ControllerMediator.getInstance().getNonterminalCopy());
    }

    public void setRightTextField(Expression expression) {
        expressionBody = expression;
        StringBuilder sb = new StringBuilder(64);
        for (ProductionSymbol symbol : expression.getValue()) {
            String string = symbol.getValue();
            if (string.length() > 1) {
                sb.append('<');
                sb.append(string);
                sb.append('>');
            } else {
                sb.append(string);
            }
        }
        rightTextField.setText(sb.toString());
    }

    public void setNumberLabel() {
        if (expressionHead == null || expressionBody == null) {
            int index = 1;
            Grammar grammar = ControllerMediator.getInstance().getGrammar();
            for (Production production : grammar.getProductions()) {
                index += production.getBody().size();
            }
            numberLabel.setText(index + ".");
            return;
        }
        int index = Grammars.getExpressionIndex(
                ControllerMediator.getInstance().getGrammar(),
                expressionHead,
                expressionBody
        );
        numberLabel.setText(index + ".");
    }

    private void editOrSave(ActionEvent actionEvent) {
        if ("编辑".equals(editButton.getText())) {
            leftTextField.setEditable(true);
            rightTextField.setEditable(true);
            editButton.setText("保存");
        } else {
            // 保存文法
            try {
                Nonterminal head = parseLeftTextField();
                Expression body = parseRightTextField();
                logger.debug(body.toString());
                if (!(head.equals(expressionHead) && body.equals(expressionBody))) {
                    // 判断是否改变了文本输入框内容
                    ControllerMediator.getInstance().getGrammarViewController()
                            .addExpressionToGrammar(head, body);
                }

                // 保存成功
                leftTextField.setEditable(false);
                rightTextField.setEditable(false);
                editButton.setText("编辑");
                expressionHead = head;
                expressionBody = body;
                DialogShower.showInformationDialog("保存成功");
            } catch (UnknownSymbolException e) {
                DialogShower.showErrorDialog("表达式体中含有未定义符号："
                        + e.getMessage());
            } catch (IllegalSymbolException e) {
                DialogShower.showErrorDialog(e.getMessage() + "不在非终结符中");
            } catch (RepeatedProductionException e) {
                DialogShower.showErrorDialog("重复的表达式");
            } catch (EmptyHeadProductionException e) {
                DialogShower.showErrorDialog("产生式头不可以为空");
            } catch (EmptyExpressionException e) {
                DialogShower.showErrorDialog("产生式体不可以为空");
            }
        }
    }

    private void delete(ActionEvent actionEvent) {
        try {
            if (expressionHead != null && expressionBody != null) {
                // 若条件中两个变量都不为 null，则说明对应的内容已经保存到文法中，需要删除
                ControllerMediator.getInstance().getGrammarViewController()
                        .deleteExpressionFromGrammar(expressionHead, expressionBody);
            }
            ControllerMediator.getInstance().getGrammarViewController()
                    .deleteExpressionHBox(this);
        } catch (UnknownSymbolException e) {
            DialogShower.showErrorDialog("表达式体中含有未定义符号："
                    + e.getMessage());
        } catch (IllegalSymbolException e) {
            DialogShower.showErrorDialog(e.getMessage() + "不在非终结符中");
        }
    }

    /**
     * 根据<>对字符串进行分割，并构造{@link com.chaldea.visualparsing.grammar.Expression}对象
     *
     * @return Expression 对象
     * @throws EmptyExpressionException 产生式体为空异常
     */
    private Expression parseRightTextField() {
        String input = rightTextField.getText();
        if (input.isBlank()) {
            throw new EmptyExpressionException();
        }
        List<ProductionSymbol> productionSymbolList = new ArrayList<>(input.length());
        // 定义正则表达式匹配被 <> 括住的部分
        Pattern pattern = Pattern.compile("<([^<>]+?)>");
        Matcher matcher = pattern.matcher(input);
        int previousEnd = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            for (int i = previousEnd; i < start; i++) {
                addExactSymbolToList(productionSymbolList,
                        String.valueOf(input.charAt(i)));
            }
            previousEnd = end;
            addExactSymbolToList(productionSymbolList,
                    input.substring(start + 1, end - 1));
        }
        for (int i = previousEnd; i < input.length(); i++) {
            addExactSymbolToList(productionSymbolList,
                    String.valueOf(input.charAt(i)));
        }
        return new Expression(productionSymbolList.toArray(ProductionSymbol[]::new));
    }

    private Nonterminal parseLeftTextField() {
        if (leftTextField.getText().isBlank()) {
            throw new EmptyHeadProductionException();
        }
        Set<Nonterminal> nonterminalSet = ControllerMediator.getInstance().getNonterminalCopy();
        Nonterminal nonterminalSymbol = new Nonterminal(leftTextField.getText());
        if (nonterminalSet.contains(nonterminalSymbol)) {
            return nonterminalSymbol;
        } else {
            throw new IllegalSymbolException(nonterminalSymbol.toString());
        }
    }

    /**
     * 获取文法符号是非终结符还是终结符
     * @param symbol 文法符号
     * @return Nonterminal.class 或者 Terminal.class 或 null
     */
    @Deprecated
    private Class<?> getSymbolClass(String symbol) {
        Set<Terminal> terminals = ControllerMediator.getInstance().getTerminalCopy();
        Set<Nonterminal> nonterminals = ControllerMediator.getInstance().getNonterminalCopy();
        if (terminals.contains(new Terminal(symbol))) {
            return Terminal.class;
        } else if (nonterminals.contains(new Nonterminal(symbol))) {
            return Nonterminal.class;
        } else {
            return null;
        }
    }

    /**
     * Add exact symbol to list.
     *
     * @param list   the list
     * @param symbol the symbol
     */
    private void addExactSymbolToList(List<ProductionSymbol> list, String symbol) {
        Set<Terminal> terminals = ControllerMediator.getInstance().getTerminalCopy();
        Set<Nonterminal> nonterminals = ControllerMediator.getInstance().getNonterminalCopy();
        if (terminals.contains(new Terminal(symbol))) {
            list.add(new Terminal(symbol));
        } else if (nonterminals.contains(new Nonterminal(symbol))) {
            list.add(new Nonterminal(symbol));
        } else {
            throw new UnknownSymbolException(symbol);
        }
    }

    /**
     * 感觉效果不行，
     * @return callback
     */
    private Callback<AutoCompletionBinding.ISuggestionRequest, Collection<ProductionSymbol>>
    rightSuggestionProvider() {
        return request -> {
            String input = request.getUserText();
            logger.debug(input);
            // TODO: 继续写
            return null;
        };
    }

}
