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
    static String _id;

    static int i = 0;

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(QUEUE_CONSUME, false, false, false, null);

        System.out.println("Waiting for PING...");

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
                    start(messages[1]);
                    System.out.println("Client wants to start " + messages[1]);

                    String m = "Ping " + messages[1];
                    channel.basicPublish("", QUEUE_CONSUME, null, m.getBytes(StandardCharsets.UTF_8));
                } else if (isStarted()) {
                    System.out.println("Client needs to wait");

                    String m = "wait " + messages[1];
                    channel.basicPublish("", QUEUE_CONSUME, null, m.getBytes(StandardCharsets.UTF_8));
                }

            } else if (messages[0].equals("Pong") && messages[1].equals(getId())) {
                System.out.println(message);
                if (getI() != 30) {
                    incI();
                    String m = "Ping " + messages[1];
                    channel.basicPublish("", QUEUE_CONSUME, null, m.getBytes(StandardCharsets.UTF_8));
                } else {
                    System.out.println("chegou");
                    i = 0;
                    stop();
                    String m = "end " + messages[1];
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

    public static void start(String id) {
        started = true;
        _id = id;
    }

    public static void stop() {
        _id = null;
        started = false;
    }

    public static String getId() {
        return _id;
    }

    public static boolean isStarted() {
        return started;
    }

}
