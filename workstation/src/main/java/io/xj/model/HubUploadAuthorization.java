package io.xj.model;

public class HubUploadAuthorization {
  String awsAccessKeyId;
  String uploadPolicy;
  String uploadUrl;
  String waveformKey;
  String uploadPolicySignature;
  String bucketName;
  String bucketRegion;
  String acl;
  private String error;

  public HubUploadAuthorization() {
  }

  public String getAwsAccessKeyId() {
    return awsAccessKeyId;
  }

  public void setAwsAccessKeyId(String awsAccessKeyId) {
    this.awsAccessKeyId = awsAccessKeyId;
  }

  public String getUploadPolicy() {
    return uploadPolicy;
  }

  public void setUploadPolicy(String uploadPolicy) {
    this.uploadPolicy = uploadPolicy;
  }

  public String getUploadUrl() {
    return uploadUrl;
  }

  public void setUploadUrl(String uploadUrl) {
    this.uploadUrl = uploadUrl;
  }

  public String getWaveformKey() {
    return waveformKey;
  }

  public void setWaveformKey(String waveformKey) {
    this.waveformKey = waveformKey;
  }

  public String getUploadPolicySignature() {
    return uploadPolicySignature;
  }

  public void setUploadPolicySignature(String uploadPolicySignature) {
    this.uploadPolicySignature = uploadPolicySignature;
  }

  public String getBucketName() {
    return bucketName;
  }

  public void setBucketName(String bucketName) {
    this.bucketName = bucketName;
  }

  public String getBucketRegion() {
    return bucketRegion;
  }

  public void setBucketRegion(String bucketRegion) {
    this.bucketRegion = bucketRegion;
  }

  public String getAcl() {
    return acl;
  }

  public void setAcl(String acl) {
    this.acl = acl;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
