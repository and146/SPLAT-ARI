package uk.ac.starlink.jiniutils;

         /* ----------------------------------------- */
         /*                                           */
         /*     TRANSACTION MANAGER ACCESSOR          */
         /*                                           */
         /* ----------------------------------------- */

         // Jini core packages

         import net.jini.core.discovery.LookupLocator;
         import net.jini.core.lookup.*;
         import net.jini.core.entry.Entry;
         import net.jini.core.transaction.server.TransactionManager;

         // Jini extension package

         import net.jini.lookup.entry.*;

         // Java core packages

         import java.io.*;
         import java.rmi.*;
         import java.net.*;
         import java.util.*;


         public class TransactionManagerAccessor {

             /* ------ FIELDS ------ */
          
             static String jiniURL   = null;
             Properties props;
           
             static final long MAX_LOOKUP_WAIT = 2000L;
             
             TransactionManager manager;
     /* ------ CONSTRUCTORS ------ */

             /* GET SPACE */
             
             public TransactionManagerAccessor(String propFileName) {
                 LookupLocator locator = null;
                 ServiceRegistrar registrar = null;
                 
                 // Security manager
                 
                 try {
                     System.setSecurityManager(new RMISecurityManager());
                     }
                 catch (Exception e) {
                     e.printStackTrace();
                     }

                 // Get properties from property file
                 
                 initProperties(propFileName);
                 
                 
                 try {
                     // Get lookup service locator at "jini://hostname"
                     // use default port and register of the locator
                     locator = new LookupLocator(jiniURL);
                     registrar = locator.getRegistrar();

                     // Space name provided in property file
                     ServiceTemplate template;
                     // Specify the service requirement, array (length 1) of 
                     // instances of Class
                     Class [] types = new Class[] {TransactionManager.class};
                     template = new ServiceTemplate (null, types, null);
                     
                     // Get manager, 10 attempts!
                     for (int i=0; i < 10; i++) {
                         Object obj = registrar.lookup (template);                         

                         if (obj instanceof TransactionManager) {
                             manager = (TransactionManager) obj;
                             break;
                             }
                         System.out.println("BasicService. TransactionManager not " +
                                         "available. Trying again...");
                         Thread.sleep(MAX_LOOKUP_WAIT);
                         }
                     }
                 catch(Exception e) {
                     e.printStackTrace();
                     }   
                 }
                 
             /* INITIALISE PROPERTIES. Read property file and assign value to 
             spaceName and jiniURL fields */

             protected void initProperties(String propFileName) {
             
                 // Create instance of Class Properties
                 props = new Properties(System.getProperties());

                 // Try to load property list
                 try {
                     props.load( new BufferedInputStream(new 
                                         FileInputStream(propFileName)));
                     }
                 catch(IOException ex) {
                     ex.printStackTrace();
                     System.out.println( "Exception in SpaceAccessor" );
                     System.exit(-3);
                     }   
             
                 // Output property list (can be ommitted - testing only)
                 
                 System.out.println("jiniURL   = " + 
                                 props.getProperty("jiniURL"));
        // Assign values to fields
             
                 jiniURL = props.getProperty("jiniURL");
                 }       
             
             /* GET MANAGER */
              
             public TransactionManager getTransactionManager() {
                 return manager;
                 }
             }
