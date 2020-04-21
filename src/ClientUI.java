/**
 * Avinash Shanker
 * Roll No: 1001668570
 * ID: AXS8570
 * University of Texas, Arlington
 * Distributed Systems Lab3
 * Vector Clocks
 */

//References are included in the code

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;

//This Action listener class if used for client UI

/**
 * This is the main client UI class used as a action listener
 */
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

    //Setting date for the Client logs and connections
    //https://www.javatpoint.com/java-get-current-date

    /***
     * This function used to get the current time so that, logs can be printed with the time.
     * @return time
     */
    public String GetTimeNow() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    //https://stackoverflow.com/questions/5600422/method-to-find-string-inside-of-the-text-file-then-getting-the-following-lines

    /***
     * This fucntions is used to check which users are online now
     * @param username paramenter is the what enetered by user, for this lab3 it is A, B or C only
     * @return
     */
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

    /**
     * This functions is used to write function to user queue incase he logs out
     *
     * @param FromUsername
     * @param ToUsername
     * @param MessageSent
     */
    public void WriteToUserQueue(String FromUsername, String ToUsername, String MessageSent) {
        String UserQueueName = ToUsername + ".txt";
        try {
            System.out.println("Check file exist: " + UserQueueName);
            //https://beginnersbook.com/2014/01/how-to-write-to-file-in-java-using-bufferedwriter/
            File file = new File(UserQueueName);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file, true);
            BufferedWriter UserQueue = new BufferedWriter(fw);
            //BufferedWriter UserQueue = new BufferedWriter(new FileWriter(UserQueueName, true));
            System.out.println("Text from textbox: " + MessageSent);
            //Write to Queue
            String Content = GetTimeNow() + " " + FromUsername + ": " + MessageSent + "\n";
            UserQueue.write(Content);
            UserQueue.close();
            System.out.println("Successfully Wrote to user queue: " + Content);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    /**
     * @param username
     * @throws IOException //https://stackoverflow.com/questions/5600422/method-to-find-string-inside-of-the-text-file-then-getting-the-following-line
     *                     //https://stackoverflow.com/questions/3935791/find-and-replace-words-lines-in-a-file/3936452
     *                     //This code is used to delete the users who logged out from temp file, its invoked when user logs out
     */
    public void DeleteOnline(String username) throws IOException {
        Path path = Paths.get("online.txt");
        Charset charset = StandardCharsets.UTF_8;
        String content = Files.readString(path, charset);
        content = content.replaceAll(username, "NonEmptyLine");
        Files.write(path, content.getBytes(charset));
    }

    /**
     * This function is to read vector clock values  created for each  user namely A, B and C
     * This function  returns the a integer array for clock value [0,0,0]
     *
     * @param ClockRealName
     * @return
     */
    int[] ReadVectorClock(char ClockRealName) {
        String clockName = "./VectorClocks/" + ClockRealName + ".txt";
        //initializing the clock value
        // ClockValue[0] = A
        // ClockValue[1] = B  
        // ClockValue[2] = C
        int[] ClockValue = {0, 0, 0};

        //https://stackoverflow.com/questions/18838781/converting-string-array-to-an-integer-array
        try {
            BufferedReader in = new BufferedReader(
                    new FileReader(clockName));
            String str;
            String[] ClockTimeArray = new String[0];

            //read and split the file and 
            while ((str = in.readLine()) != null) {
                ClockTimeArray = str.split(",");
            }
            ClockValue[0] = Integer.parseInt(ClockTimeArray[0]);
            ClockValue[1] = Integer.parseInt(ClockTimeArray[1]);
            ClockValue[2] = Integer.parseInt(ClockTimeArray[2]);
            in.close();
        } catch (IOException e) {
            System.out.println("File Read Error");
        }
        //given the clock value back
        return ClockValue;
    }

    /**
     * This function is used to increment the current clock each time event occurs at the user.
     * This function  returns the a integer array for clock value [0,0,0]
     *
     * @param clockname is  the user name A,B,C one of these
     * @throws IOException
     */
    void VectorClockIncrement(char clockname) throws IOException {
        int[] ClockValue = ReadVectorClock(clockname);
        if (clockname == 'A')
            ClockValue[0] += 1;
        else if (clockname == 'B')
            ClockValue[1] += 1;
        else if (clockname == 'C')
            ClockValue[2] += 1;
        WriteVectorClock(clockname, ClockValue);
    }

    /**
     * This function is where  vector clock algorithm is implement
     *
     * @param clock1 where  the event is occuring
     * @param clock2 where the event is sent to
     * @throws IOException
     */
    void VectorClockAlgorithm(char clock1, char clock2) throws IOException {
        VectorClockIncrement(clock2);
        //where the event is occuring
        int[] ClockValue1 = ReadVectorClock(clock1);
        //where the event is sent to
        int[] ClockValue2 = ReadVectorClock(clock2);

        //check if this clock A so that only its value can be incremented otherwise
        if (clock1 == 'A') {
            ClockValue1[0] += 1;
            //if the value at clock2 is greater than at clock1 assign value of clock2
            if (ClockValue1[1] < ClockValue2[1]) {
                ClockValue1[1] = ClockValue2[1];

            } else if (ClockValue1[2] < ClockValue2[2]) {
                ClockValue1[2] = ClockValue2[2];
            }

        }

        //check if this clock B so that only its value can be incremented otherwise
        if (clock1 == 'B') {
            ClockValue1[1] += 1;
            //if the value at clock2 is greater than at clock1 assign value of clock2
            if (ClockValue1[0] < ClockValue2[0]) {
                ClockValue1[0] = ClockValue2[0];

            } else if (ClockValue1[2] < ClockValue2[2]) {
                ClockValue1[2] = ClockValue2[2];
            }

        }
        //check if this clock C so that only its value can be incremented otherwise
        if (clock1 == 'C') {
            ClockValue1[2] += 1;
            //if the value at clock2 is greater than at clock1 assign value of clock2
            if (ClockValue1[1] < ClockValue2[1]) {
                ClockValue1[1] = ClockValue2[1];

            } else if (ClockValue1[0] < ClockValue2[0]) {
                ClockValue1[0] = ClockValue2[0];
            }

        }
        //once done, write the value to the vector clock file
        WriteVectorClock(clock1, ClockValue1);

    }

    /***
     * This function is used to write data to the vector clock file, its paramenter descriptiopn is as given below
     * @param ClockRealName name of the clock to which we have to write to
     * @param ClockValue   data which u want to write to the file
     * @throws IOException
     */
    void WriteVectorClock(char ClockRealName, int[] ClockValue) throws IOException {
        //parse folder and write to the file given in parameter
        String ClockName = "./VectorClocks/" + ClockRealName + ".txt";
        FileWriter fw = new FileWriter(ClockName, false);
        BufferedWriter UserQueue = new BufferedWriter(fw);
        //content for Queue
        String Content = ClockValue[0] + "," + ClockValue[1] + "," + ClockValue[2];
        //write to queue
        UserQueue.write(Content);
        UserQueue.close();
    }

    /**
     * originally interded for prinitng the clock while testing my work, curently not in user.
     * Just for printing the vector clock
     *
     * @param clockname
     */
    void PrintClock(char clockname) {
        int[] ClockValue = ReadVectorClock(clockname);
        if (clockname == 'A')
            System.out.println("Clock" + clockname + ":" + Arrays.toString(ClockValue));
        else if (clockname == 'B')
            System.out.println("Clock" + clockname + ":" + Arrays.toString(ClockValue));
        else if (clockname == 'C')
            System.out.println("Clock" + clockname + ":" + Arrays.toString(ClockValue));
    }


    /***
     * This is the main client class
     */
    //https://github.com/SrihariShastry/socketProgramming/blob/master/src/lab1/Client.java
    public class Client {
        //Client class keep up socket and I/O  transfers with server
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

        /**
         * to get the client username
         *
         * @return
         */
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
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
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
                    /**
                     * This is task created for randomly pining and sending it vector clock to other two users
                     */
                    if (checkOnline("A") && checkOnline("B") && checkOnline("C")) {
                        TimerTask task = new TimerTask() {

                            @Override
                            public void run() {
                                System.out.println("Sent Vector Clock Randomly");
                                /**
                                 * here for each client A, B and C a random client will chosen and will send the vector  clock to it
                                 */
                                char random_clientA = new Random().nextBoolean() ? 'B' : 'C';
                                char random_clientB = new Random().nextBoolean() ? 'A' : 'C';
                                char random_clientC = new Random().nextBoolean() ? 'A' : 'B';

                                try {
                                    VectorClockAlgorithm('A', random_clientA);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    VectorClockAlgorithm('B', random_clientB);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    VectorClockAlgorithm('C', random_clientC);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                            }
                        };

                        Timer timer = new Timer();
                        /**
                         * the clock is set for a timer of 10seconds after which it will send the vector time clock
                         */

                        timer.schedule(task, new Date(), 10000);
                    }
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
        UsrTxt.setText("A/B/C");
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
        SendMsg.append("\n" + GetTimeNow() + ": " + value);
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
            ClientRqst[6] = "";

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
            ClientRqst[6] = "";
            client.sendMessage(ClientRqst);

            ClientRqst2[0] = "GET Clients HTTP/1.1";
            ClientRqst2[1] = "Host: localhost";
            ClientRqst2[2] = "User-Agent: " + client.getUsername();
            ClientRqst2[3] = "Content-Type: client-list";
            ClientRqst2[4] = "Content-Length =  0";
            ClientRqst2[5] = "\r\n";
            ClientRqst2[6] = "";
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

            System.out.println("Print value of ClientRqst[6].charAt(0): " + ClientRqst[6].charAt(0));
            /**
             * This where the vector clock is called so that the value can be parsed and usernames can be sent
             */

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

            /**
             * prinitng the vector clock after each reply to the user panel
             */
            String vectorName = client.getUsername();
            String toVector = to;


            try {
                VectorClockAlgorithm(to.charAt(0), ClientRqst[6].charAt(0));
            } catch (IOException e) {
                e.printStackTrace();
            }

            int[] ClockValue = ReadVectorClock(vectorName.charAt(0));
            int[] ClockValueTo = ReadVectorClock(toVector.charAt(0));

            if(ClockValueTo[0] < ClockValue[0])
                ClockValueTo[0] = ClockValue[0];

            if(ClockValueTo[1] < ClockValue[1])
                ClockValueTo[1] = ClockValue[1];

            if(ClockValueTo[2] < ClockValue[2])
                ClockValueTo[2] = ClockValue[2];

            //broadcast message type
            if (ClientRqst[2].contains("broadcast")) {
                ClientRqst[6] += " :Clock" + toVector.charAt(0) + ": " + Arrays.toString(ClockValueTo) + ": " + TxtMsgField.getText() + "\n";
                client.sendMessage(ClientRqst);
            } else {
                ClientRqst[6] += " :Clock" + toVector.charAt(0) + ": " + Arrays.toString(ClockValueTo) + ": " + TxtMsgField.getText() + "\n";
                client.sendMessage(ClientRqst);
                ClientRqst2[6] += " :Clock" + toVector.charAt(0) + ": " + Arrays.toString(ClockValueTo) + ": " + TxtMsgField.getText() + "\n";
                if (ClientRqst2[6] != ClientRqst[6])
                    client.sendMessage(ClientRqst2);
            }
            TxtMsgField.setText("");
            client.PrintMsg("Clock" + vectorName.charAt(0) + ":" + Arrays.toString(ClockValue));

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

                //https://stackoverflow.com/questions/5868369/how-to-read-a-large-text-file-line-by-line-using-java
                //Code to read from message queue line by line from the server

                File file = new File(QueueFile);
                if (file.length() == 0) {
                    MessageQueueing[6] += ": No messages." + '\n';
                    client.sendMessage(MessageQueueing);
                } else {
                    try (BufferedReader br = new BufferedReader(new FileReader(QueueFile))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            System.out.println("\nStarted Reading from queue");
                            System.out.println(line);
                            MessageQueueing[6] += ": " + line + '\n';
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
            }
        }
        /**
         */
        else if (ClientObj == ConnectBtn) {
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

    /***
     * This used to updated the current client online
     * @param response is the parmeter containing unicast, broadcast or multicast message
     */
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
