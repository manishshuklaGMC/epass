package curfew.controller;

import com.google.gson.Gson;
import curfew.controller.request.VerifyTokenRequest;
import curfew.controller.response.VerifyTokenResponse;
import curfew.dao.ApplicationDAO;
import curfew.exception.CurfewPassException;
import curfew.model.Application;
import curfew.service.RedisService;
import curfew.service.SignatureService;
import curfew.util.QRCodeUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.SignatureException;
import java.util.Date;

/** Created by manish.shukla on 2020/3/26. */
@RestController
public class TokenController {
  private final ApplicationDAO applicationDAO;
  private final QRCodeUtil qrCodeUtil;
  private final RedisService redisService;
  private final SignatureService signatureService;

  public TokenController(
      ApplicationDAO applicationDAO,
      RedisService redisService,
      QRCodeUtil qrCodeUtil,
      SignatureService signatureService) {
    this.applicationDAO = applicationDAO;
    this.redisService = redisService;
    this.qrCodeUtil = qrCodeUtil;
    this.signatureService = signatureService;
  }

  @RequestMapping(
    value = "/verifyToken",
    method = RequestMethod.POST,
    consumes = {"application/JSON"}
  )
  public VerifyTokenResponse verifyToken(@RequestBody VerifyTokenRequest request) {
    String cachedResponse = redisService.getKey(request.getToken());
    if (cachedResponse != null) {
      // removing caching for now
      //      return new Gson().fromJson(cachedResponse, VerifyTokenResponse.class);
    }
    try {
      String token = qrCodeUtil.decodeJWT(request.getToken()).getToken();
      VerifyTokenResponse res = verifyTokenFromDB(token);
      redisService.setKey(request.getToken(), new Gson().toJson(res));
      return res;

    } catch (Exception e) {
      try {
        return verifyTokenFromDB(request.getToken());
      } catch (Exception ex) {
        throw new CurfewPassException("Invalid token" + ex.getLocalizedMessage());
      }
    }
  }

  private VerifyTokenResponse verifyTokenFromDB(String token) throws SignatureException {
    Application application =
        applicationDAO.getApplicationByToken(token).toCompletableFuture().join();
    String signPayload = new Gson().toJson(application);
    String sign = signatureService.sign(signPayload);
    if (application.getEndTime() != null && application.getEndTime() < new Date().getTime()) {
      throw new CurfewPassException("pass expired");
    }
    return VerifyTokenResponse.builder()
        .applicationID(application.getId())
        .applicationStatus(application.getApplicationStatus())
        .startTime(application.getStartTime())
        .endTime(application.getEndTime())
        .createdAt(application.getCreatedAt())
        .issuerId(application.getIssuerId())
        .purpose(application.getPurpose())
        .toPlace(application.getToPlace())
        .fromPlace(application.getFromPlace())
        .token(application.getToken())
        .applicationType(application.getApplicationType())
        .entity(application.getEntity())
        .validLocations(application.getValidLocations())
        .signature(sign)
        .build();
  }
}
