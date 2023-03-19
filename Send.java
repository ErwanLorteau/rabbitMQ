import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class Send {

    private final static String QUEUE_NAME = "hello";
    private final static String QUEUE_CONSUME = "consume";

    public static void main(String[] argv) throws Exception {
        if (argv.length != 1) {
            System.out.println("Insert your id");
            System.exit(-1);
            return;
        }
        String id = argv[0];
        System.out.println("Write ping to start");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        String ping = reader.readLine();
        if (ping.equals("ping")) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queueDeclare(QUEUE_CONSUME, false, false, false, null);

            // sending START
            String message = "Start " + id;
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));

            // Receving
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String m = new String(delivery.getBody(), StandardCharsets.UTF_8);
                String[] ms = m.split(" ");
                System.out.println(m);
                if (ms[0].equals("Ping") && ms[1].equals(id)) {
                    // System.out.println("o meu id: " + id);
                    String m2 = "Pong " + id;
                    channel.basicPublish("", QUEUE_NAME, null, m2.getBytes(StandardCharsets.UTF_8));
                    // System.out.println("PING");
                } else if (ms[0].equals("wait") && ms[1].equals(id)) {

                    System.out.println("WAIT");
                    System.exit(0);

                } else if (ms[0].equals("end") && ms[1].equals(id)) {

                    System.out.println("END");
                    System.exit(0);
                }
            };
            channel.basicConsume(QUEUE_CONSUME, true, deliverCallback, consumerTag -> {
            });
        }

    }

}