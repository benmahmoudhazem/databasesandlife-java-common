package com.databasesandlife.util;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * Sends emails via easyname.eu.
 */
public class EasynameEmailTransaction extends EmailTransaction {
    
    protected final String emailBoxName;
    protected final String emailBoxPasswordCleartext;
    
    /** @param emailBoxName for example "123mail1" */
    public EasynameEmailTransaction(String emailBoxName, String emailBoxPasswordCleartext) {
        super("smtp.easyname.eu");
        this.emailBoxName = emailBoxName;
        this.emailBoxPasswordCleartext = emailBoxPasswordCleartext;
    }
    
    @Override protected Properties newSessionProperties() {
        Properties props = super.newSessionProperties();
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.starttls.enable", "true");
        return props;
    }
    
    @Override protected Session newSession() {
        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailBoxName, emailBoxPasswordCleartext);
            }
        };
        return Session.getDefaultInstance(newSessionProperties(), auth);
    }
}
