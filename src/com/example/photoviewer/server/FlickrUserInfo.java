package com.example.photoviewer.server;

import javax.persistence.*;


import com.example.photoviewer.client.FlickrUser;

@Entity(name = "FlickrUserInfo")
public class FlickrUserInfo
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String userId;
	private String username;
	private int count;

	public FlickrUserInfo (String userId)
	{
		this.userId = userId;
	}
	
	public FlickrUserInfo(FlickrUser fUser){
		FlickrUserInfo fui;
		String username = fUser.getUsername();
		EntityManager em = EMF.get().createEntityManager();
		
		EntityTransaction ts = em.getTransaction();
        try {
            ts.begin();
            Query q = em.createQuery("select u from FlickrUserInfo u where username='" + username + "'");
  		    fui = (FlickrUserInfo)q.getSingleResult();
            ts.commit();
            this.userId = fui.getUserId();
  		  	this.username = fui.getUsername();
  			this.count = fui.count + 1;
  			System.out.println("Count = " + this.count);
        } catch (javax.persistence.NoResultException e){
  		      this.username = fUser.getUsername();
  		      this.count = 1;
  		} catch (Exception e) {
            ts.rollback();
            e.printStackTrace();
        } finally {
        	if(ts.isActive()){
        		ts.rollback();
        	}
            em.close();
        } 
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	

	public void save()
	{
		EntityManager em = EMF.get().createEntityManager();
		try {
			em.persist(this);
		}
		finally {
			em.close();
		}
	}
}
