package a2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

public class Terminal {
	
	public Terminal() {

	}
	
	public static String command(String command) {
		try {
			
			String output = null;
			
			ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
			
			Process process = pb.start();
			
			BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			
			
			int exitStatus = process.waitFor();
			
			if (exitStatus == 0) {
				String line;
				String text = "";
				while ((line = stdout.readLine()) != null) {
					text = text + "\n" + line;
				}
				output = text;
				
			} else {
				String line;
				while ((line = stderr.readLine()) != null) {
					output = line;
				}
			}
			
			if (output == null) {
				return "Output is null";
			}
			
			return output;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Nothing";
	}
	
//	public static void initialise() {
//		String command = "ruby -e \\\"$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)\\\"";
//		command(command);
//		command("brew install wikit");
//		command("brew install ffmpeg");
//	}
	
	}
