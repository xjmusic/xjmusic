package io.xj.nexus.util.aws_upload;

import io.xj.nexus.util.aws_upload.auth.AWS4SignerBase;
import io.xj.nexus.util.aws_upload.auth.AWS4SignerForAuthorizationHeader;
import io.xj.nexus.util.aws_upload.util.BinaryUtils;
import io.xj.nexus.util.aws_upload.util.HttpUtils;
import io.xj.nexus.work.DubWorkImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 Sample code showing how to PUT objects to Amazon S3 with Signature V4
 authorization
 */
public class PutS3ObjectSample {
  private static final Logger LOG = LoggerFactory.getLogger(PutS3ObjectSample.class);

  private static final String objectContent =
          """
                  Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc tortor metus, sagittis eget augue ut,
                  feugiat vehicula risus. Integer tortor mauris, vehicula nec mollis et, consectetur eget tortor. In ut
                  elit sagittis, ultrices est ut, iaculis turpis. In hac habitasse platea dictumst. Donec laoreet tellus
                  at auctor tempus. Praesent nec diam sed urna sollicitudin vehicula eget id est. Vivamus sed laoreet
                  lectus. Aliquam convallis condimentum risus, vitae porta justo venenatis vitae. Phasellus vitae nunc
                  varius, volutpat quam nec, mollis urna. Donec tempus, nisi vitae gravida facilisis, sapien sem malesuada
                  purus, id semper libero ipsum condimentum nulla. Suspendisse vel mi leo. Morbi pellentesque placerat congue.
                  Nunc sollicitudin nunc diam, nec hendrerit dui commodo sed. Duis dapibus commodo elit, id commodo erat
                  congue id. Aliquam erat volutpat.
                  """;

  /**
   Uploads content to an Amazon S3 object in a single call using Signature V4 authorization.
   */
  public static void putS3Object(String bucketName, String regionName, String awsAccessKey, String awsSecretKey) {
    LOG.debug("************************************************");
    LOG.debug("*        Executing sample 'PutS3Object'        *");
    LOG.debug("************************************************");

    URL endpointUrl;
    try {
      if (regionName.equals("us-east-1")) {
        endpointUrl = new URL("https://s3.amazonaws.com/" + bucketName + "/ExampleObject.txt");
      } else {
        endpointUrl = new URL("https://s3-" + regionName + ".amazonaws.com/" + bucketName + "/ExampleObject.txt");
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException("Unable to parse service endpoint: " + e.getMessage());
    }

    // precompute hash of the body content
    byte[] contentHash = AWS4SignerBase.hash(objectContent);
    String contentHashString = BinaryUtils.toHex(contentHash);

    Map<String, String> headers = new HashMap<>();
    headers.put("x-amz-content-sha256", contentHashString);
    headers.put("content-length", "" + objectContent.length());
    headers.put("x-amz-storage-class", "REDUCED_REDUNDANCY");

    AWS4SignerForAuthorizationHeader signer = new AWS4SignerForAuthorizationHeader(
      endpointUrl, "PUT", "s3", regionName);
    String authorization = signer.computeSignature(headers,
      null, // no query parameters
      contentHashString,
      awsAccessKey,
      awsSecretKey);

    // express authorization for this as a header
    headers.put("Authorization", authorization);

    // make the call to Amazon S3
    String response = HttpUtils.invokeHttpRequest(endpointUrl, "PUT", headers, objectContent);
    LOG.debug("--------- Response content ---------");
    LOG.debug(response);
    LOG.debug("------------------------------------");
  }
}
