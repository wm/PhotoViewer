package com.example.photoviewer.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.json.client.JSONParser;

import com.example.photoviewer.client.JSONRequestHandler;

public class FlickrUser implements IsSerializable {
  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;
  private static final String UNKNOWN = "UNKNOWN";
  private String username = "";
  private String userId = "";
  private String photosets = "";
  private String selectedSetPhotos = "";
  private String message = "";
  private int count = 0;
  

public FlickrUser(){
	  username = UNKNOWN;
  }

  public String getUsername() {
	  return username;
  }

  public void setUsername(String username) {
	  this.username = username;
  }

  public String getPhotosets() {
	  return photosets;
  }

  public void setPhotosets(String photosets) {
	  this.photosets = photosets;
  }

  public String getSelectedSetPhotos() {
	  return selectedSetPhotos;
  }

  public void setSelectedSetPhotos(String selectedSetPhotos) {
	  this.selectedSetPhotos = selectedSetPhotos;
  }

  public void setMessage(String message) {
	  this.message = message;
  }

  public String getMessage() {
	  return message;
  }
  
  public void getSelectedSetPhotosJSON(JSONRequestHandler handler){
	  createCallbackFunction( handler, "flickrPhotosetsGetPhotos" );
	  JSONParser.parse(this.selectedSetPhotos);
  }
  
  public void getPhotosetsJSON(JSONRequestHandler handler){
	  createCallbackFunction( handler, "flickrPhotosetsGetList" );
	  JSONParser.parse(this.photosets);
  }
  
  private native static void createCallbackFunction (JSONRequestHandler obj, String callbackName)/*-{
	tmpcallback = function(j) {
		obj.@com.example.photoviewer.client.JSONRequestHandler::onRequestComplete(Lcom/google/gwt/core/client/JavaScriptObject;)(j);
	};
	eval("window." + callbackName + "=tmpcallback");
  }-*/;

  public void setUserId(String userId) {
	  this.userId = userId;
  }
  
  public String getUserId(){
	  return this.userId;
	  //return "69919827@N00";
  }
  
  public int getCount() {
		return count;
  }

  public void setCount(int count) {
		this.count = count;
  }
 
}
