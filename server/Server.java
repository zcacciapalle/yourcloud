package server;
import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import shared.Commands;
import shared.Util;

public class Server {
    private Thread listener;
	private boolean softStop;
	private RequestHandler handler;
	private String workingDirectory;
	
	public Server(String root, int port, RequestHandler handler) {
		workingDirectory = "";
		this.handler = handler;

		listener = new Thread() {
			public void run() {
				try {
					ServerSocket server = new ServerSocket(port);

					while (!softStop) {
						Socket conn = server.accept();
						System.out.println("Client connected");

						new Thread() {
							public void run() {
								try {
									InputStream istream = conn.getInputStream();
									OutputStream ostream = conn.getOutputStream();

									while (true) {
										handler.onRequest(istream, ostream, Server.this);
									}
								} catch (IOException e) {
									System.exit(1);
								}
							}
						}.start();
					}
					server.close();
				}
				catch (IOException e) {
					System.out.println("IOException: " + e);
				}
			}
		};
	}
	public boolean start() {
		boolean s = handler != null;
		if (s) {
			listener.setDaemon(false);
			listener.start();
		}
		return s;
	}
	public void stop() {
		softStop=true;
	}
	private static String evaluatePath(String wd, String path) {
		System.out.println("wd: "+wd);
		String[] split = (wd+path).split("/");
		ArrayList<String> list = new ArrayList<String>();

		for (String e : split) {
			if (e.isEmpty()) {
				continue;
			}
			if (e.equals("..")) {
				if (list.size() > 0) {
					list.remove(list.size()-1);
				}
			} else if (e.equals(".")) {
				continue;
			} else {
				list.add(e);
			}
		}
		String absolutePath = "/";
		for (String e : list) {
			absolutePath += e + "/";
		}
		return absolutePath;
	}
	public static void main(String[] args) {
		Scanner stdin = new Scanner(System.in);

		System.out.println("pwd: " + System.getProperty("user.dir"));
		System.out.print("Enter storage directory : ");
		String root = new File(stdin.nextLine()).getAbsolutePath();
		System.out.println("storage directory: " + root);

		System.out.print("Enter port: ");
        int port = Integer.parseInt(stdin.nextLine());

		Server server = new Server(root, port, new RequestHandler() {
			/* Format for REQUESTS

				command: byte
				if command == SAVE:
					destinationLength: byte[4] (int)
					saveDestination: byte[destinationLength]

					contentLength: byte[8] (int)
					fileContent: byte[contentLength]

				if command == RETRIEVE:
					locationLength: byte[4] (int)
					location: byte[locationLength]

				if command == SAVE_DIR:
					

				if command == RETRIEVE_DIR:
					dirLength: byte[4] (int)
					dir: byte[dirLength]
					
				if command == MKDIR:
					dirNameLength: byte[4] (int)
					dirName: byte[dirNameLength]

				if command == LS:
					no extra data
				
				if command == CD:
					dirLength: byte[4] (int)
					dir: byte[dirLength]
					
			*/
			/* Format for RESPONSES
				First byte of response is always error (0x00 for success)
				if command is save:
					fileLength: byte[8] (long)
				if command is 
			*/
			public void onRequest(InputStream istream, OutputStream ostream, Server s)
			{
				try
				{
					int command = istream.read();
					System.out.println("Recieved command " + command);

					if (command == -1) {
						System.exit(0);
					}
				
					if (command == Commands.SAVE.getNumber())
					{
						int destinationLength=Util.readInt(istream);
						String relPath=Util.readAscii(istream, destinationLength);

						long contentLength=Util.readLong(istream);
						String path=root+evaluatePath(s.workingDirectory, relPath);

						FileOutputStream ofstream = new FileOutputStream(new File(path));

						System.out.println(contentLength);
						for (long i=0; i < contentLength; i++)
						{
							int read=istream.read();
							if (read < 0) {
								System.exit(1);
							}
							ofstream.write(read);
						}
						ofstream.flush();
						ofstream.close();
						ostream.write(0);
					}
					else if (command == Commands.RETRIEVE.getNumber())
					{
						int locationLength=Util.readInt(istream);
						String relPath=Util.readAscii(istream, locationLength);
						File path = new File(root+evaluatePath(s.workingDirectory, relPath));
						if (path.isFile())
						{
							ostream.write(0);
							Util.writeLongBe(path.length(), ostream);

							FileInputStream ifstream = new FileInputStream(path);
							int read=ifstream.read();
							while(read > 0)
							{
								ostream.write(read);
								read=ifstream.read();
							}
							ifstream.close();
						}
						else ostream.write(1);
					}
					else if (command == Commands.SAVE_DIR.getNumber())
					{
						
					}
					else if (command == Commands.RETRIEVE_DIR.getNumber())
					{
						
					}
					else if (command == Commands.MKDIR.getNumber())
					{
						int len = Util.readInt(istream);
						String path = root + evaluatePath(s.workingDirectory, Util.readAscii(istream, len));
						new File(path).mkdir();

						ostream.write(0);
					}
					else if (command == Commands.LS.getNumber())
					{
						File path = new File(root+"/"+s.workingDirectory);
						File[] children=path.listFiles();
						String out="";
						for (File child : children)
							out+=child.getName() + "\n";

						ostream.write(0);
						byte[] temp = Util.strBytes(out);
						Util.writeIntBe(temp.length, ostream);
						ostream.write(temp);
					}
					else if (command == Commands.CD.getNumber())
					{
						int dirLength=Util.readInt(istream);
						String dir=Util.readAscii(istream, dirLength);

						String newWD = evaluatePath(s.workingDirectory, dir);
						File file = new File(root + newWD);

						if (file.isDirectory()) {
							s.workingDirectory=newWD;
							ostream.write(0);
							
							byte[] temp = newWD.getBytes(Charset.forName("UTF-8"));
							Util.writeIntBe(temp.length, ostream);
							ostream.write(temp);

						} else if (file.exists()) {
							ostream.write(1);

						} else {
							ostream.write(2);
						
						}
						//System.out.println("Working directory: "+workingDirectory);
					}
					ostream.flush();
					System.out.println("Handled command " + command);
				}
				catch (IOException e) {
					System.exit(1);
				}
			}
		});
		System.out.println("Waiting for connections");
		server.start();
		stdin.close();
	}
}
