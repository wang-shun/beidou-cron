/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.api.GoogleApiHelper.java
 * 下午9:40:12 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google.api;

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets.Details;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.adexchangebuyer.Adexchangebuyer;
import com.google.api.services.adexchangebuyer.AdexchangebuyerScopes;

/**
 * Google adx api调用类
 * 
 * @author kanghongwei
 * @fileName GoogleApiHelper.java
 * @dateTime 2013-10-22 下午9:40:12
 */

public class GoogleApiHelper {

	private static final Log log = LogFactory.getLog(GoogleApiHelper.class);

	public static GoogleCredential credential;

	private static HttpTransport httpTransport;

	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	private static String CLIENT_ID;

	private static String CLIENT_SECRET;

	private static String REFRESH_TOKEN;

	private static String APPLICATION_NAME;

	/**
	 * 该方法只在应用程序上线前执行一次，用于获取google adx api的refreshToken
	 * 
	 * @throws Exception
	 */
	public static void printRefreshToken() {

		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();

			Details details = new Details();
			details.setClientId(CLIENT_ID);
			details.setClientSecret(CLIENT_SECRET);
			GoogleClientSecrets clientSecrets = new GoogleClientSecrets();
			clientSecrets.setWeb(details);

			GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, Collections.singleton(AdexchangebuyerScopes.ADEXCHANGE_BUYER)).setAccessType("offline").setApprovalPrompt("auto").build();

			Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

			if (credential != null) {
				log.info("The current refreshToken is: " + credential.getRefreshToken());
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("printRefreshToken error, " + e.getMessage());
		}
	}

	/**
	 * GoogleApiHelper对外提供Adexchangebuyer的主方法
	 * 
	 * @return
	 */
	public static Adexchangebuyer getAdexchangebuyerClient() {
		Adexchangebuyer client = null;
		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();

			Credential credential = getCredentialByRefreshToken();

			client = new Adexchangebuyer.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

		} catch (Exception e) {
			client = null;
			e.printStackTrace();
			log.error("getAdexchangebuyerClient error, " + e.getMessage());
		}
		return client;
	}

	public static Credential getCredentialByRefreshToken() {
		if ((credential == null) || (credential.getExpiresInSeconds() == null) || (credential.getExpiresInSeconds() <= 0)) {
			try {
				GoogleTokenResponse response = new GoogleRefreshTokenRequest(httpTransport, JSON_FACTORY, REFRESH_TOKEN, CLIENT_ID, CLIENT_SECRET).execute();
				credential = new GoogleCredential().setFromTokenResponse(response);
			} catch (IOException e) {
				e.printStackTrace();
				log.error("getCredentialByRefreshToken error, " + e.getMessage());
			}
		}
		return credential;
	}

	public void setAPPLICATION_NAME(String aPPLICATION_NAME) {
		APPLICATION_NAME = aPPLICATION_NAME;
	}

	public void setCLIENT_ID(String cLIENT_ID) {
		CLIENT_ID = cLIENT_ID;
	}

	public void setCLIENT_SECRET(String cLIENT_SECRET) {
		CLIENT_SECRET = cLIENT_SECRET;
	}

	public void setREFRESH_TOKEN(String rEFRESH_TOKEN) {
		REFRESH_TOKEN = rEFRESH_TOKEN;
	}

}
