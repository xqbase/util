import java.util.ArrayList;
import java.util.List;

import com.xqbase.util.winrm.WinRmClient;
import com.xqbase.util.winrm.WinRmException;

public class TestWinRm {
	public static void main(String[] args) throws WinRmException {
		List<String> stdout = new ArrayList<>();
		List<String> stderr = new ArrayList<>();
		try (WinRmClient client = new WinRmClient("http://localhost:5985/wsman",
				"Administrator", "****", 10000)) {
			client.exec("DIR C:\\ /S", stdout, stderr);
			for (String s : stdout) {
				System.out.println(s);
			}
			for (String s : stderr) {
				System.err.println(s);
			}
		}
	}
}