import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

class TokenRing
{
  static DataInputStream entrada;
  static DataOutputStream salida;
  static boolean primera_vez = true;
  static String ip;
  static long token = 0;
  static int nodo;

  static class Worker extends Thread
  {
    public void run()
    {
        try {
            
            ServerSocket servidor = new ServerSocket(50000);
            Socket conexion = servidor.accept();
            entrada = new DataInputStream(conexion.getInputStream());
            
        } catch (IOException ex) {
            Logger.getLogger(TokenRing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  }

  public static void main(String[] args) throws Exception
  {
    if (args.length != 2)
    {
      System.err.println("Se debe pasar como parametros el numero de nodo y la IP del siguiente nodo");
      System.exit(1);
    }

    nodo = Integer.valueOf(args[0]);  // el primer parametro es el numero de nodo
    ip = args[1];  // el segundo parametro es la IP del siguiente nodo en el anillo
    
    Worker w = new Worker();
    w.start();
    Socket conexion = null;
    
    for(;;)
        try
        {
            conexion = new Socket(ip,50000);
            break;
        }
        catch (Exception e)
        {
          Thread.sleep(500);
        }
    
    salida = new DataOutputStream(conexion.getOutputStream());
    w.join();
    
      for (;;) {
          if (nodo == 0) {
              if (primera_vez)
                  primera_vez = false;
              else
                  token = entrada.readLong();
          }
          else
              token = entrada.readLong();
          token ++;
          System.out.println("Token: "+token);
          salida.writeLong(token);
      }
  }
}