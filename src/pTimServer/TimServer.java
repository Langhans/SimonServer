package pTimServer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingWorker;

import de.root1.simon.Registry;
import de.root1.simon.exceptions.SimonRemoteException;
import de.root1.simon.Simon;
import pSimonGenerell.IClientCallBack;
import pSimonGenerell.ITimServer;
import pTimEvent.IEventHandler;
import pTimEvent.TimEvent;
import pTimEvent.TimEventBus;

import static pTimEvent.TimEvent.Tag;
import static pSimonGenerell.Constants.*;

public class TimServer  implements ITimServer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final int         Server_port;
    private final String      Server_host;
    private final int         reg_port;
    
    
    private ThreadPool        pool                 = new ThreadPool();
    private List<IClientCallBack> connectedClients = new ArrayList<>();
    private boolean RUNNING = true;
    private PingWorker pinger = new PingWorker();
    
    public TimServer() throws SimonRemoteException{
        Server_port = SERVER_PORT;
        Server_host = SERVER_HOST;
        reg_port = REG_PORT; 
        pinger.start();
    }
    
    
    public TimServer(int port, String host, int reg_port)
            throws SimonRemoteException {

        Server_host = host;
        Server_port = port;
        this.reg_port = reg_port; 
        pinger.start();
    }

    
    @Override
    public synchronized boolean login ( IClientCallBack client ) throws SimonRemoteException{
      connectedClients.add( client );
      return true;
    }
    
   
    @Override
    public void submitRunnable(Runnable r) throws SimonRemoteException {
       
        pool.submitRunnable(r);
        System.out.printf("Runnable %s has been submitted to pool!", r.toString());
    }

    
    @Override
    public byte[] serveFile(String filename) throws SimonRemoteException {
        
        File file;
        try{
        file = new File(filename);
        
        System.out.println("File " + filename + "requested and exists:" + file.exists());
        byte[] buffer = new byte[(int) file.length()];

        BufferedInputStream inStr = null;
        
            inStr = new BufferedInputStream(new FileInputStream(file));
            inStr.read(buffer, 0, buffer.length);
            inStr.close();
        
        System.out.println("Server served File " + filename + "! " + new Date());
        return buffer;
      
        } catch (IOException e){
          e.printStackTrace();
          throw new SimonRemoteException("Could not load and serve file!");
        }
    }


	@Override
	public String[] listFiles() throws SimonRemoteException {
		try {
			File parent;
			parent = new File( findSource( this.getClass() )).getParentFile();

			List<File> files = fileCrawl(parent , new ArrayList<File>());
			String[] filenames = new String[ files.size() ] ;

			for (int i = 0; i < files.size(); i++) {
				filenames[i] = files.get(i).getCanonicalPath();
			}
			return filenames;
			
		} catch (Exception e) {
			return new String[] { "Could not open Filelist" }; // should not use hardcoded here...
		}
	}
  
	
	private class PingWorker extends Thread{

	  private Timer timer = new Timer();
	  private TimerTask task;
	  
	  private PingWorker(){
	      
	      task = new TimerTask() {
        
        @Override
        public void run() {
          
            for (IClientCallBack c : connectedClients){
              try{
              c.ping();
              } catch (SimonRemoteException e){
                connectedClients.remove(c);
              }
            }
          }  
	      };
      timer.schedule(task, 5000, SERVER_PING_INTERVALL);
	  }
	}// End of PINGERWORKER
		
	
	/** 
	 *  crawls recursively through a file-structure, parent and all child nodes in file-tree, and saves the files to
	 *  a java.util.List<File>.  
	 *  
	 *  @author: Tim Langhans , YRGO Göteborg, class Java15 
	 *  @param: File parent [the parent file]
	 *  @param: List<File> files [call with an empty list initially!]
	 *  @return: List<File> [list of all files in parents directory and subdirectories] , throws IllegalArgumentException
	 *  if input-parameter parent is null!
	 *  @invar: File parent may not be null => throws IllegalArguementException!
	 * 
	 * */
	public static List<File> fileCrawl ( File parent , List<File> files ){
	  
	  assert parent != null;
	  if (parent == null){
	    throw new IllegalArgumentException("File parent may not be null!");
	  }
	  // TODO just for testing in ECLIPSE!!! ELSE COMMENT LINE!!!
	  //parent = new File("pics/pics2");
	  
    File[] fs;
    if (parent.isDirectory()) {
      fs = parent.listFiles();
      // recursion base case
      if (fs == null) {
        return files;
      } else {
        
        for (File f : fs) {
          if (f.isDirectory()) {
            fileCrawl(f, files);
          } else if (f.isFile()) {
            files.add(f);
          } else {
            // TODO just ignore? Exception?
           
          }
        }
        return files;
      }
    } else if (parent.isFile()) {
      files.add(parent);
      return files;

    } 
//    else {
//      // TODO error exception???
//    }
    return null;
  }

  /**
   * Method to find the absolute path of a specific Class, i.e. for beeing able
   * to use external file resources from an application running from a .jar
   * file.
   * 
   * @param clazz Class you want to get the absolute path to
   * @return String of absolute path to the Class from input parameter
   * @warning does not check for possible Exceptions, may return null!
   */
	
	public static String findSource(Class<?> clazz) {
    String resourcesPath = '/' + clazz.getName().replace(".", "/") + ".class";
    java.net.URL location = clazz.getResource(resourcesPath);
    String sourcePath = location.getPath();
    String finalPath = sourcePath.replace("file:", "").replace("!" + resourcesPath, "");
    return finalPath;
	}
}
