package io.xj.nexus.util.aws.auth;

import io.xj.nexus.util.aws.util.BinaryUtils;

import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 Sample AWS4 signer demonstrating how to sign requests to Amazon S3 using an
 'Authorization' header.
 */
public class AWS4SignerForAuthorizationHeader extends AWS4SignerBase {

  public AWS4SignerForAuthorizationHeader(URL endpointUrl, String httpMethod,
                                          String serviceName, String regionName) {
    super(endpointUrl, httpMethod, serviceName, regionName);
  }

  /**
   Computes an AWS4 signature for a request, ready for inclusion as an
   'Authorization' header.

   @param headers         The request headers; 'Host' and 'X-Amz-Date' will be added to
   this set.
   @param queryParameters Any query parameters that will be added to the endpoint. The
   parameters should be specified in canonical format.
   @param bodyHash        Precomputed SHA256 hash of the request body content; this
   value should also be set as the header 'X-Amz-Content-SHA256'
   for non-streaming uploads.
   @param awsAccessKey    The user's AWS Access Key.
   @param awsSecretKey    The user's AWS Secret Key.
   @return The computed authorization string for the request. This value
   needs to be set as the header 'Authorization' on the subsequent
   HTTP request.
   */
  public String computeSignature(Map<String, String> headers,
                                 Map<String, String> queryParameters,
                                 String bodyHash,
                                 String awsAccessKey,
                                 String awsSecretKey) {
    // first get the date and time for the subsequent request, and convert
    // to ISO 8601 format for use in signature generation
    Date now = new Date();
    String dateTimeStamp = dateTimeFormat.format(now);

    // update the headers with required 'x-amz-date' and 'host' values
    headers.put("x-amz-date", dateTimeStamp);

    String hostHeader = endpointUrl.getHost();
    int port = endpointUrl.getPort();
    if (port > -1) {
      hostHeader = hostHeader.concat(":" + port);
    }
    headers.put("Host", hostHeader);

    // canonicalize the headers; we need the set of header names as well as the
    // names and values to go into the signature process
    String canonicalizedHeaderNames = getCanonicalizeHeaderNames(headers);
    String canonicalizedHeaders = getCanonicalizedHeaderString(headers);

    // if any query string parameters have been supplied, canonicalize them
    String canonicalizedQueryParameters = getCanonicalizedQueryString(queryParameters);

    // canonicalize the various components of the request
    String canonicalRequest = getCanonicalRequest(endpointUrl, httpMethod,
      canonicalizedQueryParameters, canonicalizedHeaderNames,
      canonicalizedHeaders, bodyHash);
    System.out.println("--------- Canonical request --------");
    System.out.println(canonicalRequest);
    System.out.println("------------------------------------");

    // construct the string to be signed
    String dateStamp = dateStampFormat.format(now);
    String scope = dateStamp + "/" + regionName + "/" + serviceName + "/" + TERMINATOR;
    String stringToSign = getStringToSign(dateTimeStamp, scope, canonicalRequest);
    System.out.println("--------- String to sign -----------");
    System.out.println(stringToSign);
    System.out.println("------------------------------------");

    // compute the signing key
    byte[] kSecret = (SCHEME + awsSecretKey).getBytes();
    byte[] kDate = sign(dateStamp, kSecret);
    byte[] kRegion = sign(regionName, kDate);
    byte[] kService = sign(serviceName, kRegion);
    byte[] kSigning = sign(TERMINATOR, kService);
    byte[] signature = sign(stringToSign, kSigning);

    String credentialsAuthorizationHeader =
      "Credential=" + awsAccessKey + "/" + scope;
    String signedHeadersAuthorizationHeader =
      "SignedHeaders=" + canonicalizedHeaderNames;
    String signatureAuthorizationHeader =
      "Signature=" + BinaryUtils.toHex(signature);

      return SCHEME + "-" + ALGORITHM + " "
      + credentialsAuthorizationHeader + ", "
      + signedHeadersAuthorizationHeader + ", "
      + signatureAuthorizationHeader;
  }
}
