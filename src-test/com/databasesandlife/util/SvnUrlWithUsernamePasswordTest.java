package com.databasesandlife.util;

import com.databasesandlife.util.gwtsafe.ConfigurationException;

import junit.framework.TestCase;

public class SvnUrlWithUsernamePasswordTest extends TestCase {
    
    public void testParse() throws ConfigurationException {
        SvnUrlWithUsernamePassword withoutUsername = SvnUrlWithUsernamePassword.parse("http://www.google.com");
        assertEquals("http://www.google.com", withoutUsername.url.toString());
        
        SvnUrlWithUsernamePassword withUsername = SvnUrlWithUsernamePassword.parse("http://www.google.com|adrian|password");
        assertEquals("http://www.google.com", withUsername.url.toString());
        assertEquals("adrian", withUsername.username);
        assertEquals("password", withUsername.password);
        
        try { SvnUrlWithUsernamePassword.parse("http://www.google.com|adrian|password|something-else"); fail(); }
        catch (ConfigurationException e) { }
        
    }

}