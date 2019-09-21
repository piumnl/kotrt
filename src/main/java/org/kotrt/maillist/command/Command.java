package org.kotrt.maillist.command;

/**
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-21.
 */
public interface Command {

    /**
     * 执行类
     *
     * @param args 启动参数
     */
    void run(String[] args) throws Exception;
}
