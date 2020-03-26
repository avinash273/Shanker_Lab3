/*
Avinash Shanker
Roll No: 1001668570
ID: AXS8570
University of Texas, Arlington
*/

//References are included in the code

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.event.*;

//This Action listner class if used for client UI
public class ClientUI implements ActionListener {
    //Jframe for client interface
    JFrame ClientFrame;
    JComboBox<String> ClientMenu;
    JComboBox<String> ClientMenu2;
    JTextField PortTxtNo;
    JTextArea SendMsg;
    //Button variables set on client UI interface
    JButton ConnectBtn, replyBtn, ClientBtn;
    JTextField TxtMsgField, UsrTxt;
    Client client;
    JButton StopBtn;
    //Check message from server queue button
    JButton CheckMsg;
    boolean alive;

    //https://stackoverflow.com/questions/5600422/method-to-find-string-inside-of-the-text-file-then-getting-the-following-lines
    public boolean checkOnline(String username) {
        var file = new File("online.txt");

        try {
            Scanner scanner = new Scanner(file);

            //now read the file line by line...
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                System.out.println(line);
                if (line.contains(username)) {
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            //handle this
        }
        return false;
    }


    //Message queueing for users
    public void WriteToUserQueue(String FromUsername, String ToUsername, String MessageSent) {
        String UserQueueName = ToUsername + ".txt";
        try {
            //setting date field for logging
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date CurrDate = new Date();
            System.out.println("Check file exist: " + UserQueueName);
            BufferedWriter UserQueue = new BufferedWriter(new FileWriter(UserQueueName, true));
            System.out.println("Text from textbox: " + MessageSent);
            //Write to Queue
            String Content = CurrDate + " " + FromUsername + ": " + MessageSent + "\n";
            UserQueue.write(Content);
            UserQueue.close();
            System.out.println("Successfully Wrote to user queue: " + Content);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    //https://stackoverflow.com/questions/5600422/method-to-find-string-inside-of-the-text-file-then-getting-the-following-line
    //https://stackoverflow.com/questions/3935791/find-and-replace-words-lines-in-a-file/3936452
    //This code is used to delete the users who logged out from temp file, its invoked when user logs out
    public void DeleteOnline(String username) throws IOException {
        Path path = Paths.get("online.txt");
        Charset charset = StandardCharsets.UTF_8;
        String content = Files.readString(path, charset);
        content = content.replaceAll(username, "");
        Files.write(path, content.getBytes(charset));
    }

    //https://github.com/SrihariShastry/socketProgramming/blob/master/src/lab1/Client.java
    public static class Client {
        //Client class keepup socket and I/O  transfers with server
        public ObjectInputStream iStream;
        public ObjectOutputStream oStream;
        public Socket clientSocket;
        public ClientUI ClientInterface;
        public String clientUsrnm;
        public int portNo;

        //assigning client  variables
        Client(int portNo, String clientUsrnm, ClientUI ClientInterface) {
            this.portNo = portNo;
            this.clientUsrnm = clientUsrnm;
            this.ClientInterface = ClientInterface;
        }


        //get  function for client username
        public String getUsername() {
            return clientUsrnm;
        }

        //Socket creation function
        public boolean startClient() {
            //create new socket on localhost and given port number
            try {
                String server = "localhost";
                clientSocket = new Socket(server, portNo);
            }
            //catch error if unable to create socket
            catch (Exception err) {
                PrintMsg("Unable to connect to server: " + err);
                return false;
            }
            //Display connection message
            String value = "Connected now " + clientSocket.getInetAddress() + ":" + clientSocket.getPort();
            PrintMsg(value);

            //Reply button enabled after connection is set
            ClientInterface.replyBtn.setEnabled(true);
            //Setting up I/O streams
            try {
                iStream = new ObjectInputStream(clientSocket.getInputStream());
                oStream = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (IOException err) {
                PrintMsg("Unable to create I/O stream " + err);
                return false;
            }

            //handle server response
            new SResponse().start();
            //send client output stream to server
            try {
                oStream.writeObject(clientUsrnm);
            } catch (IOException err) {
                //unable connect print message to client interface
                PrintMsg("Unable to login " + err);
                //close connection
                CloseConnection();
                return false;
            }
            //else connection was live
            return true;
        }

        //print message on console function
        public void PrintMsg(String value) {
            ClientInterface.updateClientLog(value);
        }

        //send message to server
        void sendMessage(String[] value) {
            try {
                oStream.writeObject(value);
            } catch (IOException err) {
                PrintMsg("Unable to write to server " + err);
            }
        }

        //Close sockets and I/O streams
        public void CloseConnection() {
            try {
                if (iStream != null) iStream.close();
                if (oStream != null) oStream.close();
                if (clientSocket != null) clientSocket.close();
            } catch (Exception err) {
                err.printStackTrace();
            }

        }

        //Server response function N thread created for each client
        class SResponse extends Thread {
            public void run() {
                while (true) {
                    try {
                        String[] response = (String[]) iStream.readObject();
                        //get the response type of message
                        String msgType = response[2].substring(13);
                        if (msgType.trim().equalsIgnoreCase("message")) {
                            ClientInterface.updateClientLog(response[6]);
                        } else {
                            ClientInterface.updateClientList(response);
                        }
                    } catch (Exception err) {
                        //Server offline
                        PrintMsg("Server is offline: " + err);
                        if (ClientInterface != null)
                            ClientInterface.connectionFailed();
                        break;
                    }

                }
            }
        }
    }

    //Main function of client which first call the client UI class
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                ClientUI window = new ClientUI();
                window.ClientFrame.setVisible(true);
            } catch (Exception err) {
                err.printStackTrace();
            }
        });
    }

