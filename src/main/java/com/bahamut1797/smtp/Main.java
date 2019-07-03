package com.bahamut1797.smtp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * 
 * @author Jorge Ruiz
 * @version 1.0
 *
 */
public class Main {

	public static void main(String[] args) {
		if (args.length < 1) {
			File smtpFile = new File("defaultSMTP.conf");
			if (!smtpFile.exists()) {
				System.out.println("File \"defaultSMTP.conf\" not found. Run with \"configure\" command to create it.");
				System.out.println("Run \"help\" command for more information.");
				System.exit(107);
			}
			
			// Run the service
			sendMail(smtpFile);
		} else {
			// HELP Command
			if (args[0].equalsIgnoreCase("help")) {
				System.out.println("Read a file <filePaht> as a parameter, if you omitted, reads the file created by \"configure\" command called \"defaultSMTP.conf\".");
				System.out.println("The configuration parameters for SMTP comunication file are:");
				System.out.println("SMTP_HOST PORT");
				System.out.println("TO_EMAIL,TO_EMAIL2,...<space>[TO_EMAIL_CC,TO_EMAIL_CC2,...]<space>[TO_EMAIL_BCC,TO_EMAIL_BCC2,...]");
				System.out.println("FROM_EMAIL PASSWORD");
				System.out.println("TLS|SSL|NONE");
				System.out.println("FROM_NAME");
				System.out.println("SUBJECT");
				System.out.println("BODY_MESSAGE");
				System.out.println("[ATTACHMENT,ATTACHMENT2,...]");
				System.out.println("");
				System.out.println("Commands:");
				System.out.println("help\t\tThis command.");
				System.out.println("configure\tAn assistant to help you to create a configuration file for SMTP service.");
				System.out.println("");
				System.out.println("Exit error codes:");
				System.out.println("107 - File <fileName> not found.");
				System.out.println("108 - I/O Error");
				System.out.println("110 - Mail not sent + error msg");

				System.exit(0);
			} else if (args[0].equalsIgnoreCase("configure")) { // CONFIGURE Command
				createConfigureFile();
			} else { // Search for File send it
				File smtpFile = new File(args[0]);
				if (!smtpFile.exists()) {
					System.out.println("File \"" + args[0] + "\" not found.");
					System.exit(107);
				}
				
				// Run the service
				sendMail(smtpFile);
			}
		}
	}

	private static void createConfigureFile() {
		File smtpFile = new File("defaultSMTP.conf");

		if (!smtpFile.exists()) {
			smtpFile.delete();
		}

		try {
			Scanner sc = new Scanner(System.in);
			BufferedWriter bw = new BufferedWriter(new FileWriter(smtpFile));

			System.out.print("SMTP Host: ");
			String line = sc.nextLine();

			System.out.print("SMTP Port: ");
			line += " " + sc.nextLine();

			bw.append(line);
			bw.newLine();

			System.out.print("To Email(s) (comma separated): ");
			line = sc.nextLine();

			System.out.print("To CC Email(s) (comma separated): ");
			line += " " + sc.nextLine();

			System.out.print("To BCC Email(s) (comma separated): ");
			line += " " + sc.nextLine();

			bw.append(line);
			bw.newLine();

			boolean repeat = true;

			System.out.print("Valid ID Email (FROM Email): ");
			line = sc.nextLine();

			System.out.print("Email Password: ");
			line += " " + sc.nextLine();

			bw.append(line);
			bw.newLine();

			System.out.print("Sender Name: ");
			line = sc.nextLine();

			bw.append(line);
			bw.newLine();

			do {
				System.out.print("Do want to use a specific auth method (Default: NONE)? (Y/N): ");
				line = sc.nextLine();

				if (line.equalsIgnoreCase("Y")) {
					do {
						System.out.print("Which authentication method do you want to use? (TLS/SSL): ");
						String option = sc.nextLine().toUpperCase();

						if (option.equals("TLS")) {
							bw.append("TLS");
							bw.newLine();

							repeat = false;

							break;
						} else if (option.equals("SSL")) {
							bw.append("SSL");
							bw.newLine();

							repeat = false;
							break;
						}

					} while (repeat);
				} else if (line.equalsIgnoreCase("N")) {
					bw.append("NONE");
					bw.newLine();

					break;
				}
			} while (repeat);

			System.out.print("Subject: ");
			line = sc.nextLine();

			bw.append(line);
			bw.newLine();

			System.out.print("Body message (1 line text, HTML Support): ");
			line = sc.nextLine();

			bw.append(line);
			bw.newLine();

			System.out.print("Attachments (absolute paths, comma separated): ");
			line = sc.nextLine();

			bw.append(line);

			sc.close();
			bw.flush();
			bw.close();

		} catch (IOException e) {
			System.out.println("I/O Error \n" + e.getMessage());
			System.exit(108);
		}
	}

	static String fromEmail;
	static String fromEmailName;

	private static void sendMail(File smtpFile) {
		String server;
		int port;

		final String password;

		String line = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(smtpFile));

			// server port
			line = br.readLine();
			server = line.split(" ")[0];
			port = Integer.parseInt(line.split(" ")[1]);

			// toEmail toEmailCC
			line = br.readLine();
			String toEmail = null;
			String toEmailCC = null;
			String toEmailBCC = null;

			Pattern pattern = Pattern
					.compile("^([\\w|\\d|@|\\.|,]*)[ ]?([\\w|\\d|@|\\.|,]+)?[ ]?([\\w|\\d|@|\\.|,]+)?");
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				toEmail = matcher.group(1);
				toEmailCC = matcher.group(2);
				toEmailBCC = matcher.group(3);
			}

			// fromEmail password
			line = br.readLine();
			fromEmail = line.split(" ")[0];
			password = line.split(" ")[1];

			// fromEmailName
			fromEmailName = br.readLine();

			// authMethod
			String authMethod = br.readLine();

			// subject
			String subject = br.readLine();

			// msgBody
			String msgBody = br.readLine();

			// attachments
			String attachments = br.readLine();

			br.close();
			
			Properties props = new Properties();
			props.put("mail.smtp.host", server); // SMTP Host
			props.put("mail.smtp.port", port); // SMTP Port
			props.put("mail.smtp.auth", "true"); // enable authentication
			
			if (authMethod.equals("SSL")) {
				// SSL Factory
				props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			} else if (authMethod.equals("TLS")) {
				// enable STARTTLS
				props.put("mail.smtp.starttls.enable", "true"); 
			}
			
			// create Authenticator object to pass in Session.getInstance argument
			Authenticator auth = new Authenticator() {
				// override the getPasswordAuthentication method
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(fromEmail, password);
				}
			};

			Session session = Session.getInstance(props, auth);

			if (attachments != null && !attachments.equals("")) {
				sendAttachmentEmail(session, toEmail, toEmailCC, toEmailBCC, subject, msgBody, attachments);
			} else {
				sendEmail(session, toEmail, toEmailCC, toEmailBCC, subject, msgBody);
			}

		} catch (FileNotFoundException e) {
			System.out.println("File \"" +  smtpFile.getName() + "\" not found.");
			System.exit(107);
		} catch (IOException e) {
			System.out.println("I/O Error: \n" + e.getMessage());
			System.exit(108);
		}
	}

	/**
	 * Utility method to send simple HTML email
	 * 
	 * @param session
	 * @param toEmail
	 * @param subject
	 * @param body
	 */
	private static void sendEmail(Session session, String toEmail, String toEmailCC, String toEmailBCC, String subject,
			String body) {
		try {
			MimeMessage msg = new MimeMessage(session);
			// set message headers
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");

			msg.setFrom(new InternetAddress(fromEmail, fromEmailName));

			msg.setSubject(subject, "UTF-8");

			msg.setText(body, "UTF-8", "html");

			msg.setSentDate(new Date());

			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

			if (toEmailCC != null) {
				msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(toEmailCC, false));
			}

			if (toEmailBCC != null) {
				msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(toEmailBCC, false));
			}

			Transport.send(msg);
		} catch (MessagingException | UnsupportedEncodingException e) {
			System.out.println("Error: Mail not sent - " + e.getMessage());
			System.exit(110);
		}
	}

	/**
	 * Utility method to send email with attachment
	 * 
	 * @param session
	 * @param toEmail
	 * @param subject
	 * @param body
	 */
	private static void sendAttachmentEmail(Session session, String toEmail, String toEmailCC, String toEmailBCC,
			String subject, String body, String attachments) {
		try {
			MimeMessage msg = new MimeMessage(session);
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");

			msg.setFrom(new InternetAddress(fromEmail, fromEmailName));

			msg.setSubject(subject, "UTF-8");

			msg.setSentDate(new Date());

			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));

			if (toEmailCC != null) {
				msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(toEmailCC, false));
			}

			if (toEmailBCC != null) {
				msg.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(toEmailBCC, false));
			}

			// Create the message body part
			MimeBodyPart messageBodyPart = new MimeBodyPart();

			// Fill the message
			messageBodyPart.setContent(body, "text/html; charset=utf-8");

			// Create a multipart message for attachment
			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPart);

			// Second part is attachment
			for (String attachment : attachments.split(",")) {
				messageBodyPart = new MimeBodyPart();
				String filename = attachment;
				DataSource source = new FileDataSource(filename);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(source.getName());
				multipart.addBodyPart(messageBodyPart);
			}

			// Send the complete message parts
			msg.setContent(multipart);

			// Send message
			Transport.send(msg);
			// System.out.println("E-Mail Sent Successfully with attachment!!");

		} catch (MessagingException e) {
			System.out.println("Error: Mail not sent - " + e.getMessage());
			System.exit(110);
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error: Mail not sent - " + e.getMessage());
			System.exit(110);
		}
	}

}
