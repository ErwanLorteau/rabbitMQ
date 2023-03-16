import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Recv {

    private final static String QUEUE_NAME = "hello";
    private final static String QUEUE_CONSUME = "consume";
    private ArrayList<String> clients = new ArrayList<String>();
    static boolean started = false;

    static int i = 0;

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
            String[] messages = message.split(" ");

            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            if (messages[0].equals("Start")) {
                // addRequest(messages[1]);
                if (!isStarted()) {
                    start();
                    System.out.println("Client wants to start");

                    String m = "Ping " + messages[1];
                    channel.basicPublish("", QUEUE_CONSUME, null, m.getBytes(StandardCharsets.UTF_8));
                } else if (isStarted()) {
                    System.out.println("Client needs to wait");

                    String m = "wait";
                    channel.basicPublish("", QUEUE_CONSUME, null, m.getBytes(StandardCharsets.UTF_8));
                }

            } else if (messages[0].equals("Pong")) {
                System.out.println("PONG");
                if (getI() != 30) {
                    incI();
                    String m = "Ping " + messages[1];
                    channel.basicPublish("", QUEUE_CONSUME, null, m.getBytes(StandardCharsets.UTF_8));
                }

            }
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });

    }

    public void addRequest(String id) {
        clients.add(id);
    }

    public static int getI() {
        return i;
    }

    public static void incI() {
        i++;
    }

    public static void start() {
        started = true;
    }

    public static boolean isStarted() {
        return started;
    }

}
