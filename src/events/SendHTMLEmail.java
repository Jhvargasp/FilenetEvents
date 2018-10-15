package events;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendHTMLEmail {
	ResourceBundle bundle=ResourceBundle.getBundle("events.config");
	
	
	public void sendEmail(String to,String subject, String content) {
	      // Sender's email ID needs to be mentioned
	      String from = bundle.getString("emailFrom");

	      // Assuming you are sending email from localhost
	      String host = bundle.getString("emailHost");

	      // Get system properties
	      Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", host);

	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(properties);

	      try {
	         // Create a default MimeMessage object.
	         MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(from));

	         String[] toArray = to.split(";");
			Address[] adresses=new Address[toArray.length];
	         for (int i = 0; i < adresses.length; i++) {
				adresses[i]=new InternetAddress(toArray[i]);
			}
			// Set To: header field of the header.
	         message.addRecipients(Message.RecipientType.TO, adresses);

	         // Set Subject: header field
	         message.setSubject(subject);

	         // Send the actual HTML message, as big as you like
	         message.setContent(content, "text/html");

	         // Send message
	         Transport.send(message);
	         System.out.println("Sent message successfully....");
	      } catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	   }
}
