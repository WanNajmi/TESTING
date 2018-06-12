import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class smtpClient {

    private static Socket socket;
    private static DataOutputStream OutputStream;
    private static DataInputStream InputStream;
    private static BufferedReader reader;
    private static int isDebugging;
    private static String hostname = "";
    private static String replyMessage;

   
    public static void open(String hostname) {
        try {
            socket = new Socket(hostname, 25);
            //socket.setSoTimeout(15*1000);
            System.out.println("Connection to host established.");

            OutputStream = new DataOutputStream(socket.getOutputStream());
            InputStream = new DataInputStream(socket.getInputStream());

        } catch (UnknownHostException e) {
            System.err.println("ERROR. Host not found.");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    /**
     * This method will close an open connection. It starts by closing the input
     * and output streams, then the socket itself is closed.
     *
     * @throws IOException
     */
    public static void close() throws IOException {
        OutputStream.close();
        InputStream.close();
        socket.close();
        System.out.println("Connection closed.");
    }

   
    public static String readReplyFromServer() throws IOException {
        return InputStream.readLine();
    }

    /**
     * Main run method.
     * 1. Starts with checking if the parameters are correct.
     * 2. Opens the connection.
     * 3. Sends HELO message.
     * 4. Sends MAIL FROM message.
     * 5. Sends RCPT TO message.
     * 6. Sends DATA message.
     * 7. Sends SUBJECT message.
     * 8. Sends message BODY.
     * 9. Sends QUIT message.
     * 10. Closes the connection.
     */
    public static void main(String[] args) throws IOException {

        // Check for parameter errors. There must me 2 parameters.
        if (args.length == 0 || args.length == 1) {
            System.err.println("Usage: smtpClient <server> debug_option.");
            System.exit(0);
        } else {
            hostname = args[0];
            isDebugging = Integer.parseInt(args[1]);
        }

        // Open the socket connection using the hostname specified in commandline argument.
        open(hostname);

        // Create the BufferedReader for reading input from user.
        reader = new BufferedReader(new InputStreamReader(System.in));

        // READ CONNECTION ESTABLISHED MESSAGE
        replyMessage = readReplyFromServer();
        if (isDebugging == 1) {
            System.out.println("Message from server: " + replyMessage + "\n");
        }

        // HELO MESSAGE
        if (isDebugging == 1) {
            System.out.println("Sending HELO message to host.");
        }
        OutputStream.writeBytes("HELO " + "loki" + "\r\n");
        replyMessage = readReplyFromServer();
        if (!replyMessage.startsWith("250")) {
            System.err.println("ERROR. Something went wrong. Syntax is: HELO <hostname>");
            close();
            System.exit(0);
        }
        if (isDebugging == 1) {
            System.out.println("Message from server: " + replyMessage + "\n");
        }

        // MAIL FROM MESSAGE
        System.out.print("Please enter sender: ");
        String senderAddress = reader.readLine();
        if (isDebugging == 1) {
            //System.out.println("Sending MAIL FROM message to host.");
            System.out.println("Sending message: MAIL FROM: <" + senderAddress +">");
        }
        OutputStream.writeBytes("MAIL FROM: " + senderAddress + "\r\n");
        replyMessage = readReplyFromServer();
        if (!replyMessage.startsWith("250")) {
            System.err.println("ERROR. Something went wrong.");
            close();
            System.exit(0);
        }
        if (isDebugging == 1) {
            System.out.println("Message from server: " + replyMessage + "\n");
        }

        // RCPT TO MESSAGE
        System.out.print("Please enter a recipient: ");
        String recipientAddress = reader.readLine();
        if (isDebugging == 1) {
            //System.out.println("Sending RCPT TO message to host.");
            System.out.println("Sending message: RCPT TO: <" + recipientAddress +">");
        }
        OutputStream.writeBytes("RCPT TO: " + recipientAddress + "\r\n");
        replyMessage = readReplyFromServer();
        if (!replyMessage.startsWith("250")) {
            System.err.println("ERROR. Receiver email address is wrong. Try again.");
            close();
            System.exit(0);
        }
        if (isDebugging == 1) {
            System.out.println("Message from server: " + replyMessage + "\n");
        }

        // DATA MESSAGE
        if (isDebugging == 1) {
            System.out.println("Sending DATA message to host.");
        }
        OutputStream.writeBytes("DATA\r\n");
        replyMessage = readReplyFromServer();
        if (!replyMessage.startsWith("354")) {
            System.err.println("ERROR. Something went wrong.");
            close();
            System.exit(0);
        }
        if (isDebugging == 1) {
            System.out.println("Message from server: " + replyMessage + "\n");
        }

        // SUBJECT MESSAGE
        System.out.print("Please enter a subject: ");
        String subject = reader.readLine();
        OutputStream.writeBytes("Subject: " + subject + "\r\n");
        if (isDebugging == 1) {
            System.out.println("Sending message: To: <" + recipientAddress +">");
            System.out.println("Sending message: From: <" + senderAddress +">");
            System.out.println("Sending message: Subject: <" + subject +">\n");
        }

        OutputStream.writeBytes("To: " + recipientAddress + "\r\n");
        OutputStream.writeBytes("From: " + senderAddress + "\r\n");

        // MESSAGE BODY
        System.out.println("Enter your message. End with single period mark '.'");
        // We keep adding lines to the message until we receive the single period mark.
        while (true) {
            String message = reader.readLine();
            OutputStream.writeBytes(message + "\r\n");
            if (isDebugging == 1) {
                System.out.println("Sending message: " + message);
            }

            if (message.equals(".")) {
                OutputStream.writeBytes(".\r\n");
                break;
            }
        }
        replyMessage = readReplyFromServer();
        if (!replyMessage.startsWith("250")) {
            System.err.println("ERROR. Something went wrong.");
            close();
            System.exit(0);
        }
        if (isDebugging == 1) {
            System.out.println("Message from server: " + replyMessage);
        }

        // QUIT MESSAGE
        OutputStream.writeBytes("QUIT\r\n");
        if (isDebugging == 1) {
            System.out.println("\nSending message: QUIT");
        }
        readReplyFromServer();
        replyMessage = readReplyFromServer();
        if (!replyMessage.startsWith("221")) {
            System.err.println("ERROR. Something went wrong.");
            close();
            System.exit(0);
        }
        if (isDebugging == 1) {
            System.out.println("Message from server: " + replyMessage);
        }

        System.out.println("The message was successfully delivered.");

        // We are finished. We close the connection to the server.
        close();
    }
}