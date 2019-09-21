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
package org.kotrt.maillist.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.kotrt.maillist.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 读取服务的配置
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-21.
 */
public class PropertyCommand implements Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyCommand.class);

    private static final String PROPERTY_FILE = "maillist.properties";

    @Override
    public void run(String[] args) throws Exception {
        final Path path = Paths.get(PROPERTY_FILE);
        if (Files.exists(path)) {
            LOGGER.info("开始读取属性文件 {}", path.toAbsolutePath());
            final Properties properties = new Properties();
            properties.load(Files.newInputStream(path));
            Context.getInstance().setProperties(properties);
        } else {
            Files.createFile(path);
            LOGGER.error("缺少属性文件信息!");
            System.exit(0);
        }
    }
}
