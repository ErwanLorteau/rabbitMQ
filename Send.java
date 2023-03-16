import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

public class Send {

    private final static String QUEUE_NAME = "hello";
    private final static String QUEUE_CONSUME= "consume";


    public static void main(String[] argv) throws Exception {
        if(argv.length!=1){
            System.out.println("Insert your id");
            System.exit(-1);
            return;
        }
        String id=argv[0];

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(QUEUE_CONSUME, false, false, false, null);

        String message = "Start "+id;
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
        System.out.println(" [x] Sent '" + message + "'");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String m= new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(m);
           
            if(m.equals("Ping")){
                String m2 = "Pong";
                channel.basicPublish("", QUEUE_NAME, null, m2.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + m2 + "'");
            }
        };
        channel.basicConsume(QUEUE_CONSUME, true, deliverCallback, consumerTag -> {
        });
    }
    
}