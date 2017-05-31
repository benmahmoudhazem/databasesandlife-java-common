package com.databasesandlife.util;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Runs a number of {@link Runnable} tasks in a number of threads (over a number of CPU cores).
 *    <p>
 * Usage:
 * <pre>
 *      ThreadPool pool = new ThreadPool();
 *      pool.setThreadNamePrefix("foo"); // optional, for debugger output, default is class name
 *      pool.setThreadCount(5); // optional, default is number of CPU cores available
 *      pool.addTask(new Runnable() { ... }); // add one or more seed tasks;
 *      pool.execute(); // will start threads, execute the seed tasks, and execute any tasks they create
 * </pre>
 *    <p>
 * In the case that any task throws an exception, this exception is thrown by the {@link #execute()} method.
 * If all tasks run to completion, the {@link #execute()} method returns with no value.
 *    <p>
 * The difference to an {@link ExecutorService} is:
 * <ul>
 * <li>The processing of tasks can
 * add additional tasks to the queue (whereas to an {@link ExecutorService} the client adds a fixed number of tasks,
 * and they are then executed, without the executing tasks being able to add more tasks).
 * Such functionality is mandatory for web crawlers, which, during the processing of pages, discover links which
 * point to additional pages which require processing.<br><br>
 * <li>It is impossible to forget to "shutdown" a ThreadPool and cause a leakage of threads, as is easily possible with {@link ExecutorService}.
 * If the {@link #execute()} method is never called then no threads are ever started and the object can be garbage collected normally.
 * If the {@link #execute()} method is called then that method makes sure all threads it creates are destroyed.
 * </ul>
 *
 * @author This source is copyright <a href="http://www.databasesandlife.com">Adrian Smith</a> and licensed under the LGPL 3.
 * @see <a href="https://github.com/adrianmsmith/databasesandlife-java-common">Project on GitHub</a>
 */
public class ThreadPool {
    
    protected String threadNamePrefix = getClass().getSimpleName();
    protected int threadCount = Runtime.getRuntime().availableProcessors();
    protected final Queue<Runnable> tasks = new ArrayDeque<>();
    protected int unfinishedTaskCount = 0;
    protected Exception exceptionOrNull = null;
    
    protected class RunnerRunnable implements Runnable {
        @Override public void run() {
            while (true) {
                final Runnable nextTaskOrNull;
                synchronized (ThreadPool.this) {
                    if (unfinishedTaskCount == 0) break; // It's finished successfully
                    if (exceptionOrNull != null) break;  // It's failed, no point continuing
                    nextTaskOrNull = tasks.poll();
                }
                
                if (nextTaskOrNull != null) {
                    try {
                        nextTaskOrNull.run(); 
                    }
                    catch (Exception e) { 
                        synchronized (ThreadPool.this) { exceptionOrNull = e; }
                    }
                    finally {
                        synchronized (ThreadPool.this) { unfinishedTaskCount--; } 
                    }
                } else {
                    // it might be that other tasks are running, and they will produce lots more tasks
                    // so keep the thread alive and polling until all work is done.
                    try { Thread.sleep(10); }
                    catch (InterruptedException e) { }
                }
            }
        }
    }
    
    public void setThreadCount(int count) { threadCount = count; }
    public void setThreadNamePrefix(String prefix) { threadNamePrefix = prefix; }
    
    public synchronized void addTask(Runnable r) {
        tasks.add(r);
        unfinishedTaskCount++;
    }
    
    public void execute() {
        List<Thread> threads = IntStream.range(0, threadCount)
            .mapToObj(i -> new Thread(new RunnerRunnable(), threadNamePrefix+"-thread"+i))
            .collect(Collectors.toList());
        for (Thread t : threads) t.start();
        for (Thread t : threads) try { t.join(); } catch (InterruptedException e) { exceptionOrNull = e; }
        if (exceptionOrNull instanceof RuntimeException) throw (RuntimeException)exceptionOrNull;
        else if (exceptionOrNull != null) throw new RuntimeException(exceptionOrNull);
    }
}