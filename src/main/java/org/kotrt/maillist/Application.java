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

import java.util.concurrent.CountDownLatch;

import org.kotrt.maillist.command.EmailCommand;
import org.kotrt.maillist.command.RestartCommand;
import org.kotrt.maillist.command.SubscribeUserCommand;

/**
 * 启动类
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-20.
 */
public class Application {

    private static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        new RestartCommand().run(args);
        new SubscribeUserCommand().run(args);
        new EmailCommand().run(args);

        latch.await();
    }
}
