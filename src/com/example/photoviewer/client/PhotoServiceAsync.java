package com.example.photoviewer.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface PhotoServiceAsync
{
	void greetServer(String input, AsyncCallback<String> callback);
	void getFlickrUser(String username, String selectedSetId, AsyncCallback<FlickrUser> callback);
}
