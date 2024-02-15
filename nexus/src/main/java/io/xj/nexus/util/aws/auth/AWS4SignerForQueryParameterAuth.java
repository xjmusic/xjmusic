package io.xj.nexus.util.aws.auth;

import io.xj.nexus.util.aws.util.BinaryUtils;

import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 Sample AWS4 signer demonstrating how to sign requests to Amazon S3 using
 query string parameters.
 */
public class AWS4SignerForQueryParameterAuth extends AWS4SignerBase {

  public AWS4SignerForQueryParameterAuth(URL endpointUrl, String httpMethod,
                                         String serviceName, String regionName) {
    super(endpointUrl, httpMethod, serviceName, regionName);
  }

  /**
   Computes an AWS4 authorization for a request, suitable for embedding in
   query parameters.

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
    // to ISO 8601 format
    // for use in signature generation
    Date now = new Date();
    String dateTimeStamp = dateTimeFormat.format(now);

    // make sure "Host" header is added
    String hostHeader = endpointUrl.getHost();
    int port = endpointUrl.getPort();
    if (port > -1) {
      hostHeader += ":" + port;
    }
    headers.put("Host", hostHeader);

    // canonicalized headers need to be expressed in the query
    // parameters processed in the signature
    String canonicalizedHeaderNames = getCanonicalizeHeaderNames(headers);
    String canonicalizedHeaders = getCanonicalizedHeaderString(headers);

    // we need scope as part of the query parameters
    String dateStamp = dateStampFormat.format(now);
    String scope = dateStamp + "/" + regionName + "/" + serviceName + "/" + TERMINATOR;

    // add the fixed authorization params required by Signature V4
    queryParameters.put("X-Amz-Algorithm", SCHEME + "-" + ALGORITHM);
    queryParameters.put("X-Amz-Credential", awsAccessKey + "/" + scope);

    // x-amz-date is now added as a query parameter, but still need to be in ISO8601 basic form
    queryParameters.put("X-Amz-Date", dateTimeStamp);

    queryParameters.put("X-Amz-SignedHeaders", canonicalizedHeaderNames);

    // build the expanded canonical query parameter string that will go into the
    // signature computation
    String canonicalizedQueryParameters = getCanonicalizedQueryString(queryParameters);

    // express all the header and query parameter data as a canonical request string
    String canonicalRequest = getCanonicalRequest(endpointUrl, httpMethod,
      canonicalizedQueryParameters, canonicalizedHeaderNames,
      canonicalizedHeaders, bodyHash);
    System.out.println("--------- Canonical request --------");
    System.out.println(canonicalRequest);
    System.out.println("------------------------------------");

    // construct the string to be signed
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

    // form up the authorization parameters for the caller to place in the query string
    return "X-Amz-Algorithm=" + queryParameters.get("X-Amz-Algorithm") +
      "&X-Amz-Credential=" + queryParameters.get("X-Amz-Credential") +
      "&X-Amz-Date=" + queryParameters.get("X-Amz-Date") +
      "&X-Amz-Expires=" + queryParameters.get("X-Amz-Expires") +
      "&X-Amz-SignedHeaders=" + queryParameters.get("X-Amz-SignedHeaders") +
      "&X-Amz-Signature=" + BinaryUtils.toHex(signature);
  }
}
