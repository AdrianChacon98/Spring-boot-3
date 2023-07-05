package kafkaeventdriven.kafkaapp.email;

public interface EmailSender {

    public void send(String to, String email);
}
