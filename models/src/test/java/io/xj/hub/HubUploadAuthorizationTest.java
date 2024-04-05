package io.xj.hub;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HubUploadAuthorizationTest {

  private HubUploadAuthorization subject;

  @BeforeEach
  void setUp() {
    subject = new HubUploadAuthorization();
  }

  @Test
  void getAwsAccessKeyId_setAwsAccessKeyId() {
    subject.setAwsAccessKeyId("1293590adg");

    assertEquals("1293590adg", subject.getAwsAccessKeyId());
  }

  @Test
  void getUploadPolicy_setUploadPolicy() {
    subject.setUploadPolicy("PutObjPolicy");

    assertEquals("PutObjPolicy", subject.getUploadPolicy());
  }

  @Test
  void getUploadUrl_setUploadUrl() {
    subject.setUploadUrl("https://audio.xj.io/testaudio.wav");

    assertEquals("https://audio.xj.io/testaudio.wav", subject.getUploadUrl());
  }

  @Test
  void getWaveformKey_setWaveformKey() {
    subject.setWaveformKey("testaudio.wav");

    assertEquals("testaudio.wav", subject.getWaveformKey());
  }

  @Test
  void getUploadPolicySignature_setUploadPolicySignature() {
    subject.setUploadPolicySignature("ljkaslkjg98983");

    assertEquals("ljkaslkjg98983", subject.getUploadPolicySignature());
  }

  @Test
  void getBucketName_setBucketName() {
    subject.setBucketName("xj-prod-audio");

    assertEquals("xj-prod-audio", subject.getBucketName());
  }

  @Test
  void getBucketRegion_setBucketRegion() {
    subject.setBucketRegion("us-west-1");

    assertEquals("us-west-1", subject.getBucketRegion());
  }

  @Test
  void getAcl_setAcl() {
    subject.setAcl("public");

    assertEquals("public", subject.getAcl());
  }

  @Test
  void setError_getError() {
    subject.setError("Error: Invalid audio file");

    assertEquals("Error: Invalid audio file", subject.getError());
  }
}
