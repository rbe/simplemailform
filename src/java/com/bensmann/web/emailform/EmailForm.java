/*
 * Email.java
 * Created on 07.10.2007, 18:24:54
 */
package com.bensmann.web.emailform;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

/**
 *
 * @author Ralf_Bensmann
 */
public class EmailForm extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        process(arg0, arg1);
    }

    @Override
    protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException {
        process(arg0, arg1);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String subject = getInitParameter("subject");
        String recipient = getInitParameter("recipient");
        String sender = getInitParameter("sender");
        String smtpHost = getInitParameter("smtphost");
        int smtpPort = Integer.valueOf(getInitParameter("smtpport"));
        // Map of request parameters
        Map parameterMap = request.getParameterMap();
        // Create email body
        StringBuilder sb = new StringBuilder();
        // Header
        String header = getInitParameter("email_header");
        if (header != null) {
            sb.append(header + "<br/>\n");
        }
        // Request parameter
        @SuppressWarnings(value = "unchecked")
        List<String> keys =
                new ArrayList<String>(parameterMap.keySet());
        Collections.sort(keys);
        for (Object o : keys) {
            sb.append(o + ": " + request.getParameter("" + o) + "<br/>\n");
        }
        // Footer
        String footer = getInitParameter("email_footer");
        if (footer != null) {
            sb.append(footer + "<br/>\n");
        }
        // Send email
        boolean ok = true;
        HtmlEmail email = new HtmlEmail();
        try {
            email.addTo(recipient, recipient);
            email.setFrom(sender, sender);
            email.addReplyTo(sender, sender);
            email.setSubject(subject);
            email.setHtmlMsg(sb.toString());
            email.setHostName(smtpHost);
            email.setSmtpPort(smtpPort);
            email.send();
        } catch (EmailException e) {
            e.printStackTrace();
            ok = false;
        } finally {
            email = null;
        }
        // Forward
        StringBuilder forward = new StringBuilder();
        if (ok) {
            forward.append(getInitParameter("forward_ok"));
        } else {
            forward.append(getInitParameter("forward_error"));
        }
        forward.append("&qs=" + request.getParameter("qs"));
        // Log email
        Logger.getLogger(EmailForm.class.getName()).info("Subject: " + subject + "\nSender: " + sender + "\nRecipient: " + recipient + "\nBody:\n" + sb.toString() + "\nForwarding to: " + forward.toString());
        //
        response.sendRedirect(forward.toString());
    }
}