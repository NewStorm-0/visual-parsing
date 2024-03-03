package com.chaldea.visualparsing.controller;

import com.chaldea.visualparsing.ControllerMediator;
import com.chaldea.visualparsing.grammar.*;
import com.chaldea.visualparsing.parsing.LL1Parser;
import com.chaldea.visualparsing.parsing.PredictiveParsingTable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LL1ViewController {

    @FXML
    protected AnchorPane topPane;
    @FXML
    protected ScrollPane tableScrollPane;
    @FXML
    protected TableView<Pair<Nonterminal, Expression[]>> tableView;
    @FXML
    protected TableColumn<Pair<Nonterminal, Expression[]>, String> nonterminalColumn;
    @FXML
    protected TableColumn<Pair<Nonterminal, Expression[]>, String> inputSymbolColumn;

    private Grammar grammar;
    private LL1Parser ll1Parser;
    private PredictiveParsingTable parsingTable;
    private List<TableColumn<Pair<Nonterminal, Expression[]>, String>> tableColumnList;
    private static final Logger logger = LoggerFactory.getLogger(LL1ViewController.class);

    public LL1ViewController() {
        // 注册 controller
        ControllerMediator.getInstance().setLl1ViewController(this);
        logger.debug("已经注册LL1ViewController");
    }

    @FXML
    public void initialize() {
        nonterminalColumn.setCellValueFactory(cellData -> {
            Nonterminal nonterminal = cellData.getValue().getKey();
            return new SimpleStringProperty(nonterminal.getValue());
        });
        tableScrollPane.prefHeightProperty().bind(topPane.heightProperty().subtract(42));
        tableScrollPane.prefWidthProperty().bind(topPane.widthProperty().multiply(0.35));
        tableView.prefHeightProperty().bind(topPane.heightProperty().subtract(60));
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
        for (Map.Entry<Nonterminal, Integer> entry :
                parsingTable.getNonterminalMap().entrySet()) {
            tableData.add(new Pair<>(entry.getKey(),
                    parsingTable.getTable()[entry.getValue()]));
        }
        tableView.getItems().addAll(tableData);
    }

    /**
     * Add input symbol columns.
     * 添加输入符号对应的列
     */
    private void addInputSymbolColumns() {
        Terminal[] terminals = new Terminal[parsingTable.getInputSymbolMap().size()];
        // 先根据预测分析表列号的映射值排序，以便每一列与其数据对应
        for (Map.Entry<Terminal, Integer> entry :
                parsingTable.getInputSymbolMap().entrySet()) {
            terminals[entry.getValue()] = entry.getKey();
        }
        for (Terminal t : terminals) {
            TableColumn<Pair<Nonterminal, Expression[]>, String> temp =
                    new TableColumn<>(t.getValue());
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
}
