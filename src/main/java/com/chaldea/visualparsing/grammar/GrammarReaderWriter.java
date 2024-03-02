package com.chaldea.visualparsing.grammar;

import java.io.*;

/**
 * 读取文法类
 * 可以从文件中读取文法
 * <p>该类设计有问题，应该只用其中的静态方法</p>
 */
public class GrammarReaderWriter {
    private File grammarFile;
    private Grammar grammar;

    @Deprecated
    public GrammarReaderWriter(File file) {
        grammarFile = file;
    }

    @Deprecated
    public GrammarReaderWriter(Grammar grammar) {
        this.grammar = grammar;
    }

    @Deprecated
    public Grammar getGrammar() {
        return grammar;
    }

    @Deprecated
    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
    }

    @Deprecated
    public File getGrammarFile() {
        return grammarFile;
    }

    @Deprecated
    public void setGrammarFile(File grammarFile) {
        this.grammarFile = grammarFile;
    }

    /**
     * 将文件进行反序列化，获取一个 {@link com.chaldea.visualparsing.grammar.Grammar} 对象
     * @return 反序列化得到的对象
     * {@link com.chaldea.visualparsing.grammar.Grammar}
     * @throws StreamCorruptedException 打开错误，文件格式不对
     * @throws FileNotFoundException 文件未找到
     */
    @Deprecated
    public Grammar readGrammar() throws IOException {
        return GrammarReaderWriter.readGrammarFromFile(grammarFile);
    }

    /**
     * 将文件进行反序列化，获取一个 {@link com.chaldea.visualparsing.grammar.Grammar} 对象
     * @param file 读取的文件
     * @return 反序列化得到的对象
     * {@link com.chaldea.visualparsing.grammar.Grammar}
     * @throws StreamCorruptedException 打开错误，文件格式不对
     * @throws FileNotFoundException 文件未找到
     */
    public static Grammar readGrammarFromFile(File file) throws IOException {
        return readGrammarFromStream(new FileInputStream(file));
    }

    /**
     * 根据输入流内容进行反序列化，获取一个 {@link com.chaldea.visualparsing.grammar.Grammar} 对象
     * @param inputStream 输入流
     * @return 反序列化得到的对象
     * {@link com.chaldea.visualparsing.grammar.Grammar}
     * @throws StreamCorruptedException 反序列化错误，内容格式不对
     */
    public static Grammar readGrammarFromStream(InputStream inputStream) throws IOException {
        try (ObjectInputStream input = new ObjectInputStream(inputStream)) {
            Object object = input.readObject();
            if (object instanceof Grammar) {
                return (Grammar) object;
            } else {
                throw new StreamCorruptedException();
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeGrammarToFile(Grammar grammar, File file) throws IOException {
        if (!file.exists() && file.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                writeGrammarToStream(grammar, fos);
            }
        } else {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                writeGrammarToStream(grammar, fos);
            }
        }
    }

    /**
     * 对一个 {@link com.chaldea.visualparsing.grammar.Grammar} 对象进行序列化，并写入
     * 到输出流
     * @param grammar 文法对象
     * @param outputStream 输出流
     * @throws IOException IO错误
     */
    public static void writeGrammarToStream(Grammar grammar, OutputStream outputStream) throws IOException {
        try (ObjectOutputStream output = new ObjectOutputStream(outputStream)) {
            output.writeObject(grammar);
        }
    }
}
