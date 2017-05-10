// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.internal;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.doc.Doc;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 [#215] Internal "Docs" section where users of different permissions can view static content that is stored in .md static files on the backend, for easy editing.

 This is a wrapper for a Doc, which is an internal object,
 actually generated from source code when the project is compiled.

 This mechanism allows for documents tracked to the version of the source code
 to be made available with permissions only to logged-in users.
 */
public interface DocProvider {
  /**
   Fetch one File from internal resources classpath

   @return file
    @param path from which to fetch file
   */
  Doc fetchOne(String path) throws BusinessException;

  /**
   Fetch index of docs from internal resources classpath

   @return file
   */
  List fetchIndex() throws BusinessException;

  /**
   Build a list of Doc objects based on a simple list of keys

   @param keys to build a list of
   @return list of doc objects
   */
  JSONArray keysToJSONArray(List keys);
}
