package com.example.photoviewer.client;

import java.util.HashMap;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.*;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.DOM;


public class PhotoViewer implements EntryPoint,ValueChangeHandler<String>
{
	private HorizontalPanel inputPanel;
	private TextBox username;
	private ListBox setlist = new ListBox();
	private Button submit;
	private HTML photoHTML;
	private static final int SET_SELECTED = 0;
	private static final int SUBMIT_CLICKED = 1;
	private final PhotoServiceAsync ws = GWT.create(PhotoService.class); // Making the RPC

	/**
	 * Keep historical information so we can use he back button.
	 */
	public void onValueChange(ValueChangeEvent<String> event) {
		onHistoryChange(event.getValue());
	}

	// Loads the data based on the historical params.
	public void onHistoryChange(String token) {
		HashMap<String,String> params = parseParamString(token);
		String set = params.get("set");
		String user = params.get("user");
		username.setText(user);
		if(set != null && !set.isEmpty() && !set.equals("null")) validateAndSubmit(SET_SELECTED,user,set,false);
		else validateAndSubmit(SUBMIT_CLICKED,user,null,false);
	}

	public void initHistoryState(){
		String token = History.getToken();
		if(token.length() > 0){
			onHistoryChange(token);
		}
	}

	public void onModuleLoad()
	{
		inputPanel = new HorizontalPanel();
		inputPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		inputPanel.setStyleName("person-input-panel"); // See CSS under "war" folder

		// Create textbox for username
		Label lbl = new Label("Enter Flickr username: ");
		inputPanel.add(lbl);
		username = new TextBox();
		username.addKeyPressHandler(new KeyPressHandler() {
			public void onKeyPress (KeyPressEvent event)
			{
				if (event.getCharCode() == KeyCodes.KEY_ENTER) {
					validateAndSubmit(SUBMIT_CLICKED,username.getText(),null,true);
				}
			}
		});
		username.setVisibleLength(10);
		inputPanel.add(username);

		// Create a set selection drop list
		Panel setSelection = new VerticalPanel();
		setlist = new ListBox();
		setlist.addChangeHandler(new ChangeHandler () {
			public void onChange(ChangeEvent event) {
				validateAndSubmit(SET_SELECTED,username.getText(),setlist.getValue(setlist.getSelectedIndex()),true);
			}
		});
		setlist.addItem("", "");
		setSelection.add(setlist);
		inputPanel.add(setSelection);
		setlist.setEnabled(false);

		// Create Submit button
		submit = new Button("Submit");
		submit.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				validateAndSubmit(SUBMIT_CLICKED, username.getText(),null,true);
			}
		});

		// Add button to inputs
		inputPanel.add(submit);
		inputPanel.setCellVerticalAlignment(submit, HasVerticalAlignment.ALIGN_MIDDLE);
		inputPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
		inputPanel.setCellWidth(lbl, "160px");
		inputPanel.setCellWidth(username, "90px");
		inputPanel.setCellWidth(lbl, "160px");
		inputPanel.setCellWidth(lbl, "160px");
		inputPanel.setCellHorizontalAlignment(submit,HasHorizontalAlignment.ALIGN_RIGHT);
		
		// Add the input panel to the page
		RootPanel.get("input").add(inputPanel); // input is the div

		// Create widget for HTML output
		photoHTML = new HTML();
		photoHTML.setVisible(false);
		RootPanel.get("output").add(photoHTML);

		//Add history functionality
		History.addValueChangeHandler(this);
		initHistoryState();
	}

	private void validateAndSubmit(int event,String name, String set, boolean affectsHistory)
	{
		if(name != null) name = name.trim();
		if (affectsHistory) {History.newItem("?user="+name+"&set="+set,false);}
		
		if(event == SUBMIT_CLICKED){
		  fetchUserSets(name);
	    }else{
	      fetchUserPhotos(name,set);
	    }
	}

	private void fetchUserPhotos(String user,String selectedSetId){
		photoHTML.setHTML("Loading...");
		photoHTML.setVisible(true);
		ws.getFlickrUser(user, selectedSetId, new AsyncCallback<FlickrUser>() {
			@Override
			public void onFailure(Throwable caught)
			{
				Window.alert(caught.getMessage());
				photoHTML.setHTML("");
			}

			@Override
			public void onSuccess(FlickrUser flickrUser)
			{
				flickrUser.getSelectedSetPhotosJSON(new JSONRequestHandler() {
					public void onRequestComplete (JavaScriptObject json)
					{
						JSONObject jso = new JSONObject(json);
						JSONString stat = jso.get("stat").isString();

						if(!stat.stringValue().equals("ok")){
							JSONString msg = jso.get("message").isString();
							Window.alert(msg.stringValue());
							photoHTML.setHTML("");
						}else{
							JSONObject photosets = jso.get("photoset").isObject();
							JSONArray photoArray = photosets.get("photo").isArray();

							String html = "";
							for(int i=0; i < photoArray.size(); i++){
						        String auth = DOM.getElementById("auth").getAttribute("value");
								JSONObject photo = photoArray.get(i).isObject();
								int farm = (int)photo.get("farm").isNumber().doubleValue();
								String server = photo.get("server").isString().stringValue();
								String id = photo.get("id").isString().stringValue();
								String secret = photo.get("secret").isString().stringValue();
								String url = "http://farm"+farm+".static.flickr.com/"+server+"/"+id+"_"+secret+"_"+"s.jpg";
								String bigUrl = "http://farm"+farm+".static.flickr.com/"+server+"/"+id+"_"+secret+".jpg";
						        String regImage = "<img src='"+url+"'/>";
						        String authImage = "<a href='"+bigUrl+"'>"+regImage+"</a>";	

						        String image = (auth == "true") ? authImage : regImage;
								html = html + image;
							}
							photoHTML.setHTML(html);	
						}
					}
				});
				
				// Ensure setlist is populated in case we got gere from a history event
				flickrUser.getPhotosetsJSON(new JSONRequestHandler() {
					public void onRequestComplete (JavaScriptObject json)
					{
						JSONObject jso = new JSONObject(json);
						JSONString stat = jso.get("stat").isString();

						if(stat.toString().equals("ok")){
							JSONString msg = jso.get("message").isString();
							Window.alert(msg.stringValue());
							photoHTML.setHTML("");
						}else{
							JSONObject photosets = jso.get("photosets").isObject();
							JSONArray photosetArray = photosets.get("photoset").isArray();

							setlist.clear();
							setlist.addItem(""); // add a blank set at the begining
							for(int i=0; i < photosetArray.size(); i++){
								JSONObject photoset = photosetArray.get(i).isObject();
								JSONString setId = photoset.get("id").isString();
								JSONString title = photoset.get("title").isObject().get("_content").isString();
								setlist.addItem(title.stringValue(), setId.stringValue());
							}
						}
						setlist.setEnabled(true);
					}
				});
			}
		});
		photoHTML.setVisible(true);	
	}

	private void fetchUserSets(String username){
		photoHTML.setHTML("Loading...");
		photoHTML.setVisible(true);	
		setlist.setEnabled(false);				
		setlist.clear();//Remove the current list if it is populated
		
		ws.getFlickrUser(username, null, new AsyncCallback<FlickrUser>() {
			@Override
			public void onFailure(Throwable caught)
			{
				Window.alert(caught.getMessage());
				photoHTML.setHTML("");
			}

			@Override
			public void onSuccess(FlickrUser flickrUser)
			{
				flickrUser.getPhotosetsJSON(new JSONRequestHandler() {
					public void onRequestComplete (JavaScriptObject json)
					{
						JSONObject jso = new JSONObject(json);
						JSONString stat = jso.get("stat").isString();

						if(stat.toString().equals("ok")){
							JSONString msg = jso.get("message").isString();
							Window.alert(msg.stringValue());
							photoHTML.setHTML("");
						}else{
							JSONObject photosets = jso.get("photosets").isObject();
							JSONArray photosetArray = photosets.get("photoset").isArray();

							setlist.clear();
							setlist.addItem(""); // add a blank set at the begining
							for(int i=0; i < photosetArray.size(); i++){
								JSONObject photoset = photosetArray.get(i).isObject();
								JSONString setId = photoset.get("id").isString();
								JSONString title = photoset.get("title").isObject().get("_content").isString();
								setlist.addItem(title.stringValue(), setId.stringValue());
							}
						}
						setlist.setEnabled(true);
						photoHTML.setHTML("Loaded "+(setlist.getItemCount() - 1)+" set(s).");
					}
				});

				photoHTML.setHTML(photoHTML.getHTML() + " This user has been quired " + flickrUser.getCount() + " times.");
			}
		});
		
	}

	// From http://developerlife.com/tutorials/?p=232 (modified slightly)
	public static HashMap<String,String> parseParamString(String string) {
		  String[] ray = string.substring(1, string.length()).split("&");
		  HashMap<String,String> map = new HashMap<String,String>();

		  for (int i = 0; i < ray.length; i++) {
		    String[] substrRay = ray[i].split("=");
		    if(substrRay.length > 1) map.put(substrRay[0], substrRay[1]);
		    else map.put(substrRay[0], null);
		  }
		  return map;
	}
}
