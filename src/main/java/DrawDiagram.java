import java.io.*;
import java.net.*;

public class DrawDiagram{

    public static Boolean drawPNG(String grammar, String outPath) {

        try {

            String link = "https://yuml.me/diagram/plain/class/" + URLEncoder.encode(grammar, "UTF-8") + ".png";
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            if (connection.getResponseCode() != 200) {
                throw new RuntimeException(
                        "Failed : HTTP error code : " + connection.getResponseCode());
            }
            OutputStream outputStream = new FileOutputStream(new File(outPath));
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = connection.getInputStream().read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
            outputStream.close();
            connection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
