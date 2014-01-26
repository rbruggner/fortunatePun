package com.fortunatePun;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.*;


public class EnqueueTweetProcessor extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5617507030940714830L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException{
		Queue queue = QueueFactory.getDefaultQueue();
		String twitterId = request.getParameter("twitterId");
		queue.add(withUrl("/eatTweets").param("twitterId", twitterId).method(Method.GET));
		Map<String,String[]> params = request.getParameterMap();
		if (params.keySet().contains("twitterHandle")){
			try {
				response.sendRedirect("http://www.lastwhalestanding.com/t/"+params.get("twitterHandle")[0]);
			} catch (IOException e) {
				System.err.println("Couldn't get twitterHandle: "+ e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
}