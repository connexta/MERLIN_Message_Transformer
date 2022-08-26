package mil.dia.swe;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public interface SWETransform {

  public static void main(String args[]) {
    if (args != null && args.length > 0)  {
      System.out.println(insertSensorToJSON(new File(args[0])));
    } else {
      System.out.println("Usage: SWETransform <filename>");
    }
  }

  public static String insertSensorToJSON(String xml) throws IllegalArgumentException {
    if (xml == null || xml.length() == 0) {
      throw new IllegalArgumentException("Input string cannot be null or empty");
    }

    InputStream is = new ByteArrayInputStream(xml.getBytes());
    String json = InsertSensorTransformer.insertSensorToJSON(is);
    return json;
  }

  public static String insertSensorToJSON(File xmlFile) throws IllegalArgumentException {
    BufferedInputStream bis;
    try {
      bis = new BufferedInputStream(new FileInputStream(xmlFile));
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("File not found.", e);
    }
    return insertSensorToJSON((InputStream) bis);
  }

  public static String insertSensorToJSON(InputStream is) throws IllegalArgumentException {
    return InsertSensorTransformer.insertSensorToJSON(is);
  }
}
