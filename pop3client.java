package pop3client;

import java.util.Properties;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

public class pop3client {
  private static String OTP_MAIL_SUBJECT = "[Gateprov3]OTP Code";
  
  private static String OTP_MAIL_HOST = "pop3.viettel.com.vn";
  
  public static void main(String[] args) throws Exception {
    String otp = "";
    long timeSent = 0L;
    if (args.length < 2) {
      usage();
      return;
    } 
    String user = args[0];
    String pass = args[1];
    if (args.length > 2)
      timeSent = Long.parseLong(args[2]); 
    for (int i = 0; i < 5; i++) {
      otp = getNewestOTP(user, pass, timeSent);
      if (!otp.equals(""))
        break; 
      Thread.sleep(2000L);
    } 
    System.out.print(otp);
  }
  
  private static String getNewestOTP(String user, String pass, long timeSent) throws Exception {
    Store store = createPop3sConnection(user, pass);
    String otp = "";
    store.connect();
    Folder inbox = store.getFolder("Inbox");
    inbox.open(1);
    Message[] messages = inbox.getMessages();
    if (messages.length == 0)
      System.out.println("No messages found."); 
    int countMailInbox = messages.length;
    int maxFileToRead = (countMailInbox > 10) ? 10 : countMailInbox;
    for (int i = 1; i <= maxFileToRead; i++) {
      int currentInbox = countMailInbox - i;
      Message msg = messages[currentInbox];
      String subject = msg.getSubject();
      if (subject != null && subject.contains(OTP_MAIL_SUBJECT)) {
        if (timeSent == 0L || (msg
          .getSentDate() != null && msg.getSentDate().getTime() >= timeSent)) {
          Multipart mp = (Multipart)msg.getContent();
          BodyPart bp = mp.getBodyPart(0);
          String content = bp.getContent().toString();
          otp = content.substring(30, 36);
        } 
        break;
      } 
    } 
    inbox.close(true);
    store.close();
    return otp;
  }
  
  private static void usage() {
    System.out.println("java -jar jar_name USERNAME PASSWORD [SENDTIME(milisecond)]");
  }
  
  public static Store createPop3sConnection(String username, String pass) throws Exception {
    Properties props = new Properties();
    String HOST = OTP_MAIL_HOST;
    props.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    props.setProperty("mail.pop3.socketFactory.fallback", "false");
    props.setProperty("mail.pop3.port", "995");
    props.setProperty("mail.pop3.socketFactory.port", "995");
    props.setProperty("mail.pop3.ssl.trust", "*");
    URLName urln = new URLName("pop3", HOST, Integer.parseInt("995"), null, username, pass);
    Session session = Session.getInstance(props, null);
    Store store = session.getStore(urln);
    return store;
  }
}
