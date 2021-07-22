package curfew;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.sendgrid.SendGrid;
import curfew.exception.CurfewPassException;
import curfew.model.*;
import curfew.util.JDBCTemplateWrapper;
import org.apache.tomcat.util.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.*;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@EnableScheduling
@SpringBootApplication
@PropertySource("classpath:/config/application-${property.environment}.properties")
public class Application extends SpringBootServletInitializer {

  @Value("${spring.sendgrid.api-key}")
  private String sendgridApiKey;

  @Value("${keyPair}")
  private Resource keyPair;

  @Value("${signaturePrivateKeyname}")
  private String privateKeyFileName;

  @Value("${signaturePublicKeyname}")
  private String publicKeyFileName;

  @Autowired
  protected JDBCTemplateWrapper jdbcTemplateWrapper;

  public static void main(String[] args) throws Exception {
    System.getProperties().put("server.port", 8080);
    System.getProperties().put("server.hostname", "0.0.0.0");
    Security.addProvider(new BouncyCastleProvider());
    SpringApplication.run(Application.class, args);
  }

  private static String getKey(String filename) throws IOException {
    // Read key from file
    StringBuilder strKeyPEM = new StringBuilder();
    BufferedReader br = new BufferedReader(new FileReader(filename));
    String line;
    while ((line = br.readLine()) != null) {
      strKeyPEM.append(line).append("\n");
    }
    br.close();
    return strKeyPEM.toString();
  }

  public static PrivateKey getPrivateKey(String filename)
      throws IOException, GeneralSecurityException {
    String privateKeyPEM = getKey(filename);
    return getPrivateKeyFromString(privateKeyPEM);
  }

  private static PrivateKey getPrivateKeyFromString(String key)
      throws IOException, GeneralSecurityException {
    String privateKeyPEM = key;
    privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
    privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
    byte[] encoded = Base64.decodeBase64(privateKeyPEM);
    KeyFactory kf = KeyFactory.getInstance("DSA");
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    DSAPrivateKey privKey = (DSAPrivateKey) kf.generatePrivate(keySpec);
    return privKey;
  }

  public static PublicKey getPublicKey(String filename)
      throws IOException, GeneralSecurityException {
    String publicKeyPEM = getKey(filename);
    return getPublicKeyFromString(publicKeyPEM);
  }

  private static PublicKey getPublicKeyFromString(String key)
      throws IOException, GeneralSecurityException {
    String publicKeyPEM = key;
    publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
    publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
    byte[] encoded = Base64.decodeBase64(publicKeyPEM);
    KeyFactory kf = KeyFactory.getInstance("DSA");
    DSAPublicKey pubKey = (DSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));
    return pubKey;
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(Application.class);
  }

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins("*");
      }
    };
  }

  @Bean
  public SendGrid sendGrid() {
    return new SendGrid(sendgridApiKey);
  }

  @Bean
  public KeyPair keyPair() {
    try {
      ObjectInputStream in = new ObjectInputStream(keyPair.getInputStream());
      final KeyPair result = (KeyPair) in.readObject();
      in.close();
      return result;
    } catch (final Throwable e) {
      throw new CurfewPassException("Unable to load KeyPair object", e);
    }
  }

  @Bean
  public SignatureKeyPair getSignatureKeyPair() {
    try {
      return new SignatureKeyPair(
          getPublicKey(publicKeyFileName), getPrivateKey(privateKeyFileName));
    } catch (final Throwable e) {
      throw new CurfewPassException("Unable to load SignatureKeyPair object", e);
    }
  }

  private State getState(ResultSet resultSet) throws SQLException {
    return State.builder()
        .id(resultSet.getInt("id"))
        .stateName(StateName.valueOf(resultSet.getString("name")))
        .stateConfig(new Gson().fromJson(resultSet.getString("config"), StateConfig.class))
        .build();
  }

  @Bean
  public StatesDetail getStateDetails() {
    List<State> stateList =
    jdbcTemplateWrapper.query("select * from state", (resultSet, i) -> getState(resultSet),
        ImmutableMap.of()).toCompletableFuture().join();
    Map<StateName, State> stateDetails = stateList
        .stream().collect(Collectors.toMap(State::getStateName, Function.identity()));


    Map<Integer, State> statesDetailById = stateList
        .stream().collect(Collectors.toMap(State::getId, Function.identity()));
    return new StatesDetail(stateDetails, statesDetailById);
  }

  @Bean
  public CommonsRequestLoggingFilter logFilter() {
    CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();

    filter.setIncludeQueryString(true);
    filter.setIncludePayload(true);
    filter.setMaxPayloadLength(10000);
    filter.setIncludeHeaders(false);
    filter.setAfterMessagePrefix("REQUEST DATA : ");

    return filter;
  }

  @Configuration
  @PropertySource("classpath:/config/application-${property.environment}.properties")
  public static class PropertyConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
      PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer =
          new PropertySourcesPlaceholderConfigurer();
      return propertySourcesPlaceholderConfigurer;
    }
  }
}
