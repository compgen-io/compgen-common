package io.compgen.common.updates;

import io.compgen.common.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * This class will retrieve a URL that should contain a line for each "project" to check version numbers.
 * This lets us check to see if the user is using the current version of a program. There will be a limit
 * on the number of times that this is checked within a time-frame (managed by a user-local temp file).
 * 
 * Each line is tab delimited and should contain the following:
 * 
 * project\tbuild\tversion\tcomment about version (code name, date, etc...)
 * 
 * "project" should be a project name
 * "build" is the program track to follow. Examples would be "stable", "testing", "dev", etc...
 * "version" should be a version string in the following format: major.minor.increment where
 * 			 major, minor, and increment are numbers, followed by (optional) an alphabet characters.
 * 
 *           NOTE: this value is not parsed. It is compared as-is (query.equals(version))
 *
 * This check can be overridden with an environmental variable. The default variable is "CG_NO_PHONE_HOME".
 * It doesn't have to be set to anything specific - any setting will block the phone home.
 * 
 * @author mbreese
 *
 */
public class UpdateCheck {
	static private final int MAX_ATTEMPTS = 3;

	private final String url;
	private final String envName;
	private int attempts = 0;
	
	private Map<String, Map<String, Pair<String, String>>> versions = null;
	
	// these are phone-home URL parameters to track (command used, OS, etc...)
	private Map<String, String> values = new HashMap<String, String>();
	
	public UpdateCheck(String url, String envName) {
		this.url = url;
		this.envName = envName;
	}

	public UpdateCheck(String url) {
		this(url, null);
	}

	protected boolean noPhoneHome() {
		if (envName != null) {
			String check = System.getenv(envName);
			return (check == null) ? false: true;
		}
		return false;
	}

	public void setValue(String key, String value) {
		values.put(key,  value);
	}
	
	protected String buildQuery() {
		String ret = "";
		
		for (String k: values.keySet()) {
			if (!ret.equals("")) {
				ret += "&";
			}
			try {
				ret += URLEncoder.encode(k, "UTF-8");
				ret += "=";
				ret += URLEncoder.encode(values.get(k), "UTF-8");
			} catch (UnsupportedEncodingException e) {
			}
		}
		
		return ret;
	}
	
	protected void load() throws IOException {
		if (versions != null) {
			return;
		}
		
		attempts++;
		
		if (attempts > MAX_ATTEMPTS) {
			return;
		}
		
		versions = new HashMap<String, Map<String, Pair<String, String>>>();
		
		try {
			URL urlGet = new URL(url+"?"+buildQuery());
			BufferedReader reader = new BufferedReader(new InputStreamReader(urlGet.openStream()));
			String line=null;
			while ((line = reader.readLine()) != null) {
				String[] spl = line.split("\t");
				
				if (!versions.containsKey(spl[0])) {
					versions.put(spl[0], new HashMap<String, Pair<String, String>>());
				}
				
				if (spl.length == 4) {
					versions.get(spl[0]).put(spl[1], new Pair<String, String>(spl[2], spl[3]));
				} else if (spl.length == 3) {
					versions.get(spl[0]).put(spl[1], new Pair<String, String>(spl[2], ""));
				} else {
					// bad version file... ignore...
				}

			}
		} catch (IOException e) {
			versions = null;
			// try again
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
			load();
		}
	}
	
	public boolean isCurrentVersion(String project, String build, String version) {
		String currentVersion = getCurrentVersion(project, build);
		if (currentVersion == null) {
			return true;
		}

		String current = versions.get(project).get(build).one;

		if (!current.equals(version)) {
			return false;
		}
		return true;
	}

	
	public String getCurrentVersion(String project, String build) {
		if (noPhoneHome()) {
			return null;
		}

		try {
			load();
		} catch (IOException e) {
			return null;
		}

		if (versions == null) {
			return null;
		}
		
		if (!versions.containsKey(project) || !versions.get(project).containsKey(build)) {
			return null;
		}
		
		return versions.get(project).get(build).one;
	}
	public String getCurrentVersionDescription(String project, String build) {
		if (noPhoneHome()) {
			return null;
		}

		try {
			load();
		} catch (IOException e) {
			return null;
		}

		if (versions == null) {
			return null;
		}
		
		
		if (!versions.containsKey(project) || !versions.get(project).containsKey(build)) {
			return null;
		}
		
		
		return versions.get(project).get(build).two;
	}
}
