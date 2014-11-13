package com.xqbase.util.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

import com.xqbase.util.Pool;

public class TransportPool extends Pool<Transport, MessagingException> {
	public static boolean validate(String email) {
		int length = email.getBytes().length;
		if (length > 32 || length != email.length()) {
			return false;
		}
		try {
			new InternetAddress(email, null).validate();
			return true;
		} catch (MessagingException e) {
			return false;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Session session;
	private InternetAddress from;

	public TransportPool(Session session, String protocol,
			String user, String password, InternetAddress from) {
		super(() -> {
			Transport transport = session.getTransport(protocol);
			try {
				transport.connect(user, password);
				return transport;
			} catch (MessagingException e) {
				transport.close();
				throw e;
			}
		}, Transport::close, 60000);
		this.session = session;
		this.from = from;
	}

	private static void setText(MimePart part, String plainText,
			String htmlText) throws MessagingException {
		if (plainText == null) {
			if (htmlText == null) {
				part.setText("");
			} else {
				part.setContent(htmlText, "text/html; charset=utf-8");
			}
		} else {
			if (htmlText == null) {
				part.setContent(plainText, "text/plain; charset=utf-8");
			} else {
				MimeMultipart multipart = new MimeMultipart();
				multipart.setSubType("alternative");
				MimeBodyPart plainPart = new MimeBodyPart();
				plainPart.setContent(plainText, "text/plain; charset=utf-8");
				multipart.addBodyPart(plainPart);
				MimeBodyPart htmlPart = new MimeBodyPart();
				htmlPart.setContent(htmlText, "text/html; charset=utf-8");
				multipart.addBodyPart(htmlPart);
				part.setContent(multipart);
			}
		}
	}

	private MimeMessage write(String[] recipients, String replyTo,
			String subject, String plainText, String htmlText,
			int priority, DataSource... attachments) throws MessagingException {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(from);
		InternetAddress[] addresses = new InternetAddress[recipients.length];
		for (int i = 0; i < recipients.length; i ++) {
			addresses[i] = new InternetAddress(recipients[i]);
		}
		message.setRecipients(Message.RecipientType.TO, addresses);
		if (replyTo != null) {
			message.setReplyTo(new Address[] {new InternetAddress(replyTo)});
		}
		message.setSubject(subject);
		message.setHeader("X-Priority", "" + priority);
		if (attachments.length == 0) {
			setText(message, plainText, htmlText);
		} else {
			MimeMultipart multipart = new MimeMultipart();
			MimeBodyPart bodyPart = new MimeBodyPart();
			setText(bodyPart, plainText, htmlText);
			multipart.addBodyPart(bodyPart);
			for (DataSource attachment : attachments) {
				bodyPart = new MimeBodyPart();
				bodyPart.setFileName(attachment.getName());
				bodyPart.setDataHandler(new DataHandler(attachment));
				multipart.addBodyPart(bodyPart);
			}
			message.setContent(multipart);
		}
		message.saveChanges();
		return message;
	}

	private void send(MimeMessage message) throws MessagingException {
		try (Entry entry = borrow()) {
			entry.getObject().sendMessage(message, message.getAllRecipients());
			entry.setValid(true);
		}
	}

	public void write(OutputStream out, String recipient, String replyTo,
			String subject, String plainText, String htmlText, int priority,
			DataSource... attachments) throws MessagingException, IOException {
		write(out, new String[] {recipient},
				replyTo, subject, plainText, htmlText, priority, attachments);
	}

	public void write(OutputStream out, String[] recipients, String replyTo,
			String subject, String plainText, String htmlText, int priority,
			DataSource... attachments) throws MessagingException, IOException {
		write(recipients, replyTo, subject,
				plainText, htmlText, priority, attachments).writeTo(out);
	}

	public void send(InputStream in) throws MessagingException {
		send(new MimeMessage(session, in));
	}

	public void send(String recipient, String replyTo,
			String subject, String plainText, String htmlText, int priority,
			DataSource... attachments) throws MessagingException {
		send(new String[] {recipient}, replyTo, subject,
				plainText, htmlText, priority, attachments);
	}

	public void send(String[] recipients, String replyTo,
			String subject, String plainText, String htmlText, int priority,
			DataSource... attachments) throws MessagingException {
		send(write(recipients, replyTo, subject,
				plainText, htmlText, priority, attachments));
	}
}