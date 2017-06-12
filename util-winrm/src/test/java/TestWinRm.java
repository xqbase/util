import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.xqbase.util.winrm.WinRmClient;
import com.xqbase.util.winrm.WinRmException;

public class TestWinRm {
	public static void main(String[] args) throws WinRmException, IOException {
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		try (
			WinRmClient client = new WinRmClient("http://localhost:5985/wsman",
					"Administrator", "****", 10000);
			PrintStream out = new PrintStream("C:\\DIR.TXT");
		) {
			int exitCode = client.exec("DIR C:\\ /S", stdout, stderr);
			System.out.println(exitCode);
			for (String s : stdout) {
				out.println(s);
			}
			for (String s : stderr) {
				out.println(s);
			}
		}
	}
}