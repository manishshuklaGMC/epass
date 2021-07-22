package curfew.util;

import curfew.exception.CurfewPassException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This Java program demonstrates how to compress multiple files in ZIP format.
 *
 * @author www.codejava.net
 */
public class ZipFiles {

  public static void zipFiles(Set<String> filePaths, String zipFilePath) {
    try {
      FileOutputStream fos = new FileOutputStream(zipFilePath);
      ZipOutputStream zos = new ZipOutputStream(fos);

      for (String aFile : filePaths) {
        zos.putNextEntry(new ZipEntry(new File(aFile).getName()));

        byte[] bytes = Files.readAllBytes(Paths.get(aFile));
        zos.write(bytes, 0, bytes.length);
        zos.closeEntry();
      }
      zos.flush();
      zos.finish();
      zos.close();
      fos.close();

    } catch (IOException ex) {
      throw new CurfewPassException("Error in zipping file : " + ex.getLocalizedMessage());
    }
  }
}
