package com.enice.wlan.selector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class AuthPortal {
	private static String LOGIN_TEST_URL = "http://www.baidu.com";
	private static String LOGIN_REQUEST_SIGNATURE = "cmcccs|login_req";
	private static String LOGIN_FORM_PATTERN = "<form.*?name=\"loginform\".*?action=\"(.*?)\".*?>(.*?)</form>";
	private static String LOGIN_INPUT_PATTERN =  "<input.*?name=\"(.*?)\".*?value=\"(.*?)\".*?>";
	private static String LOGIN_RESPONSE_CODE_PATTERN = "cmcccs\\|login_res\\|(.*?)\\|";
	private static String LOGOUT_RESPONSE_CODE_PATTERN = "cmcccs\\|offline_res\\|(.*?)\\|";
	
	private static AuthPortal instance = null;
	private String nextAction = "";
	private String user = "";
	private String password = "";
	private Pattern formPattern = null;
	private Pattern inputPattern = null;
	private Pattern loginCodePattern = null;
	private Pattern logoutCodePattern = null;
	
	private AuthPortal() {
		formPattern = Pattern.compile(LOGIN_FORM_PATTERN);
		inputPattern = Pattern.compile(LOGIN_INPUT_PATTERN);
		loginCodePattern = Pattern.compile(LOGIN_RESPONSE_CODE_PATTERN);
		logoutCodePattern = Pattern.compile(LOGOUT_RESPONSE_CODE_PATTERN);
	}
	
	public static AuthPortal getInstance() {
		if (instance == null) {
			instance = new AuthPortal();
		}
		return instance;
	}
	
	private String stream2String(InputStream istream) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(istream));
		StringBuilder total = new StringBuilder();
		String line;
		while ((line = r.readLine()) != null) {
		    total.append(line);
		}
		return total.toString();
	}
	
	private void parseAuthenPage(String output) {
		Matcher formMatcher = formPattern.matcher(output);
		if (formMatcher.find()) {
			StringBuffer action = new StringBuffer();
			action.append(formMatcher.group(1)).append("?USER=").append(user).append("&PWD=").append(password);
			Matcher inputMatcher = inputPattern.matcher(formMatcher.group(2));
			while (inputMatcher.find()) {
				String name = inputMatcher.group(1);
				String value = inputMatcher.group(2);
				action.append("&").append(name).append("=").append(value);
			}
			nextAction = action.toString();
		}
	}
	
	public boolean login(String user, String password) {
		this.user = user;
		this.password = password;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(new HttpGet(LOGIN_TEST_URL));
			String output = stream2String(response.getEntity().getContent());
			
			if (output.contains(LOGIN_TEST_URL)) {
				Log.i("WLANSelector", "Already loginned!");
				return true;
			} 
			
			if (output.contains(LOGIN_REQUEST_SIGNATURE)) {
				parseAuthenPage(output);
				response = client.execute(new HttpPost(nextAction));
				output = stream2String(response.getEntity().getContent());
				Matcher codeMatcher = loginCodePattern.matcher(output);
				if (codeMatcher.find()) {
					int code = Integer.valueOf(codeMatcher.group(1));
					if (code == 0) {
						parseAuthenPage(output);  // prepare action parameters for logout
						Log.i("WLANSelector", "Login success!");
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean logout() {
		try {
			HttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(new HttpPost(nextAction));
			String output = stream2String(response.getEntity().getContent());
			Matcher codeMatcher = logoutCodePattern.matcher(output);
			if (codeMatcher.find()) {
				int code = Integer.valueOf(codeMatcher.group(1));
				if (code == 0) {
					Log.i("WLANSelector", "Logout success!");
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
