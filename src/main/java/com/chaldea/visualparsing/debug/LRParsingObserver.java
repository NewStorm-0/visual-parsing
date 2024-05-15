package com.chaldea.visualparsing.debug;

import com.chaldea.visualparsing.grammar.Nonterminal;
import com.chaldea.visualparsing.grammar.Production;
import com.chaldea.visualparsing.grammar.ProductionSymbol;
import com.chaldea.visualparsing.grammar.Terminal;
import com.chaldea.visualparsing.parsing.ActionItem;

public interface LRParsingObserver {

    /**
     * 生成一条步骤
     *
     * @param actionItem 采取的动作
     */
    void addStepData(ActionItem actionItem);

    /**
     * show next algorithm step.
     *
     * @param index 要运行的下一步算法的索引
     */
    void showNextAlgorithmStep(int index);

    /**
     * Show exception.算法执行发生异常
     *
     * @param e the e
     */
    void showException(Exception e);

    /**
     * Complete execution.正常执行结束
     */
    void completeExecution();

    /**
     * 向语法分析树中添加一个终结符节点
     * @param terminal 节点代表的终结符
     */
    void addNodeToTree(Terminal terminal);

    /**
     * 向语法分析树添加一个终结符节点，并连接其与子节点的线
     * @param nonterminal 非终结符
     * @param symbols 作为子节点的文法符号
     */
    void addParentNodeToTree(Nonterminal nonterminal, ProductionSymbol... symbols);

    /**
     * 向语法分析器状态图中添加起始节点
     * @param state 起始状态
     */
    void initializeParserState(int state);

    /**
     * 向语法分析器状态中加一个节点
     *
     * @param state  the state
     * @param symbol 文法符号的值，通过该文法符号语法分析器的状态改变
     */
    void addNodeToState(String state, ProductionSymbol symbol);

    /**
     * 回退语法分析器状态，在归约时发生
     * @param production 归约用的产生式
     */
    void rollbackState(Production production);
}
