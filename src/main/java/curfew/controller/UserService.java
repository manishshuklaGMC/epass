package curfew.controller;

import curfew.controller.request.PutUserProfile;
import curfew.dao.UserDAO;
import curfew.model.User;
import curfew.service.KeyStoreService;
import curfew.util.AESEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private UserDAO userDAO;

  @Autowired private KeyStoreService keyStoreService;

  @Value("${encryptionSecret}")
  private String encryptionSecret;

  @Autowired
  public UserService(UserDAO userDAO) {
    this.userDAO = userDAO;
  }

  public Void PutUserProfile(PutUserProfile userProfile) {
    return userDAO
        .createUser(
            User.builder()
                .city(userProfile.getCity())
                .createdAt(System.currentTimeMillis())
                .DOB(userProfile.getDob())
                .firstName(userProfile.getName().getFirstName())
                .lastName(userProfile.getName().getLastName())
                .gender(userProfile.getGender())
                .phoneNumber(userProfile.getMobileNumber())
                .proofId(userProfile.getGovtIDNumber())
                .profession(userProfile.getProfession())
                .proofType(userProfile.getGovtIDType())
                .build())
        .toCompletableFuture()
        .join();
  }

  String getEncryptedSigningKey(Long userId) {
    String remotePublicKey = userDAO.getPublicKeyForUser(userId);
    String sharedKey = keyStoreService.generateSharedKey(remotePublicKey);
    return AESEncryptor.encrypt(sharedKey, encryptionSecret);
  }
}
