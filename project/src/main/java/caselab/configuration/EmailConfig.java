package caselab.configuration;

import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String host;
    @Value("${spring.mail.port}")
    private int port;
    @Value("${spring.mail.username}")
    private String username;
    @Value("${spring.mail.password}")
    private String password;
    @Value("${spring.mail.properties.mail.smtp.auth}")
    private boolean auth;
    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    private boolean starttlsEnable;
    @Value("${spring.mail.properties.mail.smtp.starttls.connection-timeout}")
    private int connectionTimeout;
    @Value("${spring.mail.properties.mail.smtp.starttls.timeout}")
    private int timeout;
    @Value("${spring.mail.properties.mail.smtp.starttls.write-timeout}")
    private int writeTimeout;
    @Value("${spring.mail.properties.mail.transport.protocol}")
    private String transportProtocol;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.starttls.connection-timeout", connectionTimeout);
        props.put("mail.smtp.starttls.timeout", timeout);
        props.put("mail.smtp.starttls.write-timeout", writeTimeout);
        props.put("mail.transport.protocol", transportProtocol);

        return mailSender;
    }
}
