package com.onecashye.web.security.helper;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MatcherHelper {

	private static String match(String text,Map<String,String> replacements) {
		
		Pattern pattern = Pattern.compile("\\{(.+?)\\}");
		Matcher matcher = pattern.matcher(text);
	
		StringBuilder builder = new StringBuilder();
		
		int i = 0;
		while (matcher.find()) {
		    String replacement = replacements.get(matcher.group(1));
		    builder.append(text.substring(i, matcher.start()));
		    if (replacement == null)
		        builder.append(matcher.group(0));
		    else
		        builder.append(replacement);
		    i = matcher.end();
		}
		builder.append(text.substring(i, text.length()));
		return new String(builder);
	}
	
	public static String convert(Map<String,Object> jsonRequest,String xmlBody) {
		
		try {
			
			//Convert Map<String,Object> to Map<String,String>
			
			Map<String,String> newJson = jsonRequest.entrySet().stream()
				     .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toString()));
			
			return MatcherHelper.match(xmlBody,newJson);
		}
		catch(Exception e) {
			
			e.printStackTrace();
			return null;
		}
	}
}
