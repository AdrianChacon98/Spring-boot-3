package kafkaeventdriven.kafkaapp.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;



@Component
public class EmailSenderImp implements EmailSender{


    private static Logger logger = LoggerFactory.getLogger(EmailSenderImp.class);
    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    @Async
    public void send(String to, String email) {

        try{

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,"utf-8");
            helper.setText(email,true);
            helper.setTo(to);
            helper.setSubject("Confirm your account");
            helper.setFrom("cursosprogramacion44@gmail.com");

            javaMailSender.send(mimeMessage);

        }catch(MessagingException e)
        {
            logger.info(e.getMessage());
        }

    }
}
