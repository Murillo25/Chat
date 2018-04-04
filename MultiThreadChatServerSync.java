package multithreadchatserversync;

import java.io.DataInputStream;
import java.io.PrintStream; 
import java.io.IOException; 
import java.net.Socket; 
import java.net.ServerSocket; 

class clientThread extends Thread {
    private String clientName = null;
    private DataInputStream is = null;     
    private PrintStream os = null;     
    private Socket clientSocket = null;
    private final clientThread[] threads;   
    private int maxClientsCount; 
    
    public clientThread(Socket clientSocket, clientThread[] threads) { 
         this.clientSocket = clientSocket; 
         this.threads = threads; 
         maxClientsCount = threads.length; 
    }
    
     public void run() { 
         int maxClientsCount = this.maxClientsCount;
         clientThread[] threads = this.threads; 
         try{
             is = new DataInputStream(clientSocket.getInputStream()); 
             os = new PrintStream(clientSocket.getOutputStream());
             String name; 
             while (true) { 
                 os.println("Escribe tu nombre : "); 
                 name = is.readLine().trim(); 
                  if (name.indexOf('@') == -1) {
                      break;
                  }else{
                       os.println("El nombre no debe contener el caracter '@'."); 
                  }
             }
              os.println("Bienvenido " + name + " a nuestro cuarto de chat.\npara salir escribe /quit en una linea nueva."); 
               synchronized (this) { 
                    for (int i = 0; i < maxClientsCount; i++) { 
                        if (threads[i] != null && threads[i] == this) { 
                            clientName = "@" + name; 
                            break;
                        }
                    }
                    for (int i = 0; i < maxClientsCount; i++) { 
                        if (threads[i] != null && threads[i] != this) {
                             threads[i].os.println("*** Un usuario nuevo " + name  + " entró al cuarto de chat!!! ***"); 
                        }
                    }
               }
               
                while (true) { 
                    String line = is.readLine(); 
                     if (line.startsWith("/quit")) { 
                         break;
                     }
                     if (line.startsWith("@")) { 
                         String[] words = line.split("\\s", 2); 
                          if (words.length > 1 && words[1] != null) { 
                               words[1] = words[1].trim();
                                if (!words[1].isEmpty()) {
                                    synchronized (this) { 
                                        for (int i = 0; i < maxClientsCount; i++) { 
                                             if (threads[i] != null && threads[i] != this&& threads[i].clientName != null&& threads[i].clientName.equals(words[0])) { 
                                                 threads[i].os.println("<" + name + "> " + words[1]); 
                                                 this.os.println(">" + name + "> " + words[1]); 
                                                 break;
                                             }
                                        }
                                    }
                                }
                          }
                     }else{
                         synchronized (this) { 
                             for (int i = 0; i < maxClientsCount; i++) { 
                                  if (threads[i] != null && threads[i].clientName != null) { 
                                       threads[i].os.println("<" + name + "> " + line);
                                  }
                             }
                         }
                     }
                }
                synchronized (this) {
                     for (int i = 0; i < maxClientsCount; i++) { 
                          if (threads[i] != null && threads[i].clientName != null) { 
                               threads[i].os.println("<" + name + "> "); 
                          }
                     }
                }
                synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) { 
                     if (threads[i] != null && threads[i] != this && threads[i].clientName != null) { 
                  threads[i].os.println("*** El usuario " + name + " abandonó el cuarto de chat!!! ***");  
              }
         }
     }
     os.println("*** Adios " + name + " ***"); 
     synchronized (this) { 
          for (int i = 0; i < maxClientsCount; i++) { 
              if (threads[i] == this) { 
                   threads[i] = null;
              }
          }
     }
      is.close(); 
      os.close(); 
      clientSocket.close(); 
         }catch (IOException e) {
     }  
    }   
}

public class MultiThreadChatServerSync {
    private static ServerSocket serverSocket = null; 
    private static Socket clientSocket = null;
    private static final int maxClientsCount = 10;    
    private static final clientThread[] threads = new clientThread[maxClientsCount]; 
    public static void main(String[] args) {
        // TODO code application logic here
        int portNumber = 2000; 
        if (args.length < 1) { 
            System.out.println("Utilizacion: java MultiThreadChatServerSync <numeroPuerto>\n"+ "Ahora utilizando el numero de puerto = " + portNumber); 
            
        }else{
            portNumber = Integer.valueOf(args[0]).intValue();
        }
         try { 
            serverSocket = new ServerSocket(portNumber); 
         }catch (IOException e) { 
             System.out.println(e); 
         }
         while(true){
             try{
                 clientSocket = serverSocket.accept(); 
                 int i = 0;
                 for (i = 0; i < maxClientsCount; i++) { 
                     if (threads[i] == null) { 
                        (threads[i] = new clientThread(clientSocket, threads)).start();                         
                         break; 
                     }
                 }
                 if (i == maxClientsCount) { 
                      PrintStream os = new PrintStream(clientSocket.getOutputStream()); 
                      os.println("Servidor ocupado. Trata despues.");
                      os.close(); 
                      clientSocket.close(); 
                 }
             }catch (IOException e) { 
                 System.out.println(e); 
             }
         }
    }
    
}
