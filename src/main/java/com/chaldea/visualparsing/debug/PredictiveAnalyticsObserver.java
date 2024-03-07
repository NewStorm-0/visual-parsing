package com.chaldea.visualparsing.debug;

import com.chaldea.visualparsing.grammar.Expression;
import com.chaldea.visualparsing.grammar.Nonterminal;

public interface PredictiveAnalyticsObserver {
    String REPLACE = "替代";
    String MATCH = "匹配";
    String ACCEPT = "接受";

    /**
     * 生成一条信息
     *
     * @param action     执行的动作
     * @param head       产生式头
     * @param expression the expression
     */
    void addStepData(String action, Nonterminal head, Expression expression);

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
}
