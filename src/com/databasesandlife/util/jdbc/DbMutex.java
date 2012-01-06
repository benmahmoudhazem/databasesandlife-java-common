package com.databasesandlife.util.jdbc;

/** 
 * Maintains a table full of mutexes as described
 * <a href="http://www.databasesandlife.com/mysql-lock-tables-does-an-implicit-commit/">here</a>
 *    <p>
 * To use this, create this table:
 * <pre>
 *   CREATE TABLE mutex (name VARCHAR(100)) ENGINE=InnoDB;
 * </pre> 
 */
public class DbMutex {
    
    public String name;
    
    public DbMutex(String name) { this.name = name; }
    
    public void acquire(DbClient db) {
        try { db.doSqlAction("INSERT INTO mutex SET name=?", name); }
        catch (DbClient.UniqueConstraintViolation e) { }
        
        db.doSqlQuery("SELECT * FROM mutex WHERE name=? FOR UPDATE", name);
    }

}