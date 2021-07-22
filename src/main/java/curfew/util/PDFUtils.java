package curfew.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import curfew.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;

public class PDFUtils {
  private static final String PDF_FOLDER = "src/main/data";

  public static String getPDFFile(
      Application application, String qrCode, String organizationName, String titleImageFile)
      throws IOException, DocumentException, URISyntaxException {
    File dir = new File(PDF_FOLDER);
    if (!dir.exists()) dir.mkdirs();
    String filePath =
        PDF_FOLDER
            + "/"
            + application.getId()
            + "_"
            + application.getEntity().getProofId()
            + ".pdf";
    File file = new File(filePath);
    Files.deleteIfExists(file.toPath());
    Rectangle rectangle = new Rectangle(200f, 400f);
    Document document = new Document(rectangle);
    PdfWriter.getInstance(document, new FileOutputStream(filePath));

    BaseColor bgColor =
        (application.getApplicationType() == ApplicationType.person)
            ? new BaseColor(245, 245, 245)
            : new BaseColor(251, 250, 98);

    document.open();

    PdfPTable table = new PdfPTable(1);
    table.setWidthPercentage(100);

    // title image
    PdfPCell titleCell = getTitleImageCell(bgColor, titleImageFile);
    table.addCell(titleCell);

    // pass title
    PdfPCell passTypeCell = getPassTypeCell(application, bgColor);
    table.addCell(passTypeCell);

    // pass details
    PdfPCell detailsCell = getPassDetailsCell(application, organizationName, bgColor);
    table.addCell(detailsCell);

    // QR code cell
    PdfPCell qRCodeCell = getQRCodeCell(qrCode);
    table.addCell(qRCodeCell);

    document.add(table);
    document.close();
    return filePath;
  }

  private static PdfPCell getTitleImageCell(BaseColor bgColor, String titleImageFile)
      throws BadElementException, IOException {
    Image title_image = Image.getInstance(titleImageFile);
    title_image.setAlignment(Paragraph.ALIGN_CENTER);
    PdfPCell titleCell = new PdfPCell(title_image, true);
    titleCell.setPadding(5);
    titleCell.setBackgroundColor(bgColor);
    titleCell.setBorder(Rectangle.NO_BORDER);
    return titleCell;
  }

  private static PdfPCell getPassTypeCell(Application application, BaseColor bgColor) {
    PdfPCell passTypeCell = new PdfPCell();
    passTypeCell.setPadding(5);
    passTypeCell.setBackgroundColor(bgColor);
    passTypeCell.setBorder(Rectangle.NO_BORDER);
    Font passTypeFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 10, BaseColor.BLACK);
    String passTypeContent =
        (application.getApplicationType() == ApplicationType.person)
            ? "PERSON PASS"
            : "VEHICLE PASS";
    Chunk passTypeChunk = new Chunk(passTypeContent, passTypeFont);
    Paragraph passTypePara = new Paragraph(passTypeChunk);
    passTypePara.setAlignment(Paragraph.ALIGN_CENTER);
    passTypeCell.addElement(passTypePara);
    return passTypeCell;
  }

  private static PdfPCell getQRCodeCell(String qrCode) throws BadElementException, IOException {
    Image qRCodeImg = Image.getInstance(qrCode);
    qRCodeImg.setAlignment(Paragraph.ALIGN_JUSTIFIED_ALL);
    PdfPCell qRCodeCell = new PdfPCell(qRCodeImg, true);
    qRCodeCell.setPadding(10);
    qRCodeCell.setBackgroundColor(BaseColor.WHITE);
    qRCodeCell.setBorder(Rectangle.NO_BORDER);
    return qRCodeCell;
  }

  private static String getIDTypeTitle(ProofType proofType) {
    switch (proofType) {
      case AADHAR:
        return "AADHAR No";
      case RC:
        return "Reg No";
      case ORG:
        return "Employee ID";
      case DL:
        return "DL No";
      case PAN:
        return "PAN No";
      case PASSPORT:
        return "Passport No";
    }
    return "ID";
  }

  private static PdfPCell getPassDetailsCell(
      Application application, String organizationName, BaseColor bgColor) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    PdfPCell detailsCell = new PdfPCell();
    detailsCell.setPadding(5);
    detailsCell.setBackgroundColor(bgColor);
    detailsCell.setBorder(Rectangle.NO_BORDER);
    Font data_font = FontFactory.getFont(FontFactory.COURIER, 7, BaseColor.BLACK);

    String data_content = "";
    if (application
        .getEntity()
        .getClass()
        .getCanonicalName()
        .equals(Person.class.getCanonicalName())) {
      String idTitle = getIDTypeTitle(application.getEntity().getProofType());
      Person person = (Person) application.getEntity();
      data_content = "Name: " + person.getFirstName() + " " + person.getLastName() + "\n";
      data_content += "Mobile No: " + person.getPhoneNumber() + "\n";
      data_content += idTitle + ": " + person.getProofId() + "\n";
      data_content += "Organization: " + organizationName + "\n";
      if (application.getPurpose() != null) {
        data_content += "Purpose: " + application.getPurpose() + "\n";
      }
      if (application.getEndTime() != null) {
        data_content += "Expiry Date: " + dateFormat.format(application.getEndTime()) + "\n";
      }
    }

    if (application
        .getEntity()
        .getClass()
        .getCanonicalName()
        .equals(Vehicle.class.getCanonicalName())) {
      Vehicle vehicle = (Vehicle) application.getEntity();
      data_content = "Vehicle No: " + vehicle.getProofId() + "\n";
      data_content += "Reg No: " + vehicle.getRegistrationNumber() + "\n";
      data_content += "Organization: " + organizationName + "\n";
      if (application.getPurpose() != null) {
        data_content += "Purpose: " + application.getPurpose() + "\n";
      }
      if (application.getEndTime() != null) {
        data_content += "Expiry Date: " + dateFormat.format(application.getEndTime()) + "\n";
      }
    }
    data_content += "QR Code : " + application.getToken() + "\n";
    Chunk data_chunk = new Chunk(data_content, data_font);
    Paragraph data_para = new Paragraph(data_chunk);
    data_para.setAlignment(Paragraph.ALIGN_JUSTIFIED);

    detailsCell.addElement(data_para);
    return detailsCell;
  }

  //  public static void main(String[] args) throws IOException, DocumentException,
  // URISyntaxException {
  //    System.out.println(System.getProperty("user.dir"));
  //    Application application =
  // Application.builder().applicationType(ApplicationType.person).id(1L).purpose("essential
  // supplies").entity(new Person("123489012", ProofType.AADHAR, "Mayank", "Natani", "8297504324",
  // "11/07/1994")).endTime(System.currentTimeMillis()).token("abcdef").build();
  //    Application application1 =
  // Application.builder().applicationType(ApplicationType.vehicle).id(2L).purpose("medical
  // supplies").entity(new Vehicle("TS07 AB 1234", ProofType.RC, "RC 123456789",
  // "i20")).endTime(System.currentTimeMillis()).token("xyztuv").build();
  //    getPDFFile(application, "src/main/data/QR-Code-Standard.jpg", "SWIGGY",
  // "src/main/data/pass_title.png");
  //    getPDFFile(application1, "src/main/data/QR-Code-Standard.jpg", "SWIGGY",
  // "src/main/data/pass_title.png");
  //  }
}
