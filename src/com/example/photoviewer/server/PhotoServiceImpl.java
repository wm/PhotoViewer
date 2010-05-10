package com.example.photoviewer.server;

import com.example.photoviewer.client.FlickrUser;
import com.example.photoviewer.client.PhotoService;
import com.example.photoviewer.client.PhotoViewerException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import java.io.StringReader;


/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class PhotoServiceImpl extends RemoteServiceServlet implements PhotoService
{
	private static final String API_KEY = "7b6ba415a261a9822be49b8b7e4b6c79";
	private static final String PhotoSet_URL = "http://api.flickr.com/services/rest/?method=flickr.photosets.getList&jsoncallback=flickrPhotosetsGetList";
	private static final String SetPhotoList_URL = "http://api.flickr.com/services/rest/?method=flickr.photosets.getPhotos&jsoncallback=flickrPhotosetsGetPhotos";
	private static final String User_URL = "http://api.flickr.com/services/rest/?method=flickr.people.findByUsername";

	public String greetServer(String input)
	{
		String serverInfo = getServletContext().getServerInfo();
		String userAgent = getThreadLocalRequest().getHeader("User-Agent");
		return "I am running " + serverInfo + ".<br><br>It looks like you are using:<br>" + userAgent;
	}

	public FlickrUser getFlickrUser(String username, String setId) throws PhotoViewerException  {
		FlickrUser flickrUser = new FlickrUser();
		FlickrUserInfo fui;
		try{
			if( setId != null && !setId.isEmpty() && username != null && !username.isEmpty()){
				flickrUser.setUsername(username);
				String userId = flickrUserFindByUsernameXML(username);
				flickrUser.setUserId(userId);
				flickrUser.setPhotosets( flickrPhotosetsGetList(flickrUser.getUserId()) );
				flickrUser.setSelectedSetPhotos(flickrPhotosetsGetPhotos(setId));
				flickrUser.setMessage("OK");
				fui = new FlickrUserInfo(flickrUser);
			}else if( setId != null && !setId.isEmpty()){
				flickrUser.setSelectedSetPhotos(flickrPhotosetsGetPhotos(setId));
				flickrUser.setMessage("OK");
				fui = new FlickrUserInfo(flickrUser);
			}else if( username != null && !username.isEmpty() ){
				flickrUser.setUsername(username);
				String userId = flickrUserFindByUsernameXML(username);
				flickrUser.setUserId(userId);
				flickrUser.setPhotosets( flickrPhotosetsGetList(flickrUser.getUserId()) );
				flickrUser.setMessage("OK");
				fui = new FlickrUserInfo(flickrUser);
				fui.save();
				flickrUser.setCount(fui.getCount());
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

		URL urlIn = new URL(url);
		BufferedReader reader = new BufferedReader(new InputStreamReader(urlIn.openStream()));
		String line;

		StringBuffer response = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			response.append(line);
		}
		reader.close();
		return response.toString();

	}

	private String flickrUserFindByUsernameXML(String username) throws IOException, ParserConfigurationException, SAXException, PhotoViewerException
	{
		String url = User_URL + "&api_key=" + API_KEY + "&username=" + username;
		String responseStr = "";

		URL urlIn = new URL(url);
		BufferedReader reader = new BufferedReader(new InputStreamReader(urlIn.openStream()));
		String line;

		StringBuffer response = new StringBuffer();
		while ((line = reader.readLine()) != null) {
			response.append(line);
		}
		reader.close();
		responseStr = response.toString();
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document userXML = builder.parse(new InputSource(new StringReader(responseStr)));

		if( userXML.getElementsByTagName("err").item(0) == null){
			return userXML.getElementsByTagName("user").item(0).getAttributes().getNamedItem("nsid").getNodeValue();
		}else{
			String msg = userXML.getElementsByTagName("err").item(0).getAttributes().getNamedItem("msg").getNodeValue();
			throw new PhotoViewerException(msg);
		}
	}
}