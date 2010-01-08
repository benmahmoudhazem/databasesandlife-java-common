package com.databasesandlife.util;

import com.databasesandlife.util.StringsFile.StringInFile;
import junit.framework.TestCase;

/**
 * @author Adrian Smith
 */
public class StringsFileTest extends TestCase {
    
    public StringsFileTest(String testName) {
        super(testName);
    }

    public void test() {
        StringsFile str = new StringsFile();
        StringInFile a = str.newString("abc");
        StringInFile b = str.newString("foo\u20E0bar");
        assertEquals("abc",    a.toString());
        assertEquals("foo\u20E0bar", b.toString());
        try { a.append("x"); fail(); } catch (StringsFile.StringCannotBeAppendedException e) { }
        b.append("joe");
        assertEquals("foo\u20E0barjoe", b.toString());
    }
}
