package com.chaldea.visualparsing.debug;


import java.util.List;

/**
 * 表示算法可以分步骤执行，有状态存储
 */
public abstract class StepwiseAlgorithm {

    protected List<AlgorithmStep> algorithmStepList;
    /**
     * The Current step index.
     */
    protected int currentStepIndex;
    /**
     * The Last step return value.
     */
    protected Object[] lastStepReturnValue;

    public AlgorithmStep getAlgorithmStep(int index) {
        return algorithmStepList.get(index);
    }

    /**
     * Gets steps number.获取算法步骤数
     *
     * @return the steps number
     */
    public int getStepsNumber() {
        return algorithmStepList.size();
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    /**
     * 设置状态为未执行，以及执行一些算法初始化操作
     */
    public abstract void reset();

    /**
     * 算法向前执行一步
     */
    public abstract void executeStep();

    /**
     * Is execution complete boolean.
     * 判断当前算法是否执行完毕
     *
     * @return the boolean
     */
    public boolean isExecutionComplete() {
        return !(currentStepIndex < algorithmStepList.size() && currentStepIndex >= 0);
    }

}
