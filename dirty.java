import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class PingPong {

    /**Arguments**/
    //Queues for exchanges
    private final static String QUEUE_NAME = "ping_pong_queue"; //for ping/pong
    private final static String ID_EXCHANGE_NAME = "id_exchange"; //for ids sharing
    private final static String PING_COMMAND = "ping";

    private static boolean pingStarted = false; //to say if the node is busy
    private static int exchangesLeft = 0;
    private static int nodeId; //our id
    private static Set<Integer> nodeIds = new HashSet<>(); //to know nodes id

    public static void main(String[] argv) throws Exception {

        //Setup the queues
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        //Define our id
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Enter a node ID: ");
        nodeId = Integer.parseInt(reader.readLine());
        System.out.println("Node ID set to " + nodeId);

        // Declare our id to all other nodes
        channel.exchangeDeclare(ID_EXCHANGE_NAME, "fanout");
        String messageId = Integer.toString(nodeId);
        channel.basicPublish(ID_EXCHANGE_NAME, "", null, messageId.getBytes("UTF-8"));

        //Make sur it's unique --> TOFIX
        Consumer consumerId = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, com.rabbitmq.client.AMQP.BasicProperties properties, byte[] body) throws IOException {
                int id = Integer.parseInt(new String(body, "UTF-8"));
                if (id != nodeId) {
                    nodeIds.add(id);
                }
            }
        };

        String replyQueueName1 = channel.queueDeclare().getQueue();
        channel.basicConsume(replyQueueName1, true, consumerId);
        channel.queueBind(replyQueueName1, ID_EXCHANGE_NAME, "");

        while (nodeIds.contains(nodeId)) {
            System.out.println("Node ID already in use, enter a new node ID: ");
            nodeId = Integer.parseInt(reader.readLine());
        }
        System.out.println("Node ID set to " + nodeId);
        nodeIds.add(nodeId);


        //LOOP ON COMMANDS
        while (true) {
            //We start by verifying the pending request
            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                      String message = new String(body, "UTF-8");
                      System.out.println("Received message: " + message);
                      //TODO --> resend ping or pong depending on the request with a decreased counter of remaining exehcange, set busy, and if the counter is 1 or 0, send nothing and set busy to false
                      pingStarted = true ;

                }
            };


            //migth not be the right syntax but just check if the queue is empty, if yes, call the previously defined function that perform the answer
            channel.basicConsume(queueName, true, consumer);

             if (consumer.getNextDelivery() == null) {
                    System.out.println("No message found in the queue yet");

             }

             //if nothing to proceed & not already in a exchange, then can  we can send ping commands
             if (!pingStarted) {

                System.out.println("Enter a command:");
                String input = reader.readLine();
                String[] tokens = input.split(" ");

                //SEND PING CASE
                if (tokens.length == 3 && tokens[0].equals(PING_COMMAND)) {
                    int destNodeId = Integer.parseInt(tokens[1]);

                    //verify we dont ping ourself
                    if (destNodeId == nodeId) {
                        System.out.println("Cannot ping self");
                        continue;
                    }
                    //make sur we're not busy
                     if (pingStarted) {
                         System.out.println("Ping request already in progress for node " + destNodeId);
                         continue;
                     }

                     //todo, make sure we dont ping someone who don't exist (no id)

                     //otherwise, perform the send request
                     int exchangesLeft = Integer.parseInt(tokens[2]);
                     channel.basicPublish("pong", QUEUE_NAME, null, responseMessage.getBytes("UTF-8"));
                     //set oursleff to busy

                }
             }


