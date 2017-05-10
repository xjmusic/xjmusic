// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.internal;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.doc.Doc;
import io.outright.xj.core.util.Text;

import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 [#215] Internal "Docs" section where users of different permissions can view static content that is stored in .md static files on the backend, for easy editing.
 <p>
 This is a wrapper for a Doc, which is an internal object,
 actually generated from source code when the project is compiled.
 <p>
 This mechanism allows for documents tracked to the version of the source code
 to be made available with permissions only to logged-in users.
 */
public class DocProviderImpl implements DocProvider {
  private final static String DOC_FILE_EXTENSION = ".md";
  private static final String PATH_SEPARATOR = "/";
  private static final String DOC_INDEX_FILENAME = "index.yaml";
  private static final Yaml yaml = new Yaml();

  @Override
  public Doc fetchOne(String key) throws BusinessException {
    return Doc.from(key, streamForKey(key));
  }

  @Override
  public List fetchIndex() throws BusinessException {
    InputStream stream = streamForIndex();
    return yaml.loadAs(stream, List.class);
  }

  @Override
  public JSONArray keysToJSONArray(List keys) {
    JSONArray docs = new JSONArray();
    for (Object key : keys) {
      JSONObject doc = new JSONObject();
      doc.put(Doc.KEY_KEY, key);
      doc.put(Doc.KEY_NAME, Text.DocNameForKey(String.valueOf(key)));
      docs.put(doc);
    }
    return docs;
  }

  /**
   Get a stream for a doc, by key

   @param key of doc to get stream for
   @return streamed doc content
   */
  private InputStream streamForKey(String key) throws BusinessException {
    InputStream stream = getClass().getResourceAsStream(pathForFile(key + DOC_FILE_EXTENSION));
    if (Objects.isNull(stream))
      throw new BusinessException("Cannot open doc: " + key);

    return stream;
  }

  /**
   Get a stream for doc index

   @return streamed doc content
   */
  private InputStream streamForIndex() throws BusinessException {
    InputStream stream = getClass().getResourceAsStream(pathForFile(DOC_INDEX_FILENAME));
    if (Objects.isNull(stream))
      throw new BusinessException("Cannot open doc index: " + DOC_INDEX_FILENAME);

    return stream;
  }

  /**
   Path to Doc, from index

   @param index to get path for
   @return path
   */
  private String pathForFile(String index) {
    return PATH_SEPARATOR +
      Doc.KEY_MANY + PATH_SEPARATOR +
      index;
  }

}
