// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus;

import ch.qos.logback.classic.LoggerContext;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Module;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.nexus.craft.CraftModule;
import io.xj.nexus.dao.NexusDAOModule;
import io.xj.nexus.dub.DubModule;
import io.xj.nexus.fabricator.NexusFabricatorModule;
import io.xj.nexus.hub_client.client.HubClientModule;
import io.xj.nexus.persistence.NexusEntityStoreModule;
import io.xj.nexus.work.NexusWorkModule;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;
import java.util.Set;

/**
 Nexus service
 */
public interface Main {
  Set<Module> injectorModules = ImmutableSet.of(
    new CraftModule(),
    new HubClientModule(),
    new NexusDAOModule(),
    new DubModule(),
    new NexusFabricatorModule(),
    new FileStoreModule(),
    new JsonApiModule(),
    new MixerModule(),
    new NexusEntityStoreModule(),
    new NexusWorkModule()
  );

  /**
   Main method.

   @param args arguments-- the first argument must be the path to the configuration file
   */
  @SuppressWarnings("DuplicatedCode")
  static void main(String[] args) throws AppException, UnknownHostException {
    var config = AppConfiguration.getDefault();
    var env = Environment.fromSystem();
    if (0 < env.getAwsSecretName().length())
      env = Environment.augmentSystem(getSecret(env.getAwsDefaultRegion(), env.getAwsSecretName()));

    var injector = AppConfiguration.inject(config, injectorModules);

    // Add context to logs
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.setPackagingDataEnabled(true);
    lc.putProperty("source", "java");
    lc.putProperty("service", "nexus");
    lc.putProperty("host", env.getHostname());
    lc.putProperty("env", env.getEnvironment());

    // Instantiate app
    NexusApp app = new NexusApp(injector);

    // Shutdown Hook
    Runtime.getRuntime().addShutdownHook(new Thread(app::finish));

    // start
    app.start();
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
  static String getSecret(String region, String secretName) {
    AWSSecretsManager client = AWSSecretsManagerClientBuilder.standard().withRegion(region).build();
    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);
    return client.getSecretValue(getSecretValueRequest).getSecretString();
  }
}
