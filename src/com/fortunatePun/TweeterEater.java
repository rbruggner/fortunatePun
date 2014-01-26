package com.fortunatePun;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.auth.AccessToken;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TweeterEater extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	DBConnection dbc;
	CallableStatement urlCheck;
	CallableStatement urlInsert;
	CallableStatement urlerInsert;
	CallableStatement urlerTokenInsert;
	CallableStatement urlerMaxTweetId;
	
	
	public TweeterEater(){
		this.dbc = new DBConnection();
		this.dbc.connect();
		 try {
			this.urlCheck = this.dbc.connection.prepareCall("select urlid from URL where t_url=?;");
			this.urlInsert = this.dbc.connection.prepareCall("insert into URL (t_url,url) values (?,?);");
			this.urlerInsert = this.dbc.connection.prepareCall("insert into URLer(urlid,twitter_id,twitter_handle,tweetid,tweet_time) values (?,?,?,?,?);");
			this.urlerTokenInsert = this.dbc.connection.prepareCall("select oauth_token, oauth_token_secret from tokens where twitter_id=?;");
			this.urlerMaxTweetId = this.dbc.connection.prepareCall("select max(tweetid) from URLer where twitter_id=?;");
		} catch (SQLException e) {
			System.err.println("Can't prepare database connection:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        long twitterId = (long) Long.parseLong(request.getParameter("twitterId"));
        System.out.println("TwitterID:" + twitterId);
        
        // Get user oauth credentials:
        String[] oauth_tokens = this.getToken(twitterId);
        
        try {
        	TwitterFactory factory = new TwitterFactory();
        	AccessToken accessToken = new AccessToken(oauth_tokens[0],oauth_tokens[1]);
        	Twitter twitter = factory.getInstance();
            twitter.setOAuthAccessToken(accessToken);

        	User user = twitter.verifyCredentials();
            
        	// Check to get max tweet id
        	this.urlerMaxTweetId.setLong(1, user.getId());
        	ResultSet rs = this.urlerMaxTweetId.executeQuery();
        	Long maxId=0L;
        	while (rs.next()){
        		maxId = rs.getLong(1);
        	}
        	maxId++;
        	//
        	System.out.println("Max Tweet ID:" + maxId);
        	List<Status> statuses = twitter.getHomeTimeline(new Paging(1,200).sinceId(maxId));
        	System.out.println("Number of statusts:" + statuses.size());
        	
            //System.out.println("Showing @" + user.getScreenName() + "'s home timeline.");
            
            for (Status status : statuses) {
                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                for (URLEntity u : status.getURLEntities()){
                	System.out.println("\t"+u.getURL());
                	long urlId = getOrInsertURL(u.getURL(),u.getExpandedURL());
                	System.out.println("Got URL ID:" + urlId);
                	this.insertHandleURLPair(urlId, user.getId(),status.getUser().getScreenName(),status.getId(),status.getCreatedAt());
                }
                System.out.println("");
            }
            response.sendRedirect("http://able-inn-471.appspot.com/t/"+twitter.verifyCredentials().getScreenName());
        } catch (Exception e){
        	System.err.println("Failed:" + e.getMessage());
        	e.printStackTrace();
        }
        
        
    }
    
    private long getOrInsertURL(String ticoURL,String url) throws Exception{
    	// TO DO: Expand shortened URLS
    	System.out.println("Checking for " + ticoURL);
    	this.urlCheck.setString(1, ticoURL);
    	ResultSet urlIdPresent = this.urlCheck.executeQuery();
    	urlIdPresent.last();
    	if (urlIdPresent.getRow()==0){
    		System.out.println("Inserting " + ticoURL + ": " + url);
    		this.urlInsert.setString(1, ticoURL);
    		this.urlInsert.setString(2, url);
    		this.urlInsert.executeUpdate();
    	}
    	this.urlCheck.setString(1,ticoURL);
    	
    	long urlId = -1;
    	ResultSet rs = this.urlCheck.executeQuery();
    	while (rs.next()){
    		urlId = rs.getInt("urlid");
    	}    
    	return(urlId);
    }
    
	private void insertHandleURLPair(long urlid, long twitter_id,String twitterHandle,long tweetid,Date createdAt) throws Exception{
		//insert into URLer(urlid,twitter_id,twitter_handle,tweetid,tweet_time) values (?,?,?,?,?);
    	this.urlerInsert.setLong(1, urlid);
    	this.urlerInsert.setLong(2,twitter_id);
    	this.urlerInsert.setString(3,twitterHandle);
    	this.urlerInsert.setLong(4, tweetid);
    	this.urlerInsert.setTimestamp(5, new java.sql.Timestamp(createdAt.getTime()));
    	this.urlerInsert.executeUpdate();
    }
    
    private String[] getToken(long twitterId){
    	String[] tokens = new String[2];
    	try {
			this.urlerTokenInsert.setLong(1,twitterId);
			ResultSet rs = this.urlerTokenInsert.executeQuery();
			while (rs.next()){
				tokens[0] = rs.getString("oauth_token");
				tokens[1] = rs.getString("oauth_token_secret");
			}
			rs.close();
		} catch (SQLException e) {
			System.err.println("Couldn't query:" + e.getMessage());
			e.printStackTrace();
		}
    	return(tokens);
    }
    
}
