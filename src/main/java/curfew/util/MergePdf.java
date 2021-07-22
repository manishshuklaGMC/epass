package curfew.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MergePdf {
  public static void mergePDFs(List<String> pdfFiles, String outputFile) throws IOException, DocumentException {
    Document document = new Document();
    File mergedPdfFile = new File(outputFile);
    Files.deleteIfExists(mergedPdfFile.toPath());
    mergedPdfFile.createNewFile();
    PdfCopy copy = new PdfCopy(document, new FileOutputStream(mergedPdfFile));
    List<InputStream> list = new ArrayList<InputStream>();
    for (String pdfFile : pdfFiles) {
      list.add(new FileInputStream(new File(pdfFile)));
    }
    document.open();
    for (InputStream file : list) {
      PdfReader reader = new PdfReader(file);
      copy.addDocument(reader);
      copy.freeReader(reader);
      reader.close();
    }
    document.close();
  }
}
