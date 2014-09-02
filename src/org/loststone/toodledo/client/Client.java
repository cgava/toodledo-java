package org.loststone.toodledo.client;

import java.io.IOException;
import java.util.List;

import org.loststone.toodledo.ToodledoApi;
import org.loststone.toodledo.ToodledoApiImpl;
import org.loststone.toodledo.data.Folder;
import org.loststone.toodledo.data.Goal;
import org.loststone.toodledo.data.Todo;
import org.loststone.toodledo.exception.IncorrectUserPasswordException;
import org.loststone.toodledo.exception.MissingPasswordException;
import org.loststone.toodledo.exception.ToodledoApiException;
import org.loststone.toodledo.request.Request;
import org.loststone.toodledo.util.AuthToken;

/**
 * Class that opens a connection to www.toodledo.com and does some simple
 * queries. 
 * @author lant
 */
public class Client {

	private String email;
	private String userid; 
	private String password; 
	private AuthToken token; 
	private ToodledoApi tdApi; 
	
	/**
	 * Creator.
	 * @param email E-mail of the user to test.
	 * @param password 
	 */
	public Client(String email, String password) throws IOException{
		this.email = email; 
		this.password = password; 
		tdApi = new ToodledoApiImpl(Request.DEFAULT_STUB_FILENAME);
	}
	
	/**
	 * Connects to www.toodledo.com.
	 * It uses the email and password provided to get the userid and then
	 * get the AuthToken.
	 * @return
	 */
	public boolean connect() {
		try {
			this.userid = tdApi.getUserId(email, password);
			this.token = tdApi.initialize(userid, password);
		} catch (ToodledoApiException e) {
			System.out.println("Could not connect to http://www.toodledo.com");
			e.printStackTrace();
			return false; 
		} catch (IncorrectUserPasswordException e) {
			e.printStackTrace();
			return false; 
		} catch (MissingPasswordException e) {
			e.printStackTrace();
			return false; 
		}
		return true;
	}


	/**
	 * Gets the list of all todos and prints their id and their title.
	 */
	public void getTodos() {
		try {
			List<Todo> todolist = tdApi.getTodosList(token,null);
			System.out.println("Found "+todolist.size()+" todos:");
			for (Todo _tmp : todolist) {
				System.out.println("  ["+_tmp.getId()+"] - "+_tmp.getTitle());
			}
		} catch (ToodledoApiException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the list of all todos and prints their id and their title.
	 */
	public void storeProperties() {
		try {
			tdApi.storeProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the list of all goals and prints their id and their name.
	 */
	private void getGoals() {
		try {
			List<Goal> goalsList = tdApi.getGoals(token);
			System.out.println("Found "+goalsList.size()+" goals:");
			for (Goal _tmp : goalsList) {
				System.out.println("  ["+_tmp.getId()+"] - "+_tmp.getName());
			}
		} catch (ToodledoApiException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the list of all folders and prints their id and name.
	 */
	private void getFolders() {
		try {
			List<Folder> foldersList = tdApi.getFolders(token);
			System.out.println("Found "+foldersList.size()+" folders:");
			for (Folder _tmp : foldersList) {
				System.out.println("  ["+_tmp.getId()+"] - "+_tmp.getSName());
			}
		} catch (ToodledoApiException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws IOException{
		String email = null; 
		String password = null; 
		
		//Console cons = System.console();
		//if (cons == null) {
		//	System.out.println("Couldn't get System Console. Exiting.");
		//	System.exit(1);
		//}
		
		System.out.println(":: Toodledo Java API example ::");
		System.out.println("\nHey there!");
		System.out.println("\nThis program is a simple tutorial/demonstration of how to "+
				"use the toodledo java API. To keep it simple it just offers access "+
				"to three rather simple read only methods. But you'll get an idea "+
				"of how it works.");
		System.out.println("\nLibrary home: http://github.com/lant/toodledo-java");
		
		// ask for username and password. 
		//System.out.print("\nToodledo user e-mail: ");
		email = "gava.c@free.fr";
		//System.out.print("Toodledo password for "+email+ " (won't be visible): ");
		password = "hj9kir77";
		
		Client c = new Client(email, password);
		if (c.connect()) {
			c.getTodos();
			c.storeProperties();
		}
	}

}
