import java.io.FileOutputStream;
import java.io.IOException;

import com.xqbase.util.winrm.WinRmClient;
import com.xqbase.util.winrm.WinRmException;

public class TestWinRm {
	public static void main(String[] args) throws WinRmException, IOException {
		try (
			WinRmClient client = new WinRmClient("http://localhost:5985/wsman",
					"Administrator", "****", 10000);
			FileOutputStream out = new FileOutputStream("D:\\DIR.TXT");
		) {
			int exitCode = client.exec("DIR D:\\ /S", out, out);
			System.out.println(exitCode);
		}
	}
}