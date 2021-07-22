package curfew.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import curfew.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;

@Service
public class PDFUtils2 {
//  @Value("${pdfheader}")
//  private String filename;
//
//
//  @Value("${issuingAuthorityDisclaimer}")
//  private String issuingAuthorityDisclaimer;
//
//  @Value("${helplineFooter}")
//  private String helplineFooter;

  private final StatesDetail statesDetail;

  public PDFUtils2(StatesDetail statesDetail) {
    this.statesDetail = statesDetail;
  }

  private static Rectangle getPDFBorder(Boolean withQR) {
    Rectangle borderRectangle =
        (!withQR) ? new Rectangle(110f, 810f, 490f, 540f) : new Rectangle(110f, 810f, 490f, 250f);
    borderRectangle.setBorder(Rectangle.BOX);
    borderRectangle.setBorderWidth(2);
    borderRectangle.setBorderColor(new CMYKColor(255, 185, 0, 45));

    return borderRectangle;
  }

//  private static Image getPDFHeaderImage() throws IOException, BadElementException {
//    Image headerImage = Image.getInstance("classpath:/pass_header_image.jpg");
//    headerImage.scaleAbsolute(350f, 35);
//    headerImage.setAlignment(Element.ALIGN_MIDDLE);
//
//    return headerImage;
//  }

  private static PdfPTable getTitleTable(String token) throws DocumentException {
    PdfPTable titlePassNumberTable = new PdfPTable(3);
    titlePassNumberTable.setWidthPercentage(60); // Width 100%
    titlePassNumberTable.setSpacingBefore(10f); // Space before table
    titlePassNumberTable.setWidths(new float[] {100f, 160f, 100f});

    PdfPCell blankCell = new PdfPCell(new Paragraph(""));
    blankCell.setBorderColor(BaseColor.WHITE);
    blankCell.setHorizontalAlignment(Element.ALIGN_LEFT);
    blankCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    titlePassNumberTable.addCell(blankCell);

    Font titleFont =
        FontFactory.getFont(FontFactory.TIMES_BOLD, 16, Font.BOLD, new CMYKColor(0, 255, 255, 50));
    PdfPCell passTitle = new PdfPCell(new Paragraph("COVID e-Pass", titleFont));
    passTitle.setBorderColor(BaseColor.WHITE);
    passTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
    passTitle.setVerticalAlignment(Element.ALIGN_MIDDLE);
    titlePassNumberTable.addCell(passTitle);

    Font numberFont = FontFactory.getFont(FontFactory.TIMES, 12, Font.BOLD);
    PdfPCell passNumber = new PdfPCell(new Paragraph("No.: " + token, numberFont));
    passNumber.setBorderColor(BaseColor.WHITE);
    passNumber.setHorizontalAlignment(Element.ALIGN_RIGHT);
    passNumber.setVerticalAlignment(Element.ALIGN_MIDDLE);
    titlePassNumberTable.addCell(passNumber);

    return titlePassNumberTable;
  }

  private static PdfPTable getHolderInfoTable(Application application, String organizationName)
      throws DocumentException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    Person person = (Person) application.getEntity();

    PdfPTable issueeInfoTable = new PdfPTable(2); // 2 columns.
    issueeInfoTable.setWidthPercentage(60); // Width 100%

    float[] columnWidths = {190f, 190f};
    issueeInfoTable.setWidths(columnWidths);

    String[][] issueeData =
        new String[][] {
          {"Name of Company:", organizationName},
          {"Name of Pass Holder:", person.getFirstName() + " " + person.getLastName()},
          {"Associated with:", application.getPartnerCompany()},
          {"Mobile Number:", person.getPhoneNumber()},
          {"Nature of Work:", application.getPurpose()},
          {"Valid Till:", dateFormat.format(application.getEndTime())},
          {"Valid for Locations:", application.getValidLocations()}
        };

    for (String[] row : issueeData) {
      Font fieldsFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 9);
      PdfPCell fieldName = new PdfPCell(new Paragraph(row[0], fieldsFont));
      fieldName.setBorderColor(BaseColor.WHITE);
      fieldName.setHorizontalAlignment(Element.ALIGN_LEFT);
      fieldName.setVerticalAlignment(Element.ALIGN_TOP);

      Font infoFont =
          FontFactory.getFont(
              FontFactory.TIMES_BOLD, 10, Font.BOLD, new CMYKColor(255, 185, 0, 45));
      PdfPCell value = new PdfPCell(new Paragraph(row[1], infoFont));
      value.setBorderColor(BaseColor.WHITE);
      value.setPaddingLeft(5);
      value.setHorizontalAlignment(Element.ALIGN_LEFT);
      value.setVerticalAlignment(Element.ALIGN_TOP);

      issueeInfoTable.addCell(fieldName);
      issueeInfoTable.addCell(value);
    }

    return issueeInfoTable;
  }

  public PdfPTable getFooterTable(Boolean withQR, String qrCode, String token, StateConfig stateConfig)
      throws DocumentException, IOException {
    PdfPTable footerTable = new PdfPTable(1);
    footerTable.setWidthPercentage(60); // Width 100%
    footerTable.setSpacingBefore(10f); // Space before table
    footerTable.setWidths(new float[] {1f});

    Font footerFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 8);
    Font footerFontBold = FontFactory.getFont(FontFactory.TIMES_ITALIC, 8, Font.BOLD);
    Font footerFontBlue =
        FontFactory.getFont(FontFactory.TIMES_BOLD, 8, Font.BOLD, new CMYKColor(255, 185, 0, 45));
    Font footerIssuedBy = FontFactory.getFont(FontFactory.TIMES_BOLD, 7, Font.BOLD);

    // QR code cell
    if (withQR) {
      PdfPCell qRCodeCell = getQRCodeCell(qrCode);
      footerTable.addCell(qRCodeCell);
    }

    PdfPCell holderFooter =
        new PdfPCell(
            new Paragraph(
                "Holder of this pass/vehicle authorised for Covid-19 ESSENTIAL SUPPLY DUTY.",
                footerFont));
    holderFooter.setBorderColor(BaseColor.WHITE);
    holderFooter.setHorizontalAlignment(Element.ALIGN_CENTER);
    holderFooter.setVerticalAlignment(Element.ALIGN_MIDDLE);
    footerTable.addCell(holderFooter);

    PdfPCell verifyFooter =
        new PdfPCell(
            new Paragraph(
                "For verification, SMS \"VERIFY " + token + "\" to 9686454890.", footerFontBold));
    verifyFooter.setBorderColor(BaseColor.WHITE);
    verifyFooter.setHorizontalAlignment(Element.ALIGN_CENTER);
    verifyFooter.setVerticalAlignment(Element.ALIGN_MIDDLE);
    footerTable.addCell(verifyFooter);

    PdfPCell govtId =
        new PdfPCell(
            new Paragraph(
                "Pass valid only when accompanied with a valid Govt ID in the name of passholder.",
                footerFontBlue));
    govtId.setBorderColor(BaseColor.WHITE);
    govtId.setHorizontalAlignment(Element.ALIGN_CENTER);
    govtId.setVerticalAlignment(Element.ALIGN_MIDDLE);
    footerTable.addCell(govtId);

    PdfPCell issuedBy = new PdfPCell( new Paragraph(stateConfig.getIssuingAuthorityDisclaimer(), footerIssuedBy));
    issuedBy.setBorderColor(BaseColor.WHITE);
    issuedBy.setHorizontalAlignment(Element.ALIGN_CENTER);
    issuedBy.setVerticalAlignment(Element.ALIGN_MIDDLE);
    footerTable.addCell(issuedBy);

    if(stateConfig.getHelplineFooter()!=null) {
        PdfPCell helpLine = new PdfPCell( new Paragraph(stateConfig.getHelplineFooter(), footerIssuedBy));
        helpLine.setBorderColor(BaseColor.WHITE);
        helpLine.setHorizontalAlignment(Element.ALIGN_CENTER);
        helpLine.setVerticalAlignment(Element.ALIGN_MIDDLE);
        footerTable.addCell(helpLine);
    }

    return footerTable;
  }

  private static PdfPCell getQRCodeCell(String qrCode) throws BadElementException, IOException {
    Image qRCodeImg = Image.getInstance(qrCode);
    qRCodeImg.setAlignment(Paragraph.ALIGN_JUSTIFIED_ALL);
    qRCodeImg.scaleAbsolute(100f, 100f);
    PdfPCell qRCodeCell = new PdfPCell(qRCodeImg, true);
    qRCodeCell.setPadding(10);
    qRCodeCell.setBackgroundColor(BaseColor.WHITE);
    qRCodeCell.setBorder(Rectangle.NO_BORDER);
    return qRCodeCell;
  }

  public String getPDFFile(Application application, String qrCode, String organizationName, Integer stateID)
      throws IOException, DocumentException, URISyntaxException, FileNotFoundException {
    StateConfig stateConfig = statesDetail.getStatesDetailById().get(stateID).getStateConfig();
    Boolean withQR = false;
    String filePath = "./" + application.getEntity().getProofId() + ".pdf";
    if (application
        .getEntity()
        .getClass()
        .getCanonicalName()
        .equals(Person.class.getCanonicalName())) {
      Person p = (Person) application.getEntity();
      filePath = "./" + p.getPhoneNumber() + "_" + Utils.getRandomNumberString() + ".pdf";
    }
    Utils.deleteFileIfExists(filePath);

    Document document = new Document();

    PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
    document.open();

    //    Create Border
    Rectangle borderRectangle = getPDFBorder(withQR);
    document.add(borderRectangle);

    //    Header Image
    Image headerImage = Image.getInstance(stateConfig.getPassTitleImageFileURL());
    headerImage.scalePercent(30);
    headerImage.setAlignment(Element.ALIGN_MIDDLE);
    document.add(headerImage);

    //    ::::::::  Pass Title and Number :::::::::;
    PdfPTable titlePassNumberTable = getTitleTable(application.getToken());
    document.add(titlePassNumberTable);

    //       ::::::: Issuee information table :::::::
    PdfPTable issueeInfoTable = getHolderInfoTable(application, organizationName);
    document.add(issueeInfoTable);

    //      ::::: Pass Footers ::::::
    PdfPTable footerTable = getFooterTable(withQR, qrCode, application.getToken(), stateConfig);
    document.add(footerTable);

    // Set attributes here
    document.addAuthor("");
    document.addCreationDate();
    document.addCreator("Epass");
    document.addTitle("covid pass");
    document.addSubject("covid pass");

    document.close();
    writer.close();

    return filePath;
  }

  //  public static void main(String[] args) throws IOException, DocumentException,
  // URISyntaxException {
  //    System.out.println(System.getProperty("user.dir"));
  //    Application application = Application.builder().applicationType(ApplicationType.person)
  //        .applicationStatus(ApplicationStatus.accepted)
  //        .startTime(System.currentTimeMillis())
  //        .endTime(System.currentTimeMillis())
  //        .issuerId(10)
  //        .token("AB23J7")
  //        .orderID("orderID")
  //        .purpose("Distribution")
  //        .entity(new Person("12345", ProofType.AADHAR, "Vibhav", "Srivastava", "8297504324",
  // "110/07/1994"))
  //        .partnerCompany("Partner pvt. ltd.")
  //        .validLocations("AAA, BBB, CCCCC")
  //        .build();
  //
  //    PDFUtils2 pdfUtils = new PDFUtils2();
  //    pdfUtils.getPDFFile(application, "sampleQR.png", "Hindustan Unilever Limited");
  //  }
}
