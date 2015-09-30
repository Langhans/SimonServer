package pTimServer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



public class ThreadPool {

   private ThreadPoolExecutor pool;
   private BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(1000, true); 
   
   
   public ThreadPool(){
       
       pool = new ThreadPoolExecutor( 4 , 8 , 60, TimeUnit.MINUTES , workQueue);
       pool.prestartCoreThread();
       
   }
   
   
   public void submitRunnable( Runnable e ){
       pool.submit( e );
       System.out.println("pool submitted a new Runnable");
   }
    
   
   
   
    
}
