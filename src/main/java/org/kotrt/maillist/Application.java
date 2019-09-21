/**
 * Copyright 2019-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kotrt.maillist;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.mail.internet.MimeMessage;

import org.kotrt.maillist.bean.User;
import org.kotrt.maillist.util.MailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 启动类
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-20.
 */
public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static List<MimeMessage> queue = new ArrayList<>();

    private static CountDownLatch latch = new CountDownLatch(1);

    private static List<User> userList = new ArrayList<>();

    static {
    }

    public static void main(String[] args) throws Exception {
        start(args == null ? new String[0] : args);
        run();
        latch.await();
    }

    private static void start(String[] args) throws IOException {
        final Path pidFile = Paths.get("maillist.pid");
        if (Files.exists(pidFile)) {
            String pid = new String(Files.readAllBytes(pidFile));
            Runtime.getRuntime().exec("kill -9 " + pid);
            LOGGER.info("杀死进程 {}.", pid);
        } else {
            Files.createFile(pidFile);
        }

        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final String pid = jvmName.split("@")[0];
        Files.write(pidFile, pid.getBytes());
        LOGGER.info("当前进程 pid：{}", pid);
    }

    private static void run() {
        Thread thread = new Thread(() -> {
            while (true) {
                LOGGER.info("开始收取邮件.");
                List<MimeMessage> email = MailUtil.getEmail();
                if (email != null) {
                    System.out.println(email.size());
                    queue.addAll(email);
                    MailUtil.batchSend(queue, userList);
                    queue.clear();
                }
                LOGGER.info("邮件收取结束.");
                try {
                    Thread.sleep(5 * 1000 * 60);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        thread.start();
    }

}
