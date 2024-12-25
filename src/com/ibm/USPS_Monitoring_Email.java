package com.ibm;

import java.io.File;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class USPS_Monitoring_Email {
	private static ReadProperties props = new ReadProperties();
	void email(String str, boolean email_flag, String dt, String path_Details_Excel) throws Exception, Exception {
		File filee = new File(path_Details_Excel);
		File[] fileList = filee.listFiles();
		String ExcelFileName="";
		if(fileList!=null) {
			for(int i=0;i<fileList.length;i++) {
				ExcelFileName = fileList[0].getName().toString();
			}
		}
		String file_Attachement=path_Details_Excel+ExcelFileName;
		String Email_TO = props.readPropertiesFile().getProperty("EMAIL_TO");
		String Email_TO_USPS = props.readPropertiesFile().getProperty("EMAIL_TO_USPS");
		String Email_CC = props.readPropertiesFile().getProperty("EMAIL_CC");
		String Email_CC_USPS = props.readPropertiesFile().getProperty("EMAIL_CC_USPS");
		String Email_Sender = props.readPropertiesFile().getProperty("EMAIL_SENDER_USPS");
		String host = "smtp.wellpoint.com";
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", host);
		Session session = Session.getDefaultInstance(properties);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(Email_Sender));
		String Email_Subject=props.readPropertiesFile().getProperty("EMAIL_SUBJECT_USPS");
		Email_Subject = Email_Subject+" - " +dt;
		message.setSubject(Email_Subject);
		if(email_flag) {
			//message.setFrom(new InternetAddress("DL-FileNetLightsOnSupport@anthem.com"));
			String[] recipientList = Email_TO_USPS.split(",");
			int counter =0;
			InternetAddress[] Email_Recipients_TO = new InternetAddress[recipientList.length];
			for(String recipient : recipientList) {
				Email_Recipients_TO[counter]=new InternetAddress(recipient.trim());
				counter++;
			}
			message.setRecipients(Message.RecipientType.TO, Email_Recipients_TO);
			//if(!(Email_CC.isEmpty())) {
				//System.out.println("Email_CC : "+Email_CC);
				String[] recipientList1 = Email_CC_USPS.split(",");
				int counter1 =0;
				InternetAddress[] Email_Recipients_CC = new InternetAddress[recipientList1.length];
				for(String recipient1 : recipientList1) {
					Email_Recipients_CC[counter1]=new InternetAddress(recipient1.trim());
					counter1++;
				}
				message.setRecipients(Message.RecipientType.CC, Email_Recipients_CC);
			//}
		}else {
			//message.setFrom(new InternetAddress(Email_Sender));
			String[] recipientList = Email_TO.split(",");
			int counter =0;
			InternetAddress[] Email_Recipients_TO = new InternetAddress[recipientList.length];
			for(String recipient : recipientList) {
				Email_Recipients_TO[counter]=new InternetAddress(recipient.trim());
				counter++;
			}
			message.setRecipients(Message.RecipientType.TO, Email_Recipients_TO);
		//	if(!(Email_CC.isEmpty())) {
				String[] recipientList1 = Email_CC.split(",");
				int counter1 =0;
				InternetAddress[] Email_Recipients_CC = new InternetAddress[recipientList1.length];
				for(String recipient1 : recipientList1) {
					Email_Recipients_CC[counter1]=new InternetAddress(recipient1.trim());
					counter1++;
				}
				message.setRecipients(Message.RecipientType.CC, Email_Recipients_CC);
		//	}
		}

		BodyPart messageBodyPart = new MimeBodyPart(); 
		messageBodyPart.setText(str);

		MimeBodyPart attachmentPart1 = new MimeBodyPart();
		attachmentPart1.attachFile(new File(file_Attachement));
		System.out.println(file_Attachement);
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);
		multipart.addBodyPart(attachmentPart1);
		message.setContent(multipart);
		Transport.send(message);
		System.out.println("Sent message successfully....");
	}
}
