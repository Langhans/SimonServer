package pTimServer;

import static pSimonGenerell.Constants.REG_PORT;
import static pSimonGenerell.Constants.SERVER_PORT;

import java.util.logging.Level;
import java.util.logging.Logger;

import de.root1.simon.Registry;
import de.root1.simon.Simon;
import pSimonGenerell.ITimRunner;
import pSimonGenerell.TimRunner;

public class TimServerMain {
  
    public static void main(String[] args) {
        
        // possible to set a SecurityManager but not needed!!!
        // System.setSecurityManager(new SecurityManager());
        
        try {
            Registry reg = Simon.createRegistry( REG_PORT );
            reg.start();
            TimServer server = 
                new TimServer(SERVER_PORT, "localhost", REG_PORT);
            System.out.println("Server running at " + REG_PORT );
            reg.bind("TimServer", server);
            ITimRunner runner = new TimRunner();
            reg.bind("TimRunner", runner);
            System.out.println("TimRunner obj. bound to registry as 'TimRunner'");
            System.out.println("Serverobject bound to registry as 'TimServer'");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Server could not be started!");
            throw new RuntimeException("SERVER CANNOT BE STARTED!");
        }
    }
}
