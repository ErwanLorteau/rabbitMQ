import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;

/**Server**/
public class Recv {

    private final static String QUEUE_NAME = "hello";
    private final static String QUEUE_CONSUME = "consume";
    private ArrayList<String> clients = new ArrayList<String>() ; //id of clients in the queue
    private ArrayList<String> numberOfRequestPerClient = new ArrayList<Integer>() ; //id of clients in the queue

    private int NumberOfRemainingMessageToProceedForCurrentClient = 0 ;
    private boolean busy = false ;

    boolean started=false;

    public static void main(String[] argv) throws Exception {

        /**Connexion**/
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

       /**Queue**/
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueDeclare(QUEUE_CONSUME, false, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");



        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            String[] messages=message.split(" ");

            if (busy) {
               clients.add(messages[1]) ; //Add id to the queue with the number of requested messages
               numberOfRequestPerClient.add(message[2]) ;
            } else { //proceed the request
                NumberOfRemainingMessageToProceedForCurrentClient = messages[2] ;
                try{
                    Thread.sleep(100); //Wait 100 ms before processing the message
                }catch(InterruptedException ie){
                    Thread.currentThread().interrupt();
                }

                //Send ping
                 if(messages[0].equals("Start")){
                    busy = true ;
                    System.out.println("Client " + messages[1] + " messages wants to start");
                    String m = "Ping";
                    channel.basicPublish("", QUEUE_CONSUME, null, m.getBytes(StandardCharsets.UTF_8));
                }

                //Send pong
                 else if(message.equals("Pong")){
                    System.out.println("PONG");
                    String m = "Ping";
                    channel.basicPublish("", QUEUE_CONSUME, null, m.getBytes(StandardCharsets.UTF_8));
                    NumberOfRemainingMessageToProceed-- ;
                }

                if (NumberOfRemainingMessageToProcess)
            }
        };

        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });
    }

}