// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.secret;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 Deploy k8s cluster on GCP #180684409
 */
public enum Secrets {
  ;
  private static final Logger LOG = LoggerFactory.getLogger(Secrets.class);

  /**
   Get the Environment from system environment variables, augmented by any secret managers present in said vars.

   @return environment
   */
  public static Environment environment() {
    var env = Environment.fromSystem();

    if (Values.isSet(env.getAwsSecretName()) && Values.isSet(env.getAwsSecretName()))
      env = Environment.augmentSystem(fetchAwsSecret(env.getAwsDefaultRegion(), env.getAwsSecretName()));

    if (Values.isSet(env.getGcpProjectId()) && Values.isSet(env.getGcpSecretId()))
      env = Environment.augmentSystem(fetchGcpSecret(env.getGcpProjectId(), env.getGcpSecretId()));

    return env;

  }

  /**
   AWS code snippet for fetching app secret.
   If you need more information about configurations or implementing the sample code, visit the AWS docs:
   https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/java-dg-samples.html#prerequisites
   <p>
   In this sample we only handle the specific exceptions for the 'GetSecretValue' API.
   See https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
   Runtime exceptions are passed through.

   @param region     from which to get secret
   @param secretName to retrieve
   @return app secret
   */
  private static String fetchAwsSecret(String region, String secretName) {
    AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().withRegion(region).build();
    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
    return client.getSecretValue(getSecretValueRequest).getSecretString();
  }

  /**
   Get the Environment via GCP Secret Manager, merged with the system environment params.
   This method is static to use before Guice injection, probably in a program's Main.java.

   @return environment
   */
  private static String fetchGcpSecret(String projectId, String secretId) {
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, "latest");
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
      return response.getPayload().getData().toStringUtf8();

    } catch (IOException e) {
      LOG.error("Failed to get secret from GCP!", e);
      throw new RuntimeException(e);
    }
  }
}
