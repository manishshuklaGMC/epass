package curfew.util;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import curfew.exception.CurfewPassException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
public class S3Util {
  public static final Long FIFTEEN_MINUTES_IN_MILLIS = Long.valueOf(900000L);
  protected final AWSStaticCredentialsProvider credentials;
  private final AmazonS3 s3Client;
  private final String bucket;

  public S3Util(
      @Value("${aws.s3.accessKeyId}") String accessKeyId,
      @Value("${aws.s3.secretAccessKey}") String secretAccessKey,
      @Value("${aws.s3.region}") String region,
      @Value("${aws.s3.bucket}") String bucket) {
    this.credentials =
        new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretAccessKey));
    this.bucket = bucket;
    this.s3Client =
        AmazonS3ClientBuilder.standard().withCredentials(credentials).withRegion(region).build();
  }

  public URL uploadAndGetSignedURLSync(
      InputStream inputStream, String contentType, Date expiryDate, String key) {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType);
    return supplyAsync(
            () -> {
              s3Client.putObject(bucket, key, inputStream, metadata);
              GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key);
              request.setExpiration(expiryDate);
              return s3Client.generatePresignedUrl(request);
            })
        .toCompletableFuture()
        .join();
  }

  public URL uploadDocumentSync(
      InputStream inputStream, String contentType, Date expiryDate, String key) {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType);
    return supplyAsync(
            () -> {
              s3Client.putObject(bucket, key, inputStream, metadata);
              return s3Client.getUrl(this.bucket, key);
            })
        .toCompletableFuture()
        .join();
  }

  public CompletionStage<URL> getSignedURL(String key) {
    return supplyAsync(
        () -> {
          GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key);
          request.setExpiration(new Date(System.currentTimeMillis() + FIFTEEN_MINUTES_IN_MILLIS));
          return s3Client.generatePresignedUrl(request);
        });
  }

  public CompletionStage<InputStream> getFileStream(String key) {
    return getFileStream(bucket, key);
  }

  public CompletionStage<InputStream> getFileStream(String bucket, String key) {
    try {
      GetObjectRequest getObjectRequest = new GetObjectRequest(bucket, key);
      return supplyAsync(() -> s3Client.getObject(getObjectRequest).getObjectContent());

    } catch (Exception e) {
      throw new CurfewPassException(
          "Unable to fetch file from s3 reason: " + e.getLocalizedMessage());
    }
  }
}
