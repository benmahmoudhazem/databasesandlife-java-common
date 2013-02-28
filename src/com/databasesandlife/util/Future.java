package com.databasesandlife.util;

import java.util.Iterator;

/**
 * A future is the response of a calculation which is done in the background (in a thread).
 *     <p>
 * A future has one method which is get which waits for the calculation to complete, and returns the result.
 * The client must create a subclass and implement the method populate which will be run in the thread.
 *     <p>
 * The author was not satisfied with the simplicity of the JVM-supplied Future object.
 */
public abstract class Future<T> {

    T result = null;
    RuntimeException exception = null;
    Thread thread;
    
    /** Calculate the result and return it. Must not return null. */
    public abstract T populate();
    
    public static class FutureComputationTimedOutException extends Exception { }
    
    public Future() {
        thread = new Thread(new Runnable() {
            @Override public void run() { 
                try { result = populate(); }
                catch (RuntimeException e) { exception = e; }
            }
        });
        thread.start();
    }
    
    /** Same as {@link #get()} but times out after 'seconds' seconds. */
    public T getOrTimeoutAfterSeconds(float seconds) throws FutureComputationTimedOutException {
        try { thread.join((int) (1000000 * seconds)); }
        catch (InterruptedException e) { throw new RuntimeException(e); }
        
        if (result == null && exception == null) throw new FutureComputationTimedOutException();
        
        if (exception != null) throw new RuntimeException(exception); // wrap exception to preserve its stack backtrace
        return result;
    }
    
    /** Returns the object, waiting for its computation to be completed if necessary. */
    public T get() {
        try { return getOrTimeoutAfterSeconds(0); }
        catch (FutureComputationTimedOutException e) { throw new RuntimeException("impossible", e); }
    }
    
    /**
     * An iterable whose values are computed in the background.
     * The populate method must return an iterable.
     */
    public abstract static class IterableFuture<I> extends Future<Iterable<I>> implements Iterable<I> {
        @Override public Iterator<I> iterator() { return get().iterator(); }
    }
}
