package org.loststone.toodledo;

import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;

import org.loststone.toodledo.data.Context;
import org.loststone.toodledo.data.Folder;
import org.loststone.toodledo.data.Goal;
import org.loststone.toodledo.data.Todo;
import org.loststone.toodledo.exception.IncorrectUserPasswordException;
import org.loststone.toodledo.exception.MissingPasswordException;
import org.loststone.toodledo.exception.ToodledoApiException;
import org.loststone.toodledo.request.AddContextRequest;
import org.loststone.toodledo.request.AddFolderRequest;
import org.loststone.toodledo.request.AddGoalRequest;
import org.loststone.toodledo.request.AddTodoRequest;
import org.loststone.toodledo.request.AuthorizeRequest;
import org.loststone.toodledo.request.DeleteFolderRequest;
import org.loststone.toodledo.request.DeleteTodoRequest;
import org.loststone.toodledo.request.GetContextsRequest;
import org.loststone.toodledo.request.GetFoldersRequest;
import org.loststone.toodledo.request.GetGoalsRequest;
import org.loststone.toodledo.request.GetTodosRequest;
import org.loststone.toodledo.request.GetUserIdRequest;
import org.loststone.toodledo.request.ModifyTodoRequest;
import org.loststone.toodledo.request.Request;
import org.loststone.toodledo.response.AddContextResponse;
import org.loststone.toodledo.response.AddFolderResponse;
import org.loststone.toodledo.response.AddGoalResponse;
import org.loststone.toodledo.response.AddTodoResponse;
import org.loststone.toodledo.response.AuthorizeResponse;
import org.loststone.toodledo.response.GenericDeleteResponse;
import org.loststone.toodledo.response.GetContextsResponse;
import org.loststone.toodledo.response.GetFoldersResponse;
import org.loststone.toodledo.response.GetGoalsResponse;
import org.loststone.toodledo.response.GetTodosResponse;
import org.loststone.toodledo.response.GetUserIdResponse;
import org.loststone.toodledo.response.ModifyTodoResponse;
import org.loststone.toodledo.util.AuthToken;
import org.loststone.toodledo.xml.ContextsParser;
import org.loststone.toodledo.xml.FolderParser;
import org.loststone.toodledo.xml.GetTodosParser;
import org.loststone.toodledo.xml.GetUserIdParser;
import org.loststone.toodledo.xml.GoalsParser;


public class ToodledoApiImpl implements ToodledoApi {
	
	//default properties filename//
	private static final int MIN_TOKEN_AGE = 240;                                //CGAVA : minimum TOKEN age, in seconds. If token age is less than this value, then the token is recreated
                                                                                 //CGAVA : create token attribute for passing tokent between methods
	private String stubFilename;
	
	private Preferences rootPrefs;
	//private FileInputStream propFile = null;                           

	//Constructor that reads the property file
	/**
	 * Constructor
	 * @param propFilename filename of the properties file
	 * @param stubFilename filename of an xml file used to stub the response for the given request (useful for testing)
	 * @throws IOException
	 */
	public ToodledoApiImpl(String stubFilename) throws IOException{     
		this.stubFilename = stubFilename;
		rootPrefs = Preferences.userNodeForPackage( ToodledoApiImpl.class );

	}
	

	

	

	public int addTodo(AuthToken auth, Todo todo) throws ToodledoApiException {
		AddTodoRequest request = new AddTodoRequest(auth, todo);
		AddTodoResponse resp = (AddTodoResponse) request.getResponse();
		if (resp.succeeded())
			return Integer.parseInt(resp.getResponseContent());
		else
			return -1;
	}

	public Todo getTodo(AuthToken auth, int id) throws ToodledoApiException {
		Todo filter = new Todo();
		filter.setId(id);
		List<Todo> res = getTodosList(auth,filter);
		if (res != null && res.size() > 0) {
			return res.get(0);
		} else {
			return null;
		}
	}

	public List<Todo> getTodosList(AuthToken auth) throws ToodledoApiException {
		return getTodosList(auth,null);
	}
	
	public List<Todo> getTodosList(AuthToken auth, Todo filter) throws ToodledoApiException {
		Request getTodosRequest = new GetTodosRequest(auth, filter, this.stubFilename);
		GetTodosResponse response = (GetTodosResponse)getTodosRequest.getResponse();
		if (response.succeeded())
			return new GetTodosParser(response.getXmlResponseContent()).getTodos();
		else
			return null;
	}
	
