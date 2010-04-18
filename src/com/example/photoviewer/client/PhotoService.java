package com.example.photoviewer.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("photo") // This is absolutely necessary
public interface PhotoService extends RemoteService
{
	public String greetServer (String name); // You have to implement this now
	public FlickrUser getFlickrUser(String username, String selectedSetId) throws PhotoViewerException;
}
