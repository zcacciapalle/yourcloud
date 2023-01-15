package server;


import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a handler for requests sent by a client
 */
public interface RequestHandler
{
    /**
     * Called when the client sends a request with the binary data the client sent
     * @return The response data
     */
	public void onRequest(InputStream istream, OutputStream ostream, Server s);
}