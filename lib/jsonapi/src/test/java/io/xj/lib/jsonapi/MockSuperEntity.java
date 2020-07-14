// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/**
 This thing has it all, for testing, one of every kind of field
 */
public class MockSuperEntity {
  private UUID id;
  private boolean primitiveBooleanValue;
  private int primitiveIntValue;
  private short primitiveShortValue;
  private long primitiveLongValue;
  private float primitiveFloatValue;
  private double primitiveDoubleValue;
  private Boolean booleanValue;
  private Integer integerValue;
  private Long longValue;
  private Short shortValue;
  private Double doubleValue;
  private Float floatValue;
  private Instant instantValue;
  private Timestamp timestampValue;
  private BigInteger bigIntegerValue;
  private String stringValue;
  private UUID uuidValue;
  private MockEntity proprietaryValue;
  private MockEnumValue enumValue;

  public boolean getPrimitiveBooleanValue() {
    return primitiveBooleanValue;
  }

  public void setPrimitiveBooleanValue(boolean primitiveBooleanValue) {
    this.primitiveBooleanValue = primitiveBooleanValue;
  }

  public int getPrimitiveIntValue() {
    return primitiveIntValue;
  }

  public void setPrimitiveIntValue(int primitiveIntValue) {
    this.primitiveIntValue = primitiveIntValue;
  }

  public short getPrimitiveShortValue() {
    return primitiveShortValue;
  }

  public void setPrimitiveShortValue(short primitiveShortValue) {
    this.primitiveShortValue = primitiveShortValue;
  }

  public long getPrimitiveLongValue() {
    return primitiveLongValue;
  }

  public void setPrimitiveLongValue(long primitiveLongValue) {
    this.primitiveLongValue = primitiveLongValue;
  }

  public float getPrimitiveFloatValue() {
    return primitiveFloatValue;
  }

  public void setPrimitiveFloatValue(float primitiveFloatValue) {
    this.primitiveFloatValue = primitiveFloatValue;
  }

  public double getPrimitiveDoubleValue() {
    return primitiveDoubleValue;
  }

  public void setPrimitiveDoubleValue(double primitiveDoubleValue) {
    this.primitiveDoubleValue = primitiveDoubleValue;
  }

  public Integer getIntegerValue() {
    return integerValue;
  }

  public void setIntegerValue(Integer integerValue) {
    this.integerValue = integerValue;
  }

  public Long getLongValue() {
    return longValue;
  }

  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }

  public Double getDoubleValue() {
    return doubleValue;
  }

  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }

  public Float getFloatValue() {
    return floatValue;
  }

  public void setFloatValue(Float floatValue) {
    this.floatValue = floatValue;
  }

  public Instant getInstantValue() {
    return instantValue;
  }

  public void setInstantValue(Instant instantValue) {
    this.instantValue = instantValue;
  }

  public BigInteger getBigIntegerValue() {
    return bigIntegerValue;
  }

  public void setBigIntegerValue(BigInteger bigIntegerValue) {
    this.bigIntegerValue = bigIntegerValue;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  public UUID getUuidValue() {
    return uuidValue;
  }

  public void setUuidValue(UUID uuidValue) {
    this.uuidValue = uuidValue;
  }

  public Short getShortValue() {
    return shortValue;
  }

  public void setShortValue(Short shortValue) {
    this.shortValue = shortValue;
  }

  public Timestamp getTimestampValue() {
    return timestampValue;
  }

  public void setTimestampValue(Timestamp timestampValue) {
    this.timestampValue = timestampValue;
  }

  public void setWillFailBecauseAcceptsNoParameters() {
    // noop
  }

  public Boolean getBooleanValue() {
    return booleanValue;
  }

  public void setBooleanValue(Boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  public MockEntity getProprietaryValue() {
    return proprietaryValue;
  }

  public void setProprietaryValue(MockEntity proprietaryValue) {
    this.proprietaryValue = proprietaryValue;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public MockEnumValue getEnumValue() {
    return enumValue;
  }

  public void setEnumValue(MockEnumValue enumValue) {
    this.enumValue = enumValue;
  }
}
