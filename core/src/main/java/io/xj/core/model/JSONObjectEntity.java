// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model;

import org.json.JSONObject;

/**
 Entity for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an instance of this object
 <p>
 Business logic ought to be performed beginning with an instance of this object,
 to implement common methods.
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public abstract class JSONObjectEntity extends Entity {

  /**
   Entity to a JSON object

   @throws Exception if invalid.
   @return JSONObject
   */
  public abstract JSONObject toJSONObject() throws Exception;

}
