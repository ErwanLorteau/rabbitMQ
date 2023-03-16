import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;


public class Recv {

    private final static String QUEUE_NAME = "hello";
    private final static String QUEUE_CONSUME = "consume";

    boolean started=false;

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        
       
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(QUEUE_CONSUME, false, false, false, null);

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            String[] messages=message.split(" ");
            try{
                Thread.sleep(100);
            }catch(InterruptedException ie){
                Thread.currentThread().interrupt();
            }           
             if(messages[0].equals("Start")){
                System.out.println("Client wants to start");
                String m = "Ping";
                channel.basicPublish("", QUEUE_CONSUME, null, m.getBytes(StandardCharsets.UTF_8));
            }
             else if(message.equals("Pong")){
                System.out.println("PONG");
                String m = "Ping";
                channel.basicPublish("", QUEUE_CONSUME, null, m.getBytes(StandardCharsets.UTF_8));
            }
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });
    }

}