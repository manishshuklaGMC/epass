package curfew.util;

import curfew.exception.CurfewPassException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterCheckUtil {
  private static final Long DAY_IN_MILLIS = 60L * 1000 * 60 * 24 - 1;

  public static String cleanName(String name) {
    name = name.trim().replaceAll("[^a-zA-Z ]", "");
    return name;
  }

  public static String cleanMobileNumber(String number) {
    number = number.trim().replaceAll("[^0-9]", "");
    if (number.length() >= 10) {
      number = "+91" + number.substring(number.length() - 10);
    } else {
      throw new CurfewPassException("incorrect mobile number");
    }
    return number;
  }

  public static long getDate(String date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    long validTill = 0;
    try {
      validTill = dateFormat.parse(date).getTime() + DAY_IN_MILLIS;
      // to make sure he gets till the end of the day printed on his pass.
    } catch (ParseException e) {
      // will never happen
      throw new CurfewPassException("Date parsing failed");
    }
    return validTill;
  }

  public static String validateEmail(String email) {
    Pattern pattern =
        Pattern.compile(
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    Matcher matcher = pattern.matcher(email);
    if (matcher.matches()) {
      return email;
    } else {
      throw new CurfewPassException("invalud email");
    }
  }

  public static String checkEmptyOrNull(String value) {
    if (value == null && value.isEmpty()) {
      throw new CurfewPassException("value is null or empty");
    }
    return value;
  }

  public static String checkLength(String value, Integer length) {
    if (value.length() > length) {
      throw new CurfewPassException(
          String.format("value is too long, size should be smaller than %d chars.", length));
    }
    return value;
  }
}
