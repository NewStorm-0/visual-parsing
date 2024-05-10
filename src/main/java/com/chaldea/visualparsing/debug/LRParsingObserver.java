package com.chaldea.visualparsing.debug;

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
}
