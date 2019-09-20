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
package org.kotrt;

import org.kotrt.bean.User;
import org.kotrt.util.MailUtil;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 启动类
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-20.
 */
public class Application {

    private static List<MimeMessage> queue = new ArrayList<>();

    private static CountDownLatch latch = new CountDownLatch(1);

    private static List<User> userList = new ArrayList<>();

    static {
        userList.add(new User("18579115540@163.com"));
    }

    public static void main(String[] args) throws Exception {
        run();
        //runSend();
        latch.await();
    }

//    private static void runSend() {
//        Thread thread = new Thread(() -> {
//            lock.lock();
//            MailUtil.batchSend(queue,userList);
//            queue.clear();
//            lock.unlock();
//        });
//        thread.start();
//    }


    private static void run() {
        Thread thread = new Thread(() -> {
            List<MimeMessage> email = MailUtil.getEmail();
            if (email != null) {
                System.out.println(email.size());
                queue.addAll(email);
                MailUtil.batchSend(queue, userList);
                queue.clear();
            }
            try {
                Thread.sleep(5 * 1000 * 60);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

}
