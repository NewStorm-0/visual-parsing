package com.chaldea.visualparsing;

/**
 * 启动类
 * 由于 javapackager maven插件的原因，需要另外一个启动类。
 * <a href="https://github.com/fvarrui/JavaPackager/issues/20">issue</a>
 */
public class Main {
    public static void main(String[] args) {
        VisualApplication.run(args);
    }
}
