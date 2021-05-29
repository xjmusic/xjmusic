// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.plus.model.Person;


public interface GoogleProvider {
  /**
   Requires these System Properties to be set:
   auth.google.id
   auth.google.secret

   @return String authorization code request URL
   @throws HubAccessException if required system properties are not set
   */
  String getAuthCodeRequestUrl() throws HubAccessException;

  /**
   URI that the authorization server directs the resource owner's user-agent back to

   @return String URI
   @throws HubAccessException if required system properties are not set.
   */
  String getCallbackUrl() throws HubAccessException;

  /**
   Submits the access code to the token server for an OAuth2 access_token

   @param code of the first leg of the OAuth2 flow
   @return String access_token of a successful completed OAuth2 flow
   @throws HubAccessException if authentication fails
   */
  GoogleTokenResponse getTokenFromCode(String code) throws HubAccessException, HubAccessException;

  /**
   Retrieves the authenticating user's Google API person data.
   <p>
   Example:
   {
   "kind": "plus#person",
   "etag": "\"FT7X6cYw9BSnPtIywEFNNGVVdio/VUQf6-pycKq5jJriA2orbVoK42g\"",
   "emails": [
   {
   "value": "charneykaye@gmail.com",
   "type": "account"
   }
   ],
   "objectType": "person",
   "id": "163611394711834774896",
   "displayName": "Charney Kaye",
   "name": {
   "familyName": "Kaye",
   "givenName": "Charney"
   },
   "url": "https://plus.google.com/163611394711834774896",
   "image": {
   "url": "https://lh6.googleusercontent.com/-uVJxoVmL42M/AAAAAAAAAAI/AAAAAAAAACg/kMWD0kJityM/photo.jpg?sz=50",
   "isDefault": false
   },
   "isPlusUser": true,
   "language": "en",
   "circledByCount": 0,
   "verified": false,
   "cover": {
   "layout": "banner",
   "coverPhoto": {
   "url": "https://lh3.googleusercontent.com/-bk0tJruxTGxQYwSPPN_Mub70bYsSbnsRqnJvza3WhV-k9B81D0zAyeeshiyW40StSQjfls=s630-fcrop64=1,00000000ffffffff",
   "height": 528,
   "width": 940
   },
   "coverInfo": {
   "topImageOffset": 0,
   "leftImageOffset": 0
   }
   }
   }

   @param access_token for OAuth2 access to Google API on behalf of authenticating user
   @return profile as JSON
   @throws HubAccessException if auth fails
   */
  Person getMe(String access_token) throws HubAccessException;
}
