package com.databasesandlife.util.emailtemplatetest;

import com.databasesandlife.util.EmailTemplate;

/**
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 */
public class MyEmailTemplate extends EmailTemplate {

    public MyEmailTemplate() {
        super(MyEmailTemplate.class.getPackage());
    }
}
