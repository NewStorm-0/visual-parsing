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
import javafx.beans.property.SimpleIntegerProperty;
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
    private HBox topHBox;
    @FXML
    private AnchorPane leftAnchorPane;
    @FXML
    private AnchorPane rightAnchorPane;
    @FXML
    private ScrollPane tableScrollPane;
    @FXML
    private TableView<Pair<Nonterminal, Expression[]>> tableView;
    @FXML
    private TableColumn<Pair<Nonterminal, Expression[]>, String> nonterminalColumn;
    @FXML
    private TableColumn<Pair<Nonterminal, Expression[]>, String> inputSymbolColumn;
    @FXML
    private TextField inputStringTextField;
    @FXML
    private VBox algorithmVBox;
    @FXML
    private ScrollPane stepScrollPane;
    @FXML
    private TableView<PredictiveParsingStepData> stepView;
    @FXML
    private TableColumn<PredictiveParsingStepData, String> numberColumn;
    @FXML
    private TableColumn<PredictiveParsingStepData, String> stackColumn;
    @FXML
    private TableColumn<PredictiveParsingStepData, String> inputColumn;
    @FXML
    private TableColumn<PredictiveParsingStepData, String> actionColumn;
    @FXML
    private TableColumn<PredictiveParsingStepData, String> expressionColumn;
    @FXML
    private CheckBox judgeStackNotNullPoint;
    @FXML
    private CheckBox ifXEqualsIpSymbolPoint;
    @FXML
    private CheckBox elseIfXIsTerminalPoint;
    @FXML
    private CheckBox elseIfMIsAWrongItemPoint;
    @FXML
    private CheckBox finalElse;
    @FXML
    private CheckBox letXEqualsTopOfTheStack;
    @FXML
    private Button resumeButton;
    @FXML
    private Button stepButton;

    private Grammar grammar;
    private LL1Parser ll1Parser;
    private PredictiveParsingTable parsingTable;

    private final StepwiseAlgorithmDebugger algorithmDebugger;
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
    private void initialize() {
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
    void loadGrammar() {
        grammar = ControllerMediator.getInstance().getGrammar();
        ll1Parser = new LL1Parser(grammar);
        parsingTable = ll1Parser.generatePredictiveParsingTable();
        // 生成对应列
        tableColumnList = new ArrayList<>(grammar.getTerminals().size() + 1);
        addInputSymbolColumns();
        // 添加子列
        inputSymbolColumn.getColumns().setAll(tableColumnList);
        // 添加数据
        Set<Pair<Nonterminal, Expression[]>> tableData = new HashSet<>();
        for (Map.Entry<Nonterminal, Integer> entry : parsingTable.getNonterminalMap().entrySet()) {
            tableData.add(new Pair<>(entry.getKey(), parsingTable.getTable()[entry.getValue()]));
        }
        tableView.getItems().clear();
        tableView.getItems().addAll(tableData);
    }

    @Override
    public void addStepData(String action, Nonterminal head, Expression expression) {
        // 向预测预测表中添加一个步骤
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
        algorithmVBox.getChildren().filtered(node -> node instanceof CheckBox)
                .forEach(node -> node.getStyleClass().remove(styleClass));
        int stepNumbers = (int) algorithmVBox.getChildren().stream()
                .filter(node -> node instanceof CheckBox).count();
        if (index == -1 || index >= stepNumbers) {
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
    private void processInputString() {
        if (inputStringTextField.getText().isBlank()) {
            DialogShower.showErrorDialog("输入不能为空");
            return;
        }
        // 将输入字符串转换为Terminal的列表
        List<Terminal> inputSymbolList = Grammars
                .convertStringToTerminalList(grammar, inputStringTextField.getText());
        PredictiveAnalyticsAlgorithm algorithm = new PredictiveAnalyticsAlgorithm(
                parsingTable, grammar.getStartSymbol(), inputSymbolList
        );
        algorithmDebugger.setStepwiseAlgorithm(algorithm);
        algorithm.addObserver(this);
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
    private void resumeDebugger() {
        algorithmDebugger.resume();
    }

    /**
     * Step debugger.
     */
    @FXML
    private void stepDebugger() {
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
     * Bind check box.为checkbox（断点）绑定事件
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

}
