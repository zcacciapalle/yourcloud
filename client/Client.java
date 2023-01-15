package client;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

import shared.*;


public class Client {
    private Socket socket;
    private OutputStream ostream;
    private InputStream istream;
    private String workingDirectory;
    
    private void save(String[] command) throws IOException {

        if (command.length != 3) {
            System.out.println("Invalid args to save");
            return;
        }
        System.out.println("Saving file to server...");
        String path = command[1];
        String destination = command[2];

        ostream.write(Commands.SAVE.getNumber());
        
        byte[] temp = destination.getBytes(Charset.forName("UTF-8"));
        //System.out.println(new String(temp, Charset.forName("UTF-8")));
        Util.writeIntBe(temp.length, ostream);
        ostream.write(temp);

        File file = new File(path);
        assert file.isFile();

        Util.writeLongBe(file.length(), ostream);
        FileInputStream ifstream = new FileInputStream(file);
        while (true) {
            int b = ifstream.read();
            if (b < 0) {
                break;
            }
            ostream.write(b);
        }
        ifstream.close();
        ostream.flush();

        int errorCode = istream.read();
        if (errorCode == 0) {

        } else {
            System.err.println("an error occured: "+ errorCode);
        }
    }
    private void retrieve(String[] command) throws IOException {
        if (command.length != 3) {
            System.out.println("Invalid args to retrieve");
            return;
        }
        System.out.println("Requesting file from server...");
        String location = command[1];
        String destination = command[2];

        ostream.write(Commands.RETRIEVE.getNumber());

        byte[] temp = location.getBytes(Charset.forName("UTF-8"));
        Util.writeIntBe(temp.length, ostream);
        ostream.write(temp);

        ostream.flush();

        int errorCode = istream.read();
        if (errorCode == 0) {
            long length = Util.readLong(istream);
            File file = new File(destination);

            FileOutputStream ofstream = new FileOutputStream(file);
            for (long i = 0; i < length; ++i) {
                int t = istream.read();
                if (t < 0) {
                    System.exit(1);
                }
                ofstream.write(t);
            }
            ofstream.close();
            
        } else {
            System.err.println("an error occured: "+ errorCode);
        }
    }
    private void save_dir(String[] command) throws IOException {
        assert false;
    }
    private void retrieve_dir(String[] command) throws IOException {
        assert false;

        if (command.length != 3) {
            System.out.println("Invalid args to retrieve_dir");
            return;
        }
        System.out.println("Requesting directory from server...");
        String location = command[1];

        ostream.write(Commands.RETRIEVE_DIR.getNumber());

        byte[] temp = location.getBytes(Charset.forName("UTF-8"));
        Util.writeIntBe(temp.length, ostream);
        ostream.write(temp);

        ostream.flush();
    }
    private void mkdir(String[] command) throws IOException {
        if (command.length != 2) {
            System.out.println("Invalid args to mkdir");
            return;
        }
        System.out.println("Creating new directory on server...");
        String location = command[1];

        ostream.write(Commands.MKDIR.getNumber());
        byte[] temp = location.getBytes(Charset.forName("UTF-8"));
        Util.writeIntBe(temp.length, ostream);
        ostream.write(temp);

        ostream.flush();

        int errorCode = istream.read();
        if (errorCode == 0) {

        } else {
            System.err.println("error " + errorCode);
        }
    }
    private void ls(String[] command) throws IOException {
        if (command.length != 1) {
            System.out.println("Invalid args to ls");
            return;
        }
        ostream.write(Commands.LS.getNumber());
        ostream.flush();

        int errorCode = istream.read();
        if (errorCode == 0) {
            System.out.print(Util.readAscii(istream, Util.readInt(istream)));
        } else {
            System.err.println("error " + errorCode);
        }
    }
    private void cd(String[] command) throws IOException {
        if (command.length != 2) {
            System.out.println("Invalid args to cd");
            return;
        }
        ostream.write(Commands.CD.getNumber());

        byte[] temp = command[1].getBytes(Charset.forName("UTF-8"));
        Util.writeIntBe(temp.length, ostream);
        ostream.write(temp);

        ostream.flush();

        int errorCode = istream.read();
        if (errorCode == 0) {
            workingDirectory = (Util.readAscii(istream, Util.readInt(istream)));
        } else {
            System.err.println("error " + errorCode);
        }
    }

    public Client(String ip, int port) throws IOException {
        Scanner stdin = new Scanner(System.in);

        socket = new Socket(ip, port);
        ostream = socket.getOutputStream();
        istream = socket.getInputStream();
        workingDirectory = "/";

        while (true) {
            System.out.printf("YourCloud%s \u001b[1;31m>\u001b[0;0m ", workingDirectory);
            String[] command = stdin.nextLine().split(" ");

            if (command.length == 0) {
                continue;
            }

            if (command[0].equals("save")) {
                save(command);

            } else if (command[0].equals("retrieve")) {
                retrieve(command);

            } else if (command[0].equals("save_dir")) {
                save_dir(command);

            } else if (command[0].equals("retrieve_dir")) {
                retrieve_dir(command);

            } else if (command[0].equals("mkdir")) {
                mkdir(command);

            } else if (command[0].equals("ls")) {
                ls(command);

            } else if (command[0].equals("cd")) {
                cd(command);

            } else if (command[0].equals("exit")) {
                break;
            } else {
                System.out.println("\"" + command[0] + "\" is not a valid command");
            }
        }

        stdin.close();
        socket.close();
    }
    public static void main(String[] args) throws IOException {
        Scanner stdin = new Scanner(System.in);

        System.out.print("Enter IP Address of server: ");
        String ip = stdin.nextLine();
        System.out.print("Enter port on server: ");
        int port = Integer.parseInt(stdin.nextLine());

        new Client(ip, port);

        stdin.close();
        /*
            Syntax
                save [client_path] [server_destination]
                retrieve [server_path] [client_destination]
                save_dir [client_path] [server_destination]
                retrieve_dir [server_path] [client_destination]
                mkdir [name]
                ls
                cd [name]    NOTE: redirecting too far up should always force user into the root YourCloud directory

        */
        
    }
}
