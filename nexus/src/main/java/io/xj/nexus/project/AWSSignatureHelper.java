package io.xj.nexus.project;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class AWSSignatureHelper {
  private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
  private static final String AWS_SECRET_KEY = "YOUR_AWS_SECRET_KEY";
  private static final String REGION = "YOUR_REGION";
  private static final String SERVICE = "s3";
  private static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss'Z'";

  public static String calculateChunkSignature(byte[] chunkData, String previousSignature, String accessKeyId, String secretKey, String region, String service, Date date) throws Exception {
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    String dateStamp = sdf.format(date);

    // Step 1: Create the string to sign for this chunk
    String stringToSign = "AWS4-HMAC-SHA256-PAYLOAD\n"
      + dateStamp + "\n"
      + dateStamp.substring(0, 8) + "/" + region + "/" + service + "/aws4_request\n"
      + previousSignature + "\n"
      + "" + "\n" // This would be the SHA256 hash of the chunk data; use an empty string for UNSIGNED-PAYLOAD
      + toHex(hash(chunkData)); // Hash of the current chunk

    // Step 2: Get the signing key
    byte[] signingKey = getSignatureKey(secretKey, dateStamp.substring(0, 8), region, service);

    // Step 3: Calculate the signature
    byte[] signature = hmacSHA256(signingKey, stringToSign);

    return toHex(signature);
  }

  private static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
    byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
    byte[] kDate = hmacSHA256(kSecret, dateStamp);
    byte[] kRegion = hmacSHA256(kDate, regionName);
    byte[] kService = hmacSHA256(kRegion, serviceName);
    return hmacSHA256(kService, "aws4_request");
  }

  private static byte[] hmacSHA256(byte[] key, String data) throws Exception {
    Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
    mac.init(new SecretKeySpec(key, HMAC_SHA256_ALGORITHM));
    return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
  }

  private static String toHex(byte[] data) {
    StringBuilder sb = new StringBuilder();
    for (byte b : data) {
      sb.append(String.format("%02x", b));
    }
    return sb.toString();
  }

  private static byte[] hash(byte[] data) throws Exception {
    // Use a suitable SHA-256 implementation to hash the data
    // This is a placeholder; you need to implement the hashing according to your environment
    return new byte[0];
  }

  public static String calculateSeedSignature() {
    return null;
  }
}
