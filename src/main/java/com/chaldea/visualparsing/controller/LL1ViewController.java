package com.chaldea.visualparsing.controller;

import com.chaldea.visualparsing.debug.PredictiveAnalyticsObserver;
import com.chaldea.visualparsing.debug.StepwiseAlgorithmDebugger;
import com.chaldea.visualparsing.exception.NullPredictivParsingCellException;
import com.chaldea.visualparsing.exception.grammar.UnknownSymbolException;
import com.chaldea.visualparsing.grammar.*;
import com.chaldea.visualparsing.gui.DialogShower;
import com.chaldea.visualparsing.gui.PredictiveParsingStepData;
import com.chaldea.visualparsing.parsing.LL1Parser;
import com.chaldea.visualparsing.debug.PredictiveAnalyticsAlgorithm;
import com.chaldea.visualparsing.parsing.PredictiveParsingTable;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LL1ViewController implements PredictiveAnalyticsObserver {

    @FXML
    protected HBox topHBox;
    @FXML
    protected AnchorPane leftAnchorPane;
    @FXML
    protected AnchorPane rightAnchorPane;
    @FXML
    protected ScrollPane tableScrollPane;
    @FXML
    protected TableView<Pair<Nonterminal, Expression[]>> tableView;
    @FXML
    protected TableColumn<Pair<Nonterminal, Expression[]>, String> nonterminalColumn;
    @FXML
    protected TableColumn<Pair<Nonterminal, Expression[]>, String> inputSymbolColumn;
    @FXML
    protected TextField inputStringTextField;
    @FXML
    protected VBox algorithmVBox;
    @FXML
    protected ScrollPane stepScrollPane;
    @FXML
    protected TableView<PredictiveParsingStepData> stepView;
    @FXML
    protected TableColumn<PredictiveParsingStepData, String> numberColumn;
    @FXML
    protected TableColumn<PredictiveParsingStepData, String> stackColumn;
    @FXML
    protected TableColumn<PredictiveParsingStepData, String> inputColumn;
    @FXML
    protected TableColumn<PredictiveParsingStepData, String> actionColumn;
    @FXML
    protected TableColumn<PredictiveParsingStepData, String> expressionColumn;
    @FXML
    protected CheckBox judgeStackNotNullPoint;
    @FXML
    protected CheckBox ifXEqualsIpSymbolPoint;
    @FXML
    protected CheckBox elseIfXIsTerminalPoint;
    @FXML
    protected CheckBox elseIfMIsAWrongItemPoint;
    @FXML
    protected CheckBox finalElse;
    @FXML
    protected CheckBox letXEqualsTopOfTheStack;
    @FXML
    protected Button resumeButton;
    @FXML
    protected Button stepButton;

    private Grammar grammar;
    private LL1Parser ll1Parser;
    private PredictiveParsingTable parsingTable;

    private StepwiseAlgorithmDebugger algorithmDebugger;
    /**
     * The Table column list.存放预测分析表中输入符号的列
     */
    private List<TableColumn<Pair<Nonterminal, Expression[]>, String>> tableColumnList;
    private static final Logger logger = LoggerFactory.getLogger(LL1ViewController.class);

    public LL1ViewController() {
        algorithmDebugger = new StepwiseAlgorithmDebugger();
        // 注册 controller
        ControllerMediator.getInstance().setLl1ViewController(this);
        logger.debug("已经注册LL1ViewController");
    }

    @FXML
    public void initialize() {
        setColumnsCellValueFactory();
        bindCheckBox();
        leftAnchorPane.prefWidthProperty().bind(topHBox.widthProperty().multiply(0.5));
        leftAnchorPane.prefHeightProperty().bind(topHBox.heightProperty().subtract(5));
        rightAnchorPane.prefWidthProperty().bind(topHBox.widthProperty().multiply(0.5));
        rightAnchorPane.prefHeightProperty().bind(topHBox.heightProperty().subtract(5));
        tableScrollPane.prefHeightProperty().bind(leftAnchorPane.heightProperty().subtract(50));
        tableScrollPane.prefWidthProperty().bind(leftAnchorPane.widthProperty().subtract(25));
//        tableView.prefHeightProperty().bind(leftAnchorPane.heightProperty().subtract(90));
        algorithmVBox.prefWidthProperty().bind(rightAnchorPane.widthProperty().subtract(20));
        stepScrollPane.prefWidthProperty().bind(rightAnchorPane.widthProperty().subtract(20));
//        algorithmVBox.layoutBoundsProperty().addListener(((observable, oldValue,
//                                                           newValue) -> {
//            AnchorPane.setTopAnchor(stepScrollPane, 45 + newValue.getHeight());
//            logger.info(newValue.getHeight()+"");
//        }));
        stepScrollPane.prefHeightProperty().bind(rightAnchorPane.heightProperty().subtract(370));
    }

    /**
     * Load grammar. 加载文法
     */
    public void loadGrammar() {
        grammar = ControllerMediator.getInstance().getGrammar();
        ll1Parser = new LL1Parser(grammar);
        parsingTable = ll1Parser.generatePredictiveParsingTable();
        // 生成对应列
        tableColumnList = new ArrayList<>(grammar.getTerminals().size() + 1);
        addInputSymbolColumns();
        // 添加子列
        inputSymbolColumn.getColumns().setAll(tableColumnList);
        // 表格添加列
        tableView.getColumns().setAll(nonterminalColumn, inputSymbolColumn);
        // 添加数据
        Set<Pair<Nonterminal, Expression[]>> tableData = new HashSet<>();
        for (Map.Entry<Nonterminal, Integer> entry : parsingTable.getNonterminalMap().entrySet()) {
            tableData.add(new Pair<>(entry.getKey(), parsingTable.getTable()[entry.getValue()]));
        }
        tableView.getItems().addAll(tableData);
    }

    @Override
    public void addStepData(String action, Nonterminal head, Expression expression) {
        // 向预测预测表中添加一个步骤，并且高亮当前执行的步骤
        PredictiveAnalyticsAlgorithm algorithm = (PredictiveAnalyticsAlgorithm)
                algorithmDebugger.getStepwiseAlgorithm();
        PredictiveParsingStepData stepData = new PredictiveParsingStepData(
                algorithm.getIp(), stepView.getItems().size() + 1,
                algorithm.getStack(), action, head, expression
        );
        logger.debug("stepData: {}", stepData);
        stepView.getItems().add(stepData);
    }

    @Override
    public void showNextAlgorithmStep(int index) {
        String styleClass = "next-statement";
        algorithmVBox.getChildren().forEach(node ->
                node.getStyleClass().remove(styleClass));
        int algorithmSize = (int) algorithmVBox.getChildren().stream()
                .filter(node -> node instanceof CheckBox).count();
        if (index == -1 || index >= algorithmSize) {
            return;
        }
        algorithmVBox.getChildren().get(index + 2).getStyleClass().add(styleClass);
    }

    @Override
    public void showException(Exception e) {
        if (e instanceof UnknownSymbolException) {
            DialogShower.showErrorDialog("无法识别的终结符 " + e.getMessage());
        } else if (e instanceof NullPredictivParsingCellException e1) {
            DialogShower.showErrorDialog("预测分析表中M[" + e1.getNonterminal()
                    + ", " + e1.getSymbol() + "] 为空"
            );
        }
    }

    @Override
    public void completeExecution() {
        DialogShower.showInformationDialog("分析完毕");
    }

    /**
     * Process input string.
     */
    @FXML
    protected void processInputString() {
        if (inputStringTextField.getText().isBlank()) {
            DialogShower.showErrorDialog("输入不能为空");
            return;
        }
        // 将输入字符串转换为Terminal的列表
        List<Terminal> inputSymbolList =
                convertStringToTerminalList(inputStringTextField.getText());
        algorithmDebugger.setStepwiseAlgorithm(new PredictiveAnalyticsAlgorithm(
                parsingTable, grammar.getStartSymbol(), inputSymbolList
        ));
        ((PredictiveAnalyticsAlgorithm) algorithmDebugger.getStepwiseAlgorithm())
                .addObserver(this);
        PredictiveParsingStepData.setInputSymbols(inputSymbolList);
        // 清除原先的数据
        stepView.getItems().clear();
        resumeButton.setDisable(false);
        stepButton.setDisable(false);
        // 开始执行debugger
        algorithmDebugger.start();
    }

    /**
     * Resume debugger.
     */
    @FXML
    protected void resumeDebugger() {
        algorithmDebugger.resume();
    }

    /**
     * Step debugger.
     */
    @FXML
    protected void stepDebugger() {
        algorithmDebugger.step();
    }

    /**
     * Add input symbol columns.
     * 添加输入符号对应的列
     */
    private void addInputSymbolColumns() {
        Terminal[] terminals = new Terminal[parsingTable.getInputSymbolMap().size()];
        // 先根据预测分析表列号的映射值排序，以便每一列与其数据对应
        for (Map.Entry<Terminal, Integer> entry : parsingTable.getInputSymbolMap().entrySet()) {
            terminals[entry.getValue()] = entry.getKey();
        }
        for (Terminal t : terminals) {
            TableColumn<Pair<Nonterminal, Expression[]>, String> temp = new TableColumn<>(t.getValue());
            // 设置每一列的数据
            temp.setCellValueFactory(cellData -> {
                StringBuilder stringBuilder = new StringBuilder(32);
                stringBuilder.append(cellData.getValue().getKey().getValue()).append("→");
                int index = parsingTable.getInputSymbolMap().get(t);
                Expression expression = cellData.getValue().getValue()[index];
                if (expression == null) {
                    return new SimpleStringProperty("");
                }
                for (ProductionSymbol productionSymbol : expression.getValue()) {
                    stringBuilder.append(productionSymbol.getValue());
                }
                return new SimpleStringProperty(stringBuilder.toString());
            });
            temp.setEditable(false);
            temp.setSortable(false);
            tableColumnList.add(temp);
        }
    }

    private void setColumnsCellValueFactory() {
        nonterminalColumn.setCellValueFactory(cellData -> {
            Nonterminal nonterminal = cellData.getValue().getKey();
            return new SimpleStringProperty(nonterminal.getValue());
        });
        numberColumn.setCellValueFactory(cellData -> cellData.getValue().getNumber());
        stackColumn.setCellValueFactory(cellDate -> cellDate.getValue().getStack());
        inputColumn.setCellValueFactory(cellData -> cellData.getValue().getInputQueue());
        actionColumn.setCellValueFactory(cellData -> cellData.getValue().getAction());
        expressionColumn.setCellValueFactory(cellData -> cellData.getValue().getUsedProduction());
    }

    /**
     * Bind check box.为checkbox绑定事件
     */
    private void bindCheckBox() {
        judgeStackNotNullPoint.setOnAction(event -> {
            algorithmDebugger.toggleBreakPoint(0);
        });
        ifXEqualsIpSymbolPoint.setOnAction(event -> {
            algorithmDebugger.toggleBreakPoint(1);
        });
        elseIfXIsTerminalPoint.setOnAction(event -> {
            algorithmDebugger.toggleBreakPoint(2);
        });
        elseIfMIsAWrongItemPoint.setOnAction(event -> {
            algorithmDebugger.toggleBreakPoint(3);
        });
        finalElse.setOnAction(event -> {
            algorithmDebugger.toggleBreakPoint(4);
        });
        letXEqualsTopOfTheStack.setOnAction(event -> {
            algorithmDebugger.toggleBreakPoint(5);
        });
    }

    /**
     * Convert string to terminal list.
     * 将字符串转换为Terminal列表
     *
     * @param inputString the input string
     * @return the list
     */
    private List<Terminal> convertStringToTerminalList(String inputString) {
        StringBuilder regexRuleBuilder = new StringBuilder();
        for (Terminal terminal : grammar.getTerminals()) {
            regexRuleBuilder.append(escapeTerminalString(terminal)).append('|');
        }
        regexRuleBuilder.deleteCharAt(regexRuleBuilder.length() - 1);
        logger.debug(regexRuleBuilder.toString());
        Pattern pattern = Pattern.compile(regexRuleBuilder.toString());
        Matcher matcher = pattern.matcher(inputString);
        int lastEnd = 0;
        List<Terminal> inputSymbolList = new ArrayList<>(inputString.length());
        while (matcher.find()) {
            int begin = matcher.start();
            if (begin != lastEnd) {
                DialogShower.showErrorDialog("存在无法识别的符号："
                        + inputString.substring(lastEnd, begin));
                throw new UnknownSymbolException();
            }
            int end = matcher.end();
            inputSymbolList.add(grammar.getTerminal(inputString.substring(begin, end)));
            lastEnd = end;
        }
        inputSymbolList.add(Terminal.END_MARKER);
        return inputSymbolList;
    }

    private String escapeTerminalString(Terminal terminal) {
        return Pattern.quote(terminal.getValue());
    }

}
