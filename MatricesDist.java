import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

class MatricesDist
{
  static Object lock = new Object();
  static int N = 4;
  static int[][] C1 = new int[N/2][N/2];
  static int[][] C2 = new int[N/2][N/2];
  static int[][] C3 = new int[N/2][N/2];
  static int[][] C4 = new int[N/2][N/2];
  
  static void read(DataInputStream f,byte[] b,int posicion,int longitud) throws Exception
  {
    while (longitud > 0)
    {
      int n = f.read(b,posicion,longitud);
      posicion += n;
      longitud -= n;
    }
  }
  static class Worker extends Thread
  {
    Socket conexion;
    int[][]MA;
    int[][]MB;
    int opc;
    
    Worker(Socket conexion, int[][]MA, int[][]MB, int opc)
    {
      this.conexion = conexion;
      this.MA = MA;
      this.MB = MB;
      this.opc = opc;
    }
    public void run()
    {
        try {
            DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
            DataInputStream entrada = new DataInputStream(conexion.getInputStream());
            
            ByteBuffer bt;
                       
            //Enviando matriz A            
            for (int i = 0; i < MA.length; i++) {
                
                bt = ByteBuffer.allocate(4*MA[i].length);
                for (int j = 0; j < MA[i].length; j++) {
                    bt.putInt(MA[i][j]);
                }
                byte[] at = bt.array();
                salida.write(at);
            }
            
            //Enviando matriz B            
            for (int i = 0; i < MB.length; i++) {
                
                bt = ByteBuffer.allocate(4*MB[i].length);
                for (int j = 0; j < MB[i].length; j++) {
                    bt.putInt(MB[i][j]);
                }
                byte[] at = bt.array();
                salida.write(at);
            }
            
            //Recibiendo matriz C, dependiendo que hilo se inició, se guarda en la respectiva matriz
            switch(opc)
            {
                case 1:
                    for (int i = 0; i < C1.length; i++) {
                        byte[] at = new byte[C1[0].length*4];
                        read(entrada,at,0,C1[0].length*4);
                        IntBuffer intBuf = ByteBuffer.wrap(at).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                        int[] array = new int[intBuf.remaining()];
                        intBuf.get(array);
                        for (int j = 0; j < C1[0].length; j++) {
                            C1[i][j] = array[j];
                        }
                    }
                break;
                case 2:
                    for (int i = 0; i < C2.length; i++) {
                        byte[] at = new byte[C2[0].length*4];
                        read(entrada,at,0,C2[0].length*4);
                        IntBuffer intBuf = ByteBuffer.wrap(at).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                        int[] array = new int[intBuf.remaining()];
                        intBuf.get(array);
                        for (int j = 0; j < C2[0].length; j++) {
                            C2[i][j] = array[j];
                        }
                    }
                break;
                case 3:
                    for (int i = 0; i < C3.length; i++) {
                        byte[] at = new byte[C3[0].length*4];
                        read(entrada,at,0,C3[0].length*4);
                        IntBuffer intBuf = ByteBuffer.wrap(at).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                        int[] array = new int[intBuf.remaining()];
                        intBuf.get(array);
                        for (int j = 0; j < C3[0].length; j++) {
                            C3[i][j] = array[j];
                        }
                    }
                break;
                default:
                    for (int i = 0; i < C4.length; i++) {
                        byte[] at = new byte[C4[0].length*4];
                        read(entrada,at,0,C4[0].length*4);
                        IntBuffer intBuf = ByteBuffer.wrap(at).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
                        int[] array = new int[intBuf.remaining()];
                        intBuf.get(array);
                        for (int j = 0; j < C4[0].length; j++) {
                            C4[i][j] = array[j];
                        }
                    }
            }
            
            salida.close();
            entrada.close();
            conexion.close();
        } catch (IOException ex) {
            Logger.getLogger(MatricesDist.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MatricesDist.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  }
  public static void main(String[] args) throws Exception
  {
    if (args.length != 1)
    {
      System.err.println("Uso:");
      System.err.println("java Mat <nodo>");
      System.exit(0);
    }
    int nodo = Integer.valueOf(args[0]);
    if (nodo == 0)
    {
        //NODO 0
        int[][] A = new int[N][N];
        int[][] B = new int[N][N];
        int[][] C = new int[N][N];
        
        int[][] A1 = new int[N/2][N];
        int[][] A2 = new int[N/2][N];
        int[][] B1 = new int[N/2][N];
        int[][] B2 = new int[N/2][N];
        
        // inicializa las matrices A y B

        for (int i = 0; i < N; i++)
          for (int j = 0; j < N; j++)
          {
            A[i][j] = 2 * i + j;
            B[i][j] = 2 * i - j;
            C[i][j] = 0;
          }
        
          //Se imprimen las matrices A y B
        System.out.println("******MATRIZ A******");
        if (N == 4) {
            for (int x=0; x < A.length; x++) {
                System.out.print("|");
                for (int y=0; y < A[x].length; y++) {
                  System.out.print (A[x][y]);
                  if (y!=A[x].length-1) System.out.print("\t");
                }
                System.out.println("|");
            }
        }
        System.out.println("******MATRIZ B******");
        if (N == 4) {
            for (int x=0; x < B.length; x++) {
                System.out.print("|");
                for (int y=0; y < B[x].length; y++) {
                  System.out.print (B[x][y]);
                  if (y!=B[x].length-1) System.out.print("\t");
                }
                System.out.println("|");
            }
        }

         //transpone la matriz B, la matriz traspuesta queda en B
        for (int i = 0; i < N; i++)
          for (int j = 0; j < i; j++)
          {
            int x = B[i][j];
            B[i][j] = B[j][i];
            B[j][i] = x;
          }
        
              
        //Llenado de A1
        for (int i = 0; i < N/2; i++) {
            for (int j = 0; j < N; j++) {
                A1[i][j] = A[i][j];
            }
        }     

        
        //Llenado de A2
        for (int i = N/2; i < N; i++) {
            for (int j = 0; j < N; j++) {
                A2[i-(N/2)][j] = A[i][j];
            }
        }
        
        //Llenado de B1
        for (int i = 0; i < N/2; i++) {
            for (int j = 0; j < N; j++) {
                B1[i][j] = B[i][j];
            }
        }     
        
        //Llenado de B2
        for (int i = N/2; i < N; i++) {
            for (int j = 0; j < N; j++) {
                B2[i-(N/2)][j] = B[i][j];
            }
        }
      
        ServerSocket servidor = new ServerSocket(50000);
        Worker [] w = new Worker[4];
        Socket conexion;
        
        //Hilo para nodo 1
        conexion = servidor.accept();
        w[0] = new Worker(conexion, A1, B1, 1);
        w[0].start();
        
        //Hilo para nodo 2
        conexion = servidor.accept();
        w[1] = new Worker(conexion, A1, B2, 2);
        w[1].start();
        
        //Hilo para nodo 3
        conexion = servidor.accept();
        w[2] = new Worker(conexion, A2, B1, 3);
        w[2].start();
        
        //Hilo para nodo 4
        conexion = servidor.accept();
        w[3] = new Worker(conexion, A2, B2, 4);
        w[3].start();
        
        //Join para que el programa espera hasta tener las matrices llenas
        for (int i = 0; i < 4; i++)
        {
            w[i].join();
        }

        //Guardamos las matrices C1, C2, C3 y C4 como se indicó
        for (int i = 0; i < C1.length; i++) {
            for (int j = 0; j < C1[0].length; j++) {
                C[i][j] = C1[i][j];
            }
        }
        
        for (int i = 0; i < C2.length; i++) {
            for (int j = 0; j < C2[0].length; j++) {
                C[i][j+(N/2)] = C2[i][j];
            }
        }
        
        for (int i = 0; i < C3.length; i++) {
            for (int j = 0; j < C3[0].length; j++) {
                C[i+(N/2)][j] = C3[i][j];
            }
        }
        
        for (int i = 0; i < C4.length; i++) {
            for (int j = 0; j < C4[0].length; j++) {
                C[i+(N/2)][j+(N/2)] = C4[i][j];
            }
        }
        
        //Imprimimos la matriz C ya llenada
        System.out.println("******MATRIZ C******");
        if (N == 4) {
            for (int x=0; x < C.length; x++) {
                System.out.print("|");
                for (int y=0; y < C[x].length; y++) {
                  System.out.print (C[x][y]);
                  if (y!=C[x].length-1) System.out.print("\t");
                }
                System.out.println("|");
            }
        }
        
        int cs = 0;
        for (int i = 0; i < C.length; i++) {
            for (int j = 0; j < C[0].length; j++) {
                cs += C[i][j];
            }
        }
        System.out.println("CHECKSUM: "+cs);
    }
    else
    {
        //NODOS 1,2,3,4
        Socket conexion = null;
    
        for(;;)
            try
            {
                conexion = new Socket("localhost",50000);
                break;
            }
            catch (Exception e)
            {
              Thread.sleep(100);
            }
        
        DataOutputStream salida = new DataOutputStream(conexion.getOutputStream());
        DataInputStream entrada = new DataInputStream(conexion.getInputStream());
                
        int MA[][] = new int [N/2][N];
        int MB[][] = new int [N/2][N];
        int MC[][] = new int [N/2][N/2];
        
        //Recibiendo Matriz A
        for (int i = 0; i < MA.length; i++) {
            byte[] at = new byte[MA[0].length*4];
            read(entrada,at,0,MA[0].length*4);
            IntBuffer intBuf = ByteBuffer.wrap(at).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
            int[] array = new int[intBuf.remaining()];
            intBuf.get(array);
            for (int j = 0; j < MA[0].length; j++) {
                MA[i][j] = array[j];
            }
        }
        
        //Recibiendo Matriz B
        for (int i = 0; i < MB.length; i++) {
            byte[] at = new byte[MB[0].length*4];
            read(entrada,at,0,MB[0].length*4);
            IntBuffer intBuf = ByteBuffer.wrap(at).order(ByteOrder.BIG_ENDIAN).asIntBuffer();
            int[] array = new int[intBuf.remaining()];
            intBuf.get(array);
            for (int j = 0; j < MB[0].length; j++) {
                MB[i][j] = array[j];
            }
        }
        
        //Multiplicando matrices
        for (int i = 0; i < MA.length; i++)
            for (int j = 0; j < MB.length; j++)
                for (int k = 0; k < MA[0].length; k++)
                    MC[i][j] += MA[i][k] * MB[j][k];
        
        //Enviando Matriz C
        for (int i = 0; i < MC.length; i++) {        
            ByteBuffer bt = ByteBuffer.allocate(4*MC[i].length);
            for (int j = 0; j < MC[i].length; j++) {
                bt.putInt(MC[i][j]);
            }
            byte[] at = bt.array();
            salida.write(at);
        }
        
        salida.close();
        entrada.close();
        conexion.close();
        
        
    }
  }
}