    //This function calls initalize which has the jframe
    //https://github.com/SrihariShastry/socketProgramming/blob/master/src/lab1/ClientGUI.java
    public ClientUI() {
        initialize();
    }

    //Client frame function to set the client console
    public void initialize() {
        ClientFrame = new JFrame();
        ClientFrame.setTitle("Client Console");
        //Client frame size
        ClientFrame.setBounds(100, 100, 400, 400);
        ClientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JScrollPane ClientPane = new JScrollPane();
        //declaring buttons on client panel
        replyBtn = new JButton("Reply");
        replyBtn.setForeground(Color.BLUE);
        replyBtn.addActionListener(this);
        //Connect to server push button
        ConnectBtn = new JButton("Connect");
        ConnectBtn.addActionListener(this);
        ConnectBtn.setForeground(Color.BLUE);

        //Button to check message from server queue
        CheckMsg = new JButton("Check Msg");
        CheckMsg.addActionListener(this);
        CheckMsg.setForeground(Color.BLUE);

        //Message typing field with column size set to 10
        TxtMsgField = new JTextField();
        TxtMsgField.setColumns(10);
        UsrTxt = new JTextField();
        //setting up more buttons and text fields
        UsrTxt.setText("user");
        UsrTxt.setColumns(10);
        JLabel ClientUsrnm = new JLabel(" Username");
        ClientUsrnm.setForeground(Color.BLUE);
        StopBtn = new JButton(" Stop");
        StopBtn.setForeground(Color.BLUE);
        StopBtn.addActionListener(this);
        ClientBtn = new JButton("Online");
        ClientBtn.addActionListener(this);
        ClientBtn.setForeground(Color.BLUE);
        //Drop down box  for list of online users
        ClientMenu = new JComboBox<>();
        ClientMenu.setModel(new DefaultComboBoxModel<>(new String[]{"Broadcast"}));
        ClientMenu.setSelectedIndex(0);
        ClientMenu.setForeground(Color.BLUE);

        //Drop down box  for list of online users
        ClientMenu2 = new JComboBox<>();
        ClientMenu2.setModel(new DefaultComboBoxModel<>(new String[]{"Broadcast"}));
        ClientMenu2.setSelectedIndex(0);
        ClientMenu2.setForeground(Color.BLUE);


        PortTxtNo = new JTextField();
        PortTxtNo.setDisabledTextColor(Color.LIGHT_GRAY);
        PortTxtNo.setText("1070");
        PortTxtNo.setColumns(10);
        JLabel PortNoLabel = new JLabel(" Port No    ");
        PortNoLabel.setForeground(Color.BLUE);

        //This part of code is to setup the code for client GUIn layout
        var ClientLayout = new GroupLayout(ClientFrame.getContentPane());
        ClientLayout.setHorizontalGroup(
                ClientLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(ClientLayout.createSequentialGroup()
                                .addGroup(ClientLayout.createParallelGroup(Alignment.LEADING)
                                        .addGroup(ClientLayout.createParallelGroup(Alignment.LEADING)
                                                .addComponent(StopBtn)
                                                .addComponent(ClientPane, GroupLayout.PREFERRED_SIZE, 398, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(ClientLayout.createSequentialGroup()
                                                .addComponent(ClientUsrnm)
                                                .addComponent(UsrTxt, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(ConnectBtn)
                                                .addComponent(CheckMsg))
                                        .addGroup(ClientLayout.createSequentialGroup()
                                                .addComponent(PortNoLabel)
                                                .addComponent(PortTxtNo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        )
                                        .addGroup(ClientLayout.createSequentialGroup()
                                                .addComponent(TxtMsgField, GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(replyBtn))
                                        .addGroup(ClientLayout.createSequentialGroup()
                                                .addComponent(ClientMenu, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                .addComponent(ClientMenu2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(ComponentPlacement.UNRELATED)
                                                .addComponent(ClientBtn)))
                        )
        );
        ClientLayout.setVerticalGroup(
                ClientLayout.createParallelGroup(Alignment.TRAILING)
                        .addGroup(ClientLayout.createSequentialGroup()
                                .addGroup(ClientLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(ClientUsrnm)
                                        .addComponent(UsrTxt))
                                .addGroup(ClientLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(PortNoLabel)
                                        .addComponent(PortTxtNo)
                                        .addComponent(ConnectBtn)
                                        .addComponent(CheckMsg))
                                .addGroup(ClientLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(TxtMsgField)
                                        .addComponent(replyBtn))
                                .addGroup(ClientLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(ClientMenu)
                                        .addComponent(ClientMenu2)
                                        .addComponent(ClientBtn))
                                .addComponent(ClientPane, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                                .addComponent(StopBtn)
                        )
        );

        //Function button on the frame to be enabled or disabled
        SendMsg = new JTextArea();
        ClientPane.setViewportView(SendMsg);
        ClientFrame.getContentPane().setLayout(ClientLayout);
        ConnectBtn.setEnabled(true);
        CheckMsg.setEnabled(false);
        StopBtn.setEnabled(false);
        ClientBtn.setEnabled(false);
        replyBtn.setEnabled(false);
    }


    public void updateClientLog(String value) {
        SendMsg.append("\n" + value);
    }

    //Check connection to server, if  unable to connect. Reset buttons
    void connectionFailed() {
        ConnectBtn.setEnabled(true);
        CheckMsg.setEnabled(false);
        StopBtn.setEnabled(false);
        ClientBtn.setEnabled(false);
        TxtMsgField.setText("Usrnm");
        PortTxtNo.setText("");
        PortTxtNo.setEditable(true);
        alive = false;
    }

    @Override
    public void actionPerformed(ActionEvent err) {
        Object ClientObj = err.getSource();
        String[] ClientRqst = new String[7];
        String[] ClientRqst2 = new String[7];
        //Implementing Message Queue
        String[] MessageQueueing = new String[7];
        //Logout button for the client
        if (ClientObj == StopBtn) {
            //Headers for client request
            ClientRqst[0] = "DELETE / HTTP/1.1";
            ClientRqst[1] = "Host: localhost";
            ClientRqst[2] = "User-Agent: " + client.getUsername();
            ClientRqst[3] = "Content-Type: Text";
            ClientRqst[4] = "Content-Length =  0";
            ClientRqst[5] = "\r\n";
            ClientRqst[6] = " ";

            try {
                DeleteOnline(client.getUsername());
            } catch (IOException e) {
                e.printStackTrace();
            }
            client.sendMessage(ClientRqst);
            ConnectBtn.setEnabled(true);
            CheckMsg.setEnabled(false);
            StopBtn.setEnabled(false);
            ClientBtn.setEnabled(false);
            TxtMsgField.setEditable(true);
            UsrTxt.setEditable(true);
            PortTxtNo.setEditable(true);

        } else if (ClientObj == ClientBtn) {
            //Headers for client request
            ClientRqst[0] = "GET Clients HTTP/1.1";
            ClientRqst[1] = "Host: localhost";
            ClientRqst[2] = "User-Agent: " + client.getUsername();
            ClientRqst[3] = "Content-Type: client-list";
            ClientRqst[4] = "Content-Length =  0";
            ClientRqst[5] = "\r\n";
            ClientRqst[6] = " ";
            client.sendMessage(ClientRqst);

            ClientRqst2[0] = "GET Clients HTTP/1.1";
            ClientRqst2[1] = "Host: localhost";
            ClientRqst2[2] = "User-Agent: " + client.getUsername();
            ClientRqst2[3] = "Content-Type: client-list";
            ClientRqst2[4] = "Content-Length =  0";
            ClientRqst2[5] = "\r\n";
            ClientRqst2[6] = " ";
            client.sendMessage(ClientRqst);
        } else if (ClientObj == replyBtn) {
            //check broadcast type of one to n or one to all type
            String to = Objects.requireNonNull(ClientMenu.getSelectedItem()).toString();
            ClientRqst[0] = "POST HTTP/1.1";
            ClientRqst[1] = "Host: localhost";
            ClientRqst[2] = "User-Agent: " + to;
            ClientRqst[3] = "Content-Type: message";
            ClientRqst[4] = "Content-Length = " + TxtMsgField.getText().length();
            ClientRqst[5] = "\r\n";
            ClientRqst[6] = client.getUsername();

            //queuing logic to first check is user is online or not to decide to write to queue or send message
            boolean isClientOnline;
            isClientOnline = checkOnline(to);
            if (!isClientOnline) {
                System.out.println("Entered To write for the file");
                WriteToUserQueue(client.getUsername(), to, TxtMsgField.getText());
            }

            String to2 = Objects.requireNonNull(ClientMenu2.getSelectedItem()).toString();
            ClientRqst2[0] = "POST HTTP/1.1";
            ClientRqst2[1] = "Host: localhost";
            ClientRqst2[2] = "User-Agent: " + to2;
            ClientRqst2[3] = "Content-Type: message";
            ClientRqst2[4] = "Content-Length = " + TxtMsgField.getText().length();
            ClientRqst2[5] = "\r\n";
            ClientRqst2[6] = client.getUsername();

            //broadcast message type
            if (ClientRqst[2].contains("broadcast")) {
                ClientRqst[6] += ": " + TxtMsgField.getText();
                client.sendMessage(ClientRqst);
            } else {
                ClientRqst[6] += ": " + TxtMsgField.getText();
                client.sendMessage(ClientRqst);
                ClientRqst2[6] += ": " + TxtMsgField.getText();
                if (ClientRqst2[6] != ClientRqst[6])
                    client.sendMessage(ClientRqst2);
            }
            TxtMsgField.setText("");
        } else if (ClientObj == CheckMsg) {
            //queuing logic to first check is user is online or not to decide to write to queue or send message
            boolean isClientOnline;
            String QueueFile = client.getUsername();
            QueueFile += ".txt";

            isClientOnline = checkOnline(client.getUsername());
            if (isClientOnline) {
                String TestMessage = "\nChecking if working";
                System.out.println("Entered To write for the file");
                MessageQueueing[0] = "POST HTTP/1.1";
                MessageQueueing[1] = "Host: localhost";
                MessageQueueing[2] = "User-Agent: " + client.getUsername();
                MessageQueueing[3] = "Content-Type: message";
                MessageQueueing[4] = "Content-Length = " + TestMessage.length();
                MessageQueueing[5] = "\r\n";
                MessageQueueing[6] = client.getUsername();

                https://stackoverflow.com/questions/5868369/how-to-read-a-large-text-file-line-by-line-using-java
                //Code to read from message queue line by line from the server
                try (BufferedReader br = new BufferedReader(new FileReader(QueueFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        System.out.println("\nStarted Reading from queue");
                        System.out.println(line);
                        MessageQueueing[6] += ": " + line +'\n';
                    }
                    //https://stackoverflow.com/questions/6994518/how-to-delete-the-content-of-text-file-without-deleting-itself
                    //Remove contents from the queue
                    PrintWriter pw = new PrintWriter(QueueFile);
                    pw.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                client.sendMessage(MessageQueueing);
            }
        } else if (ClientObj == ConnectBtn) {
            String clientUsrnm = UsrTxt.getText().trim();
            String portNumber = PortTxtNo.getText().trim();
            //check if either of username or port no is empty
            //System.out.println(client.getUsername());
            boolean isOnline = checkOnline(clientUsrnm);
            if (clientUsrnm.length() == 0 || portNumber.length() == 0)
                return;

            if (isOnline) {
                client.PrintMsg(clientUsrnm + " is already logged in.");
                return;
            }

            int portNo;
            try {
                portNo = Integer.parseInt(portNumber);
            } catch (Exception e) {
                return;
            }

            //New client object to send responses to server
            client = new Client(portNo, clientUsrnm, this);
            if (!client.startClient())
                return;
            TxtMsgField.setText("");
            alive = true;
            ConnectBtn.setEnabled(false);
            CheckMsg.setEnabled(true);
            StopBtn.setEnabled(true);
            ClientBtn.setEnabled(true);
            PortTxtNo.setEditable(false);
            UsrTxt.setEditable(false);
        }

    }

    //This functions  is to get the client connected to server actively now
    public void updateClientList(String[] response) {
        String[] list = response[6].split(",");
        String[] list2 = response[6].split(",");
        for (int i = 0; i < list2.length; i++) {
            System.out.println("List2: " + list2[i]);
            if (list2[i].contains("BROADCAST")) {
                list2[i] = "";
                System.out.println("List2 at null: " + list2[i]);
            }
        }
        ClientMenu.setModel(new DefaultComboBoxModel<>(list));
        ClientMenu2.setModel(new DefaultComboBoxModel<>(list2));
    }

}
