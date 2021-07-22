package curfew.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Created by manish.shukla on 2020/3/25. */
@RestController
public class RequestGenerator {
  public static final String UPLOADED_FOLDER = "~/";
  private OrganizationService organizationService;

  public RequestGenerator(OrganizationService organizationService) {
    this.organizationService = organizationService;
  }

  @RequestMapping("/")
  public String index() {
    return "hello";
  }

  @PostMapping("/upload") // //new annotation since 4.3
  public String singleFileUpload(
      @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

    if (file.isEmpty()) {
      redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
      return "redirect:uploadStatus";
    }

    try {

      // Get the file and save it somewhere
      byte[] bytes = file.getBytes();
      Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
      Files.write(path, bytes);

      redirectAttributes.addFlashAttribute(
          "message", "You successfully uploaded '" + file.getOriginalFilename() + "'");

    } catch (IOException e) {
      e.printStackTrace();
    }

    return "redirect:/uploadStatus";
  }
}
