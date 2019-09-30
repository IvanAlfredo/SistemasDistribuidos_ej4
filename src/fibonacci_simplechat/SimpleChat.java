package fibonacci_simplechat;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.*;
import java.util.List;
import java.util.LinkedList;

public class SimpleChat extends ReceiverAdapter {
    JChannel channel;
    //String user_name=System.getProperty("user.name", "n/a");
    String user_name="Ivan";
    final List<String> state=new LinkedList<>();

    public void viewAccepted(View new_view) {
        System.out.println("** vista: " + new_view);
    }

    public void receive(Message msg) {
        String line=msg.getSrc() + ": " + msg.getObject();
        System.out.println(line);
        synchronized(state) {
            state.add(line);
        }
    }

    public void getState(OutputStream output) throws Exception {
        synchronized(state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    @SuppressWarnings("unchecked")
    public void setState(InputStream input) throws Exception {
        List<String> list=(List<String>)Util.objectFromStream(new DataInputStream(input));
        synchronized(state) {
            state.clear();
            state.addAll(list);
        }
        System.out.println("estado recibido (" + list.size() + " mensajes en la historia del chat ):");
        for(String str: list) {
            System.out.println(str);
        }
    }


    private void start() throws Exception {
        channel=new JChannel();
        channel.setReceiver(this);
        channel.connect("ChatCluster");
        channel.getState(null, 10000);
        eventLoop();
        channel.close();
    }
    public String fibonacci(int numero){
        //numero=5            0 1 1 2 3 5 8 13
        String texto="";
        int a=0;
        int b=1;
        int f=0;
        if (numero>=0) {
           for (int i = 0; i < numero; i++) {
               f=a;
               a=f+b;
               b=f;
               texto=texto+" "+b;
           }
        }
        else{
             texto="Ingrese un número positivo";
        }        
        return texto;
    }
    private void eventLoop() {
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                System.out.print(">"); 
                System.out.flush();
                String line=in.readLine().toLowerCase();
                String resultado = fibonacci(Integer.parseInt(line));
                if(line.startsWith("quit") || line.startsWith("exit")) {
                    break;
                }
               
                resultado="[" + user_name + "] " + resultado;
                Message msg=new Message(null,resultado);
                channel.send(msg);
            }
            catch(Exception e) {
            }
        }
        
    }


    public static void main(String[] args) throws Exception {
        new SimpleChat().start();
    }
}
