package curfew.util;

import curfew.exception.CurfewPassException;
import org.apache.commons.lang.RandomStringUtils;
import org.bouncycastle.util.Strings;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class Utils {
  public static String getRandomNumberString() {
    // It will generate 6 digit random Number.
    // from 0 to 999999
    Random rnd = new Random();
    int number = rnd.nextInt(999999);

    // this will convert any number sequence into 6 character.
    return String.format("%06d", number);
  }

  public static String getRandomString(int len) {
    return Strings.toLowerCase(RandomStringUtils.randomAlphanumeric(len));
  }

  public static String getRandomSessionsString() {
    int leftLimit = 97; // letter 'a'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 128;
    Random random = new Random();

    String generatedString =
        random
            .ints(leftLimit, rightLimit + 1)
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();

    return generatedString;
  }

  public static Date addDays(Date date, int days) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    c.add(Calendar.DAY_OF_MONTH, days);
    return c.getTime();
  }

  public static List<String> getFileLines(MultipartFile file) {
    BufferedReader br;
    List<String> result = new ArrayList<>();
    try {

      String line;
      InputStream is = file.getInputStream();
      br = new BufferedReader(new InputStreamReader(is));
      while ((line = br.readLine()) != null) {
        result.add(line);
      }

    } catch (IOException e) {
      throw new CurfewPassException("Error reading file lines: " + e.getLocalizedMessage());
    }
    return result;
  }

  public static void deleteFileIfExists(String path) throws IOException {
    File file = new File(path);
    Files.deleteIfExists(file.toPath());
  }

  public static boolean isValid(String email) {
    String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
    return email.matches(regex);
  }
}
