package com.ares.urlshortening.utils;

import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import static nl.basjes.parse.useragent.UserAgent.*;


public class RequestUtils {

    public static String getIpAddress(HttpServletRequest request){

        String ipAddress = "Unknown IP";

        if (request!=null){
            ipAddress= request.getHeader("X-FORWARDED-FROM");
            if (ipAddress==null || "".equals(ipAddress)){
                ipAddress = request.getRemoteAddr();
            }
        }
        return ipAddress;
    }

    public static String getDevice(HttpServletRequest request){
        UserAgentAnalyzer userAgentAnalyzer = UserAgentAnalyzer
                .newBuilder()
                .hideMatcherLoadStats()
                .withCache(1000)
                .build();
        UserAgent agent = userAgentAnalyzer.parse(request.getHeader("user-agent"));
        return agent.getValue(DEVICE_NAME) + " [" + agent.getValue(OPERATING_SYSTEM_NAME)+"]";
    }

    public static String getBrowser(HttpServletRequest request){
        UserAgentAnalyzer ua = UserAgentAnalyzer.newBuilder()
                .hideMatcherLoadStats().withCache(1000).build();
        UserAgent agent = ua.parse(request.getHeader("user-agent"));
        return agent.getValue(UserAgent.AGENT_NAME);

    }

}
