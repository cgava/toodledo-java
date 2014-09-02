package org.loststone.toodledo.request;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.loststone.toodledo.data.Todo;
import org.loststone.toodledo.exception.ToodledoApiException;
import org.loststone.toodledo.response.GetTodosResponse;
import org.loststone.toodledo.response.Response;
import org.loststone.toodledo.util.AuthToken;

public class GetTodosRequest extends Request {
	private String stubFilename;


	public GetTodosRequest(AuthToken token, Todo filter, String filename) throws ToodledoApiException {
		super();
		this.stubFilename = filename;
		this.url = "http://api.toodledo.com/api.php?method=getTasks;key="+token.getKey();
		if (filter != null) {
			
		}
	}

	/**
	 * surcharge de la methode exec de Request pour choisir si les todo sont accedées soit par
	 *  - http (cas nominal)
	 *  - dans un fichier passé en parametre (pour les tests en local par exemple) 
	 */
	@Override
    public void exec() {
		if (this.stubFilename == null) {
			execOverHttp();
		} else {
			execOnLocalFile();
		}
		System.out.println("xmlResponse = " + this.xmlResponse);
	}

	/**
	 * restaurer la requete a partir d'un fichier XML
	 */
	public void execOnLocalFile() {
		try {
			this.xmlResponse = FileUtils.readFileToString(new File(this.stubFilename), "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * executer la requete via http
	 */
	public void execOverHttp() {
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(this.url);
		try {
			client.executeMethod(method);
			InputStream in = method.getResponseBodyAsStream();
			this.xmlResponse = IOUtils.toString(in, "UTF-8");
			
			//TODO : Ajouter un sélecteur booleen pour activer/desactiver la sauvegarde de la requete en xml
			FileUtils.writeStringToFile(new File(Request.DEFAULT_STUB_FILENAME),this.xmlResponse);
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			method.releaseConnection();
		}
	}

	@Override
	public Response getResponse() {
		this.exec();
		GetTodosResponse response = new GetTodosResponse(this.xmlResponse);
		return response;
	}
	
}
