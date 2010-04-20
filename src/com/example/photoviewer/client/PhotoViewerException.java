package com.example.photoviewer.client;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PhotoViewerException extends java.lang.Exception
implements IsSerializable
{
	private static final long serialVersionUID = 4740975800356479306L;

	String message;

	public PhotoViewerException(Exception e)
	{
		message = e.getMessage();

	}

	public PhotoViewerException()
	{
	}

	public PhotoViewerException(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}

