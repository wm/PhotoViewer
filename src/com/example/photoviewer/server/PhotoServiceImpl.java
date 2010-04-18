package com.example.photoviewer.server;

import com.example.photoviewer.client.FlickrUser;
import com.example.photoviewer.client.PhotoService;
import com.example.photoviewer.client.PhotoViewerException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class PhotoServiceImpl extends RemoteServiceServlet implements PhotoService
{
	private static final String API_KEY = "";
	private static final String PhotoSet_URL = "http://api.flickr.com/services/rest/?method=flickr.photosets.getList&jsoncallback=flickrPhotosetsGetList";
	private static final String SetPhotoList_URL = "http://api.flickr.com/services/rest/?method=flickr.photosets.getPhotos&jsoncallback=flickrPhotosetsGetPhotos";
	private static final String User_URL = "http://api.flickr.com/services/rest/?method=flickr.people.findByUsername";
	private static DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

	public String greetServer(String input)
	{
		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");
		return "I am running " + serverInfo + ".<br><br>It looks like you are using:<br>" + userAgent;
	}

	public FlickrUser getFlickrUser(String username, String setId) throws PhotoViewerException  {
		FlickrUser flickrUser = new FlickrUser();
		try{
			if( setId != null && !setId.isEmpty() && username != null && !username.isEmpty()){
				flickrUser.setUsername(username);
				String userId = flickrUserFindByUsernameXML(username);
				flickrUser.setUserId(userId);
				flickrUser.setPhotosets( flickrPhotosetsGetList(flickrUser.getUserId()) );
				flickrUser.setSelectedSetPhotos(flickrPhotosetsGetPhotos(setId));
				flickrUser.setMessage("OK");
			}else if( setId != null && !setId.isEmpty()){
				flickrUser.setSelectedSetPhotos(flickrPhotosetsGetPhotos(setId));
				flickrUser.setMessage("OK");
			}else if( username != null && !username.isEmpty() ){
				flickrUser.setUsername(username);
				String userId = flickrUserFindByUsernameXML(username);
				flickrUser.setUserId(userId);
				flickrUser.setPhotosets( flickrPhotosetsGetList(flickrUser.getUserId()) );
				flickrUser.setMessage("OK");
			}
		}catch(IOException e){
			flickrUser.setMessage("IOException: "+ e.getMessage());
		}catch(SAXException e){
			flickrUser.setMessage("SAXException: "+ e.getMessage());
		}catch(ParserConfigurationException e){
			flickrUser.setMessage("ParserConfigurationException: "+ e.getMessage());
		}
		return flickrUser;
	}
	
	private String flickrPhotosetsGetPhotos (String setId) throws IOException
	{
		String url = SetPhotoList_URL + "&api_key=" + API_KEY + "&photoset_id=" + setId + "&format=json";
		return flickrApiCall(url);
	}

	private String flickrPhotosetsGetList (String username) throws IOException
	{	
		String url = PhotoSet_URL + "&api_key=" + API_KEY + "&user_id=" + username + "&format=json";
		return flickrApiCall(url);
	}

	private String flickrApiCall(String url) throws IOException{
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(url);
		int resultCode = client.executeMethod(get);
		if (resultCode == 200) {
			String response = get.getResponseBodyAsString();
			get.releaseConnection();
			return response;
		}
		else {
			throw new IOException("HTTP Communication problem, response code: "+resultCode);
		}
	}
	
	private String flickrUserFindByUsernameXML(String username) throws IOException, ParserConfigurationException, SAXException, PhotoViewerException
	
	{
		String url = User_URL + "&api_key=" + API_KEY + "&username=" + username;

		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(url);
		try {
			int resultCode = client.executeMethod(get);
			if (resultCode == 200) {
				InputStream input = get.getResponseBodyAsStream();
				DocumentBuilder builder = builderFactory.newDocumentBuilder();
				Document userXML = builder.parse(input);
				if( userXML.getElementsByTagName("err").item(0) == null){
				  return userXML.getElementsByTagName("user").item(0).getAttributes().getNamedItem("nsid").getNodeValue();
				}else{
					String msg = userXML.getElementsByTagName("err").item(0).getAttributes().getNamedItem("msg").getNodeValue();
					throw new PhotoViewerException(msg);
				}
			}
			else {
				throw new IOException("HTTP Communication problem, response code: "+resultCode);
			}
		}
		finally {
			get.releaseConnection();
		}
	}
}