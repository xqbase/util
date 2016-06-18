import java.io.IOException;
import java.net.Socket;

public class Shutdown {
	public static void main(String[] args) throws IOException {
		try (Socket socket = new Socket("localhost", 8005)) {
			socket.getOutputStream().write("SHUTDOWN".getBytes());
		}
	}
}