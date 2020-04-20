/*
Avinash Shanker
Roll No: 1001668570
ID: AXS8570
University of Texas, Arlington
Distributed Systems Lab3
Vector Clocks
*/

/* This Code starts the server GUI, run this code first before starting client. */

//imports
//References are included in the code

/**
 * All the imports are used here
 */

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.GroupLayout.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;


//Server GUI which implements ActionListener responsible for setting up server and communicate with client

/**
 * This is server UI action listener class used to track user commands from the GUI pane
 */
public class ServerUI implements ActionListener, WindowListener {
    //Variable and object declarations
    Server server;
    JFrame FrameServer;
    JTextArea ServerLog;
    JButton ServerBtnStart;
    JButton LogButton;
    JTextField PrtNoFrame;

    //Setting date for the sever logs and connections
    //https://www.javatpoint.com/java-get-current-date
    public String GetTimeNow() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }


    //Server GUI components like buttons textboxes like start and stop server are initialized here
    //This function also setup up the Server GUI frame
    public void initialize() {
        FrameServer = new JFrame();
        //setting server frame'RequestString window size
        FrameServer.setBounds(100, 100, 500, 500);
        FrameServer.setTitle("Server Console");
        FrameServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Textfield to enter port no
        PrtNoFrame = new JTextField();
        //set inline default value for port number
        PrtNoFrame.setText("1070");
        PrtNoFrame.setColumns(3);
        JLabel PortLabel = new JLabel(" Port Number");
        PortLabel.setForeground(Color.BLUE);
        //Set start button for server
        ServerBtnStart = new JButton("Start");
        ServerBtnStart.setForeground(Color.BLUE);

        //Creating a log button for the server pane
        LogButton = new JButton("Log");
        LogButton.setForeground(Color.BLUE);

        JScrollPane ServerLogPane = new JScrollPane();

        //Line above Text box created for displaying server log 
        JLabel LogLabel = new JLabel(" Live Log:");
        LogLabel.setForeground(Color.BLUE);
        //https://stackoverflow.com/questions/13021817/grouplayout-alignment-issue
        //https://github.com/SrihariShastry/socketProgramming/blob/master/src/lab1/ServerGUI.java
        //Server window pane layout horizontal settings
        GroupLayout ServerLayout = new GroupLayout(FrameServer.getContentPane());
        ServerLayout.setHorizontalGroup(
                ServerLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(ServerLayout.createSequentialGroup()
                                .addGroup(ServerLayout.createParallelGroup(Alignment.LEADING)
                                        .addComponent(ServerLogPane, GroupLayout.PREFERRED_SIZE, 490, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(LogLabel)
                                        .addGroup(ServerLayout.createSequentialGroup()
                                                .addComponent(PortLabel)
                                                .addComponent(PrtNoFrame, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(ServerBtnStart)
                                                .addComponent(LogButton))))
        );
        //Server window pane layout vertical settings
        ServerLayout.setVerticalGroup(
                ServerLayout.createParallelGroup(Alignment.LEADING)
                        .addGroup(ServerLayout.createSequentialGroup()
                                .addGroup(ServerLayout.createParallelGroup(Alignment.BASELINE)
                                        .addComponent(PrtNoFrame)
                                        .addComponent(PortLabel)
                                        .addComponent(ServerBtnStart)
                                        .addComponent(LogButton))
                                .addComponent(LogLabel)
                                .addComponent(ServerLogPane, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(300, Short.MAX_VALUE))
        );
        //Text box created for displaying server log 
        ServerLog = new JTextArea();
        ServerLogPane.setViewportView(ServerLog);
        FrameServer.getContentPane().setLayout(ServerLayout);
        ServerBtnStart.addActionListener(this);
        LogButton.addActionListener(this);
    }

    //https://www.w3schools.com/java/java_files_create.asp
    //delete the cache file generated
    public void deleteOnline() throws IOException {
        Path fileToDeletePath = Paths.get("online.txt");
        Files.delete(fileToDeletePath);
        //https://www.dummies.com/programming/java/how-to-write-java-code-to-delete-several-files-at-once/
        //This part of the code is to delete file once you stop the server
//        File folder = new File(".");
//        for (File file : folder.listFiles()) {
//            if (file.getName().endsWith(".txt")) {
//                file.delete();
//            }
//        }
    }

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
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            //handle this
        }
        return false;
    }

    //Message queueing for users
    public void WriteToUserQueue(String ToUsername, String MessageSent) {
        String UserQueueName = ToUsername + ".txt";
        try {
            System.out.println("Check file exist: " + UserQueueName);
            BufferedWriter UserQueue = new BufferedWriter(new FileWriter(UserQueueName, true));
            System.out.println("Text from textbox: " + MessageSent);
            //Write to Queue
            String Content = MessageSent + "\n";
            UserQueue.write(Content);
            UserQueue.close();
            System.out.println("Successfully Wrote to user queue: " + Content);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    //Message queueing for users
    //https://mkyong.com/java/how-to-write-to-file-in-java-bufferedwriter-example/
    public void WriteServerLog(String MessageSent) {
        String ServerLog = "Server.log";
        try {
            System.out.println("Check file exist: " + ServerLog);
            BufferedWriter PutServerLog = new BufferedWriter(new FileWriter(ServerLog, true));
            //Write to Queue
            String Content = GetTimeNow() + ": " + MessageSent + "\n";
            PutServerLog.write(Content);
            PutServerLog.close();
            System.out.println("Successfully wrote to server log: " + Content);
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }


    @Override
    public void actionPerformed(ActionEvent err) {
        Object ServerObj = err.getSource();

        boolean done = false;
        //Check if server is running and stop
        if (server != null) {
            server.StopServer();
            try {
                deleteOnline();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //setting to null as the server has not started yet
            server = null;
            PrtNoFrame.setEditable(true);
            //Server start button setting
            ServerBtnStart.setText("Start");

        } else {
            //Start server on port number entered by user
            int portNo = 0;
            try {
                String portVar = PrtNoFrame.getText().trim();
                portNo = Integer.parseInt(portVar);
            } catch (Exception error) {
                updateLog("Invalid portNo number");
                done = true;
            }
            //Start a new server
            if (!done) {
                //invoking server on the port number using start thread and listen till manually
                server = new Server(portNo, this);
                new ServerRunning().start();

                ServerBtnStart.setText("Stop");
                //Making port number non editable after server is set to start
                PrtNoFrame.setEditable(false);
            }
        }

    }

    //Set server log onto the console pane
    void updateLog(String message) {
        ServerLog.append(GetTimeNow() + ": " + message);
        WriteServerLog(message);
        System.out.println("Server log from updateLog: " + message);

        if (message.contains("connected now") || message.contains("logged out")) {
            //https://stackoverflow.com/questions/5868369/how-to-read-a-large-text-file-line-by-line-using-java
            //Code to read from message queue line by line from the server
            try (BufferedReader br = new BufferedReader(new FileReader("online.txt"))) {
                String line;
                ServerLog.append("--------------------------" + "\n");
                ServerLog.append("Online Users:" + "\n");
                ServerLog.append("--------------------------" + "\n");
                while ((line = br.readLine()) != null) {
                    if (!line.contains("NonEmptyLine")) {
                        if(!(line =="\n"))
                        ServerLog.append(line + "\n");
                    }
                }
                ServerLog.append("--------------------------" + "\n");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //implementing run() function
    class ServerRunning extends Thread {
        public void run() {
            //run server
            server.runServer();

            // Server start button
            ServerBtnStart.setText("Start");
            PrtNoFrame.setEditable(true);
            updateLog("Server connection interrupted/closed.\n");
            server = null;
        }
    }

    //This class implements setting up server sockets
    //https://github.com/SrihariShastry/socketProgramming/blob/master/src/lab1/Server.java
    public class Server {
        //setting server UI objects
        ServerUI ServerInterface;
        int portNo;

        //https://www.w3schools.com/java/java_files_create.asp
        //This functions is used to write logs of the use who have logged in
        public void writer(String username) {
            try {
                BufferedWriter userOnline = new BufferedWriter(new FileWriter("online.txt", true));
                userOnline.write(username);
                //Server creating a message queue cache file for each user
                username = username.replaceAll("\\s+","");
                String UserQueueName = username + ".txt";
                BufferedWriter UserMsgQueue = new BufferedWriter(new FileWriter(UserQueueName, true));
                UserMsgQueue.write("");
                userOnline.close();
                UserMsgQueue.close();
                //System.out.println("Successfully wrote to the file.");
            } catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }


        //ArrayList for clients
        public ArrayList<multiThread> listClient = new ArrayList<>();
        int IDclient;
        //SetServerTrue will keep server alive until its set to false
        public boolean SetServerTrue = true;


        public Server(int portNo, ServerUI ServerInterface) {
            //UI object for server interface
            this.ServerInterface = ServerInterface;
            //portNo on which the server will work
            this.portNo = portNo;
        }

        //continiously listen to the ServerSocket for connection requests from clients
        public void runServer() {
            try {
                // ServerSocket used by server
                ServerSocket serverSocket = new ServerSocket(portNo);
                //delete old log file if present and start new one
                //deleteOnline();
                //Write a line to keep the file non-empty upon start of server
                writer("NonEmptyLine");

                //Keep waiting for get request
                while (SetServerTrue) {
                    //Set alert that we are waiting for for clients on given port no
                    printConsole("Server started on port: " + portNo + "\n");
                    //accepting server connection
                    Socket ServerSocket = serverSocket.accept();

                    //Stop is the server is close break connection
                    if (!SetServerTrue) break;

                    //Craeating thread object
                    var multiThread = new multiThread(ServerSocket);
                    //add client to client list
                    listClient.add(multiThread);
                    //start the thread and listen to it coniniously
                    multiThread.start();
                }
                //If user hits stop server  button try block is called
                try {
                    //stop client server thread for opened sockets
                    //multiThread extends Thread
                    for (Server.multiThread multiThread : listClient)
                        try {
                            multiThread.iStream.close();
                            multiThread.oStream.close();
                            multiThread.ServerSocket.close();
                            //Delete the online.txt which contains the user currently logged in
                            deleteOnline();
                        } catch (IOException err) {
                            err.printStackTrace();
                        }
                    //close the server
                    serverSocket.close();
                } catch (Exception err) {
                    //catch errors in closing server
                    printConsole("Error in closing server" + err);
                }
            }
            //If any error occurs in server creation catch is called
            catch (IOException err) {
                String txt = "Error in ServerSocket creation" + err + "\n";
                printConsole(txt);
            }
        }

        //Printconsole is used to display log server console
        public void printConsole(String txt) {
            ServerInterface.ServerLog.append(GetTimeNow() + ": " + txt);
            //Write all server logs abouts connections and disconnections to a persistent log file
            WriteServerLog(txt);
            System.out.println("Printing server log for debug: " + txt);
        }

        //function to stop the server on the given port no
        public void StopServer() {
            //SetServerTrue is set to false when stop button is hit by user
            SetServerTrue = false;
            try {
                new Socket("localhost", portNo);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }

        class multiThread extends Thread {
            //setup sockets of server and client for input and output streams
            Socket ServerSocket;
            ObjectInputStream iStream;
            ObjectOutputStream oStream;
            //To give enique ID to each client
            int SetClientID;


            //var to store the client username
            String ClientUsername;

            //Setting constructor for multiThread class
            multiThread(Socket ServerSocket) {
                //increment ID so that we can remove clients when disconnecting
                SetClientID = ++IDclient;
                this.ServerSocket = ServerSocket;
                try {
                    //setting up I/O streams
                    oStream = new ObjectOutputStream(ServerSocket.getOutputStream());
                    iStream = new ObjectInputStream(ServerSocket.getInputStream());
                    //Read the user name entered by the user
                    ClientUsername = (String) iStream.readObject();
                    writer("\n" + ClientUsername);
                    //Show that connection is connected or not
                    printConsole(ClientUsername + " has connected now.");

                } catch (IOException err) {
                    //catch if unable to create I/O streams
                    printConsole("Exception: " + err);
                } catch (ClassNotFoundException err) {
                    err.printStackTrace();
                }
            }

            //get function to return username
            public String getUserName() {
                return ClientUsername;
            }

            //update console log 
            public void printConsole(String string) {
                ServerInterface.updateLog(string + "\n");
            }

            //Send response to client
            public boolean respond(String[] respose) {
                //check is link to server is still active
                if (!ServerSocket.isConnected()) {
                    close();
                    return false;
                }

                //success then write to log
                try {
                    oStream.writeObject(respose);
                } catch (Exception err) {
                    err.printStackTrace();
                }
                return true;
            }


            public void run() {
                //keep listening to client requests and set response and request accordingly
                while (true) {
                    //request and response object
                    String[] getRequest;
                    String[] getResponse = new String[7];
                    try {
                        //get request of client
                        getRequest = (String[]) iStream.readObject();

                        //Making up response messages
                        getResponse[0] = "HTTP/1.1 200 OK";
                        getResponse[1] = "server: localhost";
                        getResponse[2] = "Content-type: message";
                        getResponse[3] = "Date: " + GetTimeNow();
                        getResponse[4] = "Content-Length: " + getRequest[5].length();
                        getResponse[5] = "\r\n";
                        getResponse[6] = "";

                        //GET request set by client
                        if (getRequest[0].contains("GET")) {
                            getResponse[2] = "Content-type: client-list";
                            //put usernames to list and get reponse
                            listClient.forEach(ClientNames -> getResponse[6] += ClientNames.ClientUsername + ",");
                            //Add broadcast type by deafult in list
                            getResponse[6] += "BROADCAST";

                            //getting the HTTP get request
                            StringBuilder RequestHTTP = new StringBuilder();
                            for (String RequestString : getRequest) {
                                RequestHTTP.append(RequestString);
                            }
                            //shot HTTP get request
                            ServerInterface.updateLog(RequestHTTP.toString());
                            respond(getResponse);
                        }

                        // 1-all braodcast message
                        else if (getRequest[0].contains("POST") && (getRequest[2].contains("BROADCAST") || getRequest[2].contains("BROADCAST"))) {
                            //unparsed HTTP getRequest
                            StringBuilder RequestHTTP = new StringBuilder();
                            for (String RequestString : getRequest) {
                                RequestHTTP.append(RequestString);
                            }
                            //print unparsed HTTP getRequest on server log
                            ServerInterface.updateLog(RequestHTTP.toString());

                            //get the message to be sent inside the getResponse data
                            getResponse[6] = getRequest[6];
                            //If there is no client then remove
                            listClient.removeIf(ClientNames -> !ClientNames.respond(getResponse));

                        }

                        //if client sends a 1-1 message
                        //This includes logging to as persistent user queue if the user is not found online
                        else if (getRequest[0].contains("POST") && !(getRequest[2].contains("BROADCAST"))) {
                            //To check if the user is online or not. If offline, then write to message queue.
                            boolean notOnline;
                            notOnline = checkOnline(getRequest[2]);

                            //write to queue if user is offline and store persistently until back online
                            if (notOnline) {
                                System.out.println("Writing to the user queue.");
                                WriteToUserQueue(getRequest[2], getRequest[6]);
                            }

                            //unparsed HTTP getRequest
                            StringBuilder RequestHTTP = new StringBuilder();
                            for (String RequestString : getRequest) {
                                RequestHTTP.append(RequestString);
                            }
                            //print unparsed HTTP getRequest on server log
                            ServerInterface.updateLog(RequestHTTP.toString());
                            String userDest = getRequest[2].substring(11).trim();
                            getResponse[6] = getRequest[6];
                            //check client list for destination
                            for (multiThread ClientNames : listClient) {
                                if (ClientNames.getUserName().equalsIgnoreCase(userDest)) {
                                    ClientNames.respond(getResponse);
                                }
                            }
                        }
                        //logout request
                        else if (getRequest[0].contains("DELETE")) {
                            StringBuilder RequestHTTP = new StringBuilder();
                            for (String RequestString : getRequest) {
                                RequestHTTP.append(RequestString);
                            }
                            ServerInterface.updateLog(RequestHTTP.toString());
                            String[] LogoutUsr = getRequest[2].split(":");
                            String UserLoggedOut = RequestHTTP.toString();
                            UserLoggedOut = UserLoggedOut.substring(44, 49);
                            ServerInterface.updateLog(UserLoggedOut + " logged out\n");
                            listClient.removeIf(ClientNames -> LogoutUsr[1].trim().equalsIgnoreCase(ClientNames.getUserName()));
                        }
                    } catch (IOException err) {
                        //Reading exception in streams
                        printConsole(ClientUsername + " Exception reading Streams: " + err);
                        break;
                    } catch (ClassNotFoundException err) {
                        break;
                    }
                }
                //remove clients
                listClient.remove(SetClientID - 2);
                close();
            }

            // close I/O streams
            public void close() {
                try {
                    if (oStream != null) oStream.close();
                    if (iStream != null) iStream.close();
                    if (ServerSocket != null) ServerSocket.close();
                    ServerInterface.updateLog(ClientUsername + "Disconnected");
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }

        }
    }

    //main class to invoke ServerUI

    /**
     * This is main class of my program for server.
     * Multiple calls are done here.
     * @param args
     */
    public static void main(String[] args) {
        /*
        //https://www.w3schools.com/java/java_files_create.asp
        try {
            File userLog = new File("online.txt");
            if (userLog.createNewFile()) {
                System.out.println("File created: " + userLog.getName());
            } else {
                userLog.delete();
                System.out.println("File already exists.");
            }
        } catch (IOException err) {
            System.out.println("An error occurred.");
            err.printStackTrace();
        }*/

        EventQueue.invokeLater(() -> {
            try {
                ServerUI window = new ServerUI();
                //Make the Server Console visible
                window.FrameServer.setVisible(true);
            } catch (Exception err) {
                err.printStackTrace();
            }
        });

    }

    //Initialize the sever variables
    public ServerUI() {
        initialize();
    }

    //Override methods

    @Override
    public void windowOpened(WindowEvent err) {

    }

    @Override
    public void windowClosing(WindowEvent err) {

        if (server != null) {
            try {
                server.StopServer();
            } catch (Exception ignored) {
            }
            server = null;
        }
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent err) {

    }

    @Override
    public void windowIconified(WindowEvent err) {

    }

    @Override
    public void windowDeiconified(WindowEvent err) {

    }

    @Override
    public void windowActivated(WindowEvent err) {

    }

    @Override
    public void windowDeactivated(WindowEvent err) {

    }


}
