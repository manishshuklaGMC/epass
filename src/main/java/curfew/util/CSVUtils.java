package curfew.util;

import curfew.model.Application;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.util.Charsets;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

/** Created by manish.shukla on 2020/3/26. */
@Service
public class CSVUtils {
  public static final Charset UTF_8 = Charsets.UTF_8;
  public static final String NEW_LINE_SEPARATOR = "\n";
  public static final String DELIMITER_COMMA = ",";
  private static final CSVFormat csvFileFormat =
      CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

  public CSVUtils() {}

  public List<Application> parseFile(InputStream inputStream) {
    try {
      CSVParser parser = new CSVParser(new InputStreamReader(inputStream, UTF_8), csvFileFormat);
      return parser.getRecords().stream().map(this::getApplication).collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException("Could not parse csv file.");
    }
  }

  public Application getApplication(CSVRecord csvRecord) {
    return Application.builder().purpose(csvRecord.get("purpose")).build();
  }
}
