package org.kotrt.maillist.command;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 重启操作
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-21.
 */
public class RestartCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestartCommand.class);

    private static final String PID_FILE = "maillist.pid";

    @Override
    public void run(String[] args) throws Exception {
        final Path pidFile = Paths.get(PID_FILE);
        if (Files.exists(pidFile)) {
            String pid = new String(Files.readAllBytes(pidFile));

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("linux")) {
                Runtime.getRuntime().exec("kill -9 " + pid);
                LOGGER.info("杀死进程 {}.", pid);
            } else {
                LOGGER.warn("非 Linux 系统，忽略 pid 操作！");
            }
        } else {
            Files.createFile(pidFile);
        }

        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final String pid = jvmName.split("@")[0];
        Files.write(pidFile, pid.getBytes());

        LOGGER.info("当前进程 pid：{}", pid);
    }
}
