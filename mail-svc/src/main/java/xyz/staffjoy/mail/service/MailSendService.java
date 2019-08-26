package xyz.staffjoy.mail.service;

import com.aliyuncs.IAcsClient;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.common.error.ServiceException;
import xyz.staffjoy.mail.config.AppConfig;
import xyz.staffjoy.mail.dto.EmailRequest;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

@Service
public class MailSendService {

    private static ILogger logger = SLoggerFactory.getLogger(MailSendService.class);

    @Autowired
    EnvConfig envConfig;

    @Autowired
    IAcsClient acsClient;

    @Autowired
    SentryClient sentryClient;

    private final static int OPEN_SSL = 1;

    private final static String STMP_HOST = "smtp.mxhichina.com";

    private final static int STMP_PORT = 465;

    private final static String STMP_USERNAME = "yangweixing@szhg9.com";
    private final static String STMP_PASSWORD = "Ywx20190326";


    @Async(AppConfig.ASYNC_EXECUTOR_NAME)
    public void sendMailAsync(EmailRequest req) {
        if (OPEN_SSL == 1 || "smtp.qq.com".equals(STMP_HOST)) {
            this.sendMailByTransport(req);
        }
    }

    public void sendMailByTransport(EmailRequest req) {

        Properties props = new Properties();
        props.put("mail.smtp.host", STMP_HOST);
        props.put("mail.smtp.port", String.valueOf(STMP_PORT));
        props.put("mail.smtp.auth", "true");
        props.put("mail.transport.protocol", "smtp");

        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.socketFactory.port", String.valueOf(STMP_PORT));
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtps.ssl.protocols", "TSLv1 TSLv1.1 TLSv1.2");
        props.put("mail.smtp.ssl.ciphersuites", "SSL_RSA_WITH_RC4_128_SHA SSL_RSA_WITH_RC4_128_MD5 TLS_RSA_WITH_AES_128_CBC_SHA TLS_DHE_RSA_WITH_AES_128_CBC_SHA TLS_DHE_DSS_WITH_AES_128_CBC_SHA SSL_RSA_WITH_3DES_EDE_CBC_SHA SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA SSL_RSA_WITH_DES_CBC_SHA SSL_DHE_RSA_WITH_DES_CBC_SHA SSL_DHE_DSS_WITH_DES_CBC_SHA SSL_RSA_EXPORT_WITH_RC4_40_MD5 SSL_RSA_EXPORT_WITH_DES40_CBC_SHA SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA TLS_EMPTY_RENEGOTIATION_INFO_SCSV");

        Session sendMailSession = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(STMP_USERNAME,
                                STMP_PASSWORD);
                    }
                });
        // 发送邮件
        try {
            Message mailMessage = new MimeMessage(sendMailSession);
            // 创建邮件发送者地址
            Address from = new InternetAddress(STMP_USERNAME, STMP_USERNAME);
            // 设置邮件消息的发送者
            mailMessage.setFrom(from);
            // 创建邮件的接收者地址，并设置到邮件消息中
            Address to = new InternetAddress(req.getTo());
            mailMessage.setRecipient(Message.RecipientType.TO, to);
            // 设置邮件消息的主题
            mailMessage.setSubject(req.getSubject());
            // 设置邮件消息发送的时间
            mailMessage.setSentDate(new Date());
            // 设置邮件消息的主要内容
            mailMessage.setContent(req.getHtmlBody(), "text/html;charset=utf-8");
            Transport.send(mailMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }


    /*@Async(AppConfig.ASYNC_EXECUTOR_NAME)
     public void sendMailAsync(EmailRequest req) {
        IToLog logContext = () -> {
            return new Object[] {
                    "subject", req.getSubject(),
                    "to", req.getTo(),
                    "html_body", req.getHtmlBody()
            };
        };

        // In dev and uat - only send emails to @jskillcloud.com
        if (!EnvConstant.ENV_PROD.equals(envConfig.getName())) {
            // prepend env for sanity
            String subject = String.format("[%s] %s", envConfig.getName(), req.getSubject());
            req.setSubject(subject);

            if (!req.getTo().endsWith(MailConstant.STAFFJOY_EMAIL_SUFFIX)) {
                logger.warn("Intercepted sending due to non-production environment.");
                return;
            }
        }

        SingleSendMailRequest mailRequest = new SingleSendMailRequest();
        mailRequest.setAccountName(MailConstant.FROM);
        mailRequest.setFromAlias(MailConstant.FROM_NAME);
        mailRequest.setAddressType(1);
        mailRequest.setToAddress(req.getTo());
        mailRequest.setReplyToAddress(false);
        mailRequest.setSubject(req.getSubject());
        mailRequest.setHtmlBody(req.getHtmlBody());

        try {
            SingleSendMailResponse mailResponse = acsClient.getAcsResponse(mailRequest);
            logger.info("Successfully sent email - request id : " + mailResponse.getRequestId(), logContext);
        } catch (ClientException ex) {
            Context sentryContext = sentryClient.getContext();
            sentryContext.addTag("subject", req.getSubject());
            sentryContext.addTag("to", req.getTo());
            sentryClient.sendException(ex);
            logger.error("Unable to send email ", ex, logContext);
        }
    }*/
}