	public AuthToken initialize(String userid, String password, String appid) throws ToodledoApiException {		
		Preferences subnode = rootPrefs.node(userid);
		String tokenStr=subnode.get("token", null);
		

		if (this.stubFilename != null) {
			if(tokenStr==null) {
				subnode.put("token", "NOTOKENWHENSTUB");
				tokenStr=subnode.get("token", null);
			}
			return new AuthToken (userid,password,tokenStr);
		}
		AuthToken token= tokenStr!=null? new AuthToken(tokenStr) : null;
		if (token!=null){
			if (token.getRemainingTime() > MIN_TOKEN_AGE) {
				System.out.println("OK token remaining time is " + token.getRemainingTime());
				return token;
			}
		} 
		Request initReq = new AuthorizeRequest(userid,appid);
		// response gives back the token, now create the AuthToken
		AuthorizeResponse resp = (AuthorizeResponse) initReq.getResponse();
		token = new AuthToken(password, userid, resp.getResponseContent());
		subnode.put("token", token.toString());
		
		return token;
	}

	public AuthToken initialize(String userid, String password) throws ToodledoApiException {
		return initialize(userid,password,null);
	}

	public boolean modifyTodo(AuthToken auth, Todo newOne)  throws ToodledoApiException{
		ModifyTodoRequest modifyRequest = new ModifyTodoRequest(auth,newOne);
		ModifyTodoResponse resp = (ModifyTodoResponse)modifyRequest.getResponse();
		if (resp.succeeded()) {
			Integer _t = Integer.parseInt(resp.getResponseContent());
			if (_t == 1) return true;
			else return false;
		} else
			return false;
	}

	public boolean deleteTodo(AuthToken auth, int id)  throws ToodledoApiException{
		DeleteTodoRequest request = new DeleteTodoRequest(auth, id);
		GenericDeleteResponse resp = (GenericDeleteResponse)request.getResponse();
		if (resp.succeeded()) {
			Integer _t = Integer.parseInt(resp.getResponseContent());
			if (_t == 1) return true;
			else return false;
		} else
			return false;
	}

	
	public List<Context> getContexts(AuthToken auth)  throws ToodledoApiException{
		GetContextsRequest request = new GetContextsRequest(auth);
		GetContextsResponse resp = (GetContextsResponse)request.getResponse();
		if (resp.succeeded())
			return new ContextsParser(resp.getXmlResponseContent()).getContexts();
		else
			return null;
	}

	public List<Folder> getFolders(AuthToken auth)  throws ToodledoApiException{
		GetFoldersRequest request = new GetFoldersRequest(auth);
		GetFoldersResponse resp = (GetFoldersResponse)request.getResponse();
		if (resp.succeeded())
			return new FolderParser(resp.getXmlResponseContent()).getFolders();
		else
			return null;
	}

	public List<Goal> getGoals(AuthToken auth)  throws ToodledoApiException{
		GetGoalsRequest request = new GetGoalsRequest(auth);
		GetGoalsResponse resp = (GetGoalsResponse)request.getResponse();
		if (resp.succeeded())
			return new GoalsParser(resp.getXmlResponseContent()).getGoals();
		else
			return null;
	}

	public int addFolder(AuthToken auth, Folder fold)
			throws ToodledoApiException {
		AddFolderRequest request = new AddFolderRequest(auth,fold);
		AddFolderResponse response = (AddFolderResponse)request.getResponse();
		if (response.succeeded())
			return Integer.parseInt(response.getResponseContent());
		else
			return -1;
	}

	public int addContext(AuthToken auth, Context context)
			throws ToodledoApiException {
		AddContextRequest request = new AddContextRequest(auth, context);
		AddContextResponse response = (AddContextResponse)request.getResponse();
		if (response.succeeded())
			return Integer.parseInt(response.getResponseContent());
		else
			return -1;
	}

	public int addGoal(AuthToken auth, Goal goal) throws ToodledoApiException {
		AddGoalRequest request = new AddGoalRequest(auth, goal);
		AddGoalResponse response = (AddGoalResponse)request.getResponse();
		if (response.succeeded())
			return Integer.parseInt(response.getResponseContent());
		else
			return -1;
	}

	public String getUserId(String mail, String password)
			throws ToodledoApiException, IncorrectUserPasswordException, MissingPasswordException {
		
		if (this.stubFilename == null) {
			GetUserIdRequest request = new GetUserIdRequest(mail,password);
			GetUserIdResponse response = (GetUserIdResponse)request.getResponse();
			if(response.succeeded()) 
				return new GetUserIdParser(response.getXmlResponseContent()).getUserId();
			else
				return null;
		} else {
			return "stubUserId";
		}
	}

	public boolean deleteFolder(AuthToken auth, int folderId)
			throws ToodledoApiException {
		DeleteFolderRequest request = new DeleteFolderRequest(auth, folderId);
		GenericDeleteResponse resp = (GenericDeleteResponse)request.getResponse();
		if (resp.succeeded()) {
			Integer _t = Integer.parseInt(resp.getResponseContent());
			if (_t == 1) return true;
			else return false;
		} else
			return false;
		
	}
	

}
