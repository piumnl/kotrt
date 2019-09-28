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
package org.kotrt.maillist.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.agitos.dkim.DKIMSigner;

/**
 * Java Mail 的所有配置
 *
 * @author piumnl
 * @version 1.0.0
 * @since on 2019-09-20.
 */
public class MailProperty {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailProperty.class);

    private static final String PROPERTY_FILE = "maillist.properties";

    private Properties properties;

    public MailProperty() {
        this.properties = new Properties();

        this.properties.setProperty("mail.transport.protocol", "smtp");
        this.properties.setProperty("mail.store.protocol", "imap");
        this.properties.setProperty("mail.smtp.host", "smtp.qq.com");
        this.properties.setProperty("mail.imap.host", "imap.qq.com");
        this.properties.setProperty("mail.smtp.auth", "true");
        this.properties.setProperty("mail.smtp.port", "465");
        this.properties.setProperty("mail.smtp.ssl.enable", "true");
        this.properties.setProperty("mail.smtp.socketFactory.port", "465");
        this.properties.setProperty("mail.smtp.socketFactory.fallback", "false");
        this.properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        // dkim
        this.properties.setProperty("mail.smtp.dkim.signingdomain", "mail.kakjcloud.com");
        this.properties.setProperty("mail.smtp.dkim.selector", "*");
        this.properties.setProperty("mail.smtp.dkim.privatekey", "C:\\ssl\\rsakey.pem");

        try {
            final Properties properties = readProperties();
            for (String propertyName : properties.stringPropertyNames()) {
                this.properties.setProperty(propertyName, properties.getProperty(propertyName));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Properties readProperties() throws IOException {
        final Path path = Paths.get(PROPERTY_FILE);
        if (Files.exists(path)) {
            LOGGER.info("开始读取属性文件 {}", path.toAbsolutePath());
            final Properties properties = new Properties();
            properties.load(Files.newInputStream(path));
            return properties;
        } else {
            Files.createFile(path);
            LOGGER.error("缺少属性文件信息!");
            throw new RuntimeException("缺少属性文件 " + PROPERTY_FILE + " ！");
        }
    }

    public String getUsername() {
        return properties.getProperty("username");
    }

    public String getPassword() {
        return properties.getProperty("password");
    }

    public String getStoreProtocol() {
        return properties.getProperty("mail.store.protocol");
    }

    public String getDKIMSigningdomain() {
        return properties.getProperty("mail.smtp.dkim.signingdomain");
    }

    public String getIMAPHost() {
        return properties.getProperty("mail.imap.host");
    }

    public DKIMSigner newDKIMSigner() throws Exception {
        KeyFactory var7 = KeyFactory.getInstance("RSA");
        final String s = "";
        PKCS8EncodedKeySpec var8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(s));
        RSAPrivateKey var9 = (RSAPrivateKey)var7.generatePrivate(var8);
        return new DKIMSigner(properties.getProperty("mail.smtp.dkim.signingdomain"),
                properties.getProperty("mail.smtp.dkim.selector"),
                var9);
                // properties.getProperty("mail.smtp.dkim.privatekey"));
    }

    public Properties getProperties() {
        return properties;
    }
}
