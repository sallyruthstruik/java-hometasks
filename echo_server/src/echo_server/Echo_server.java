/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echo_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Станислав
 */

class SocketWorker implements Runnable{
    private Thread t;
    private Socket socket;
    
    //Тут храним все текущие слушающие воркеры
    private static ConcurrentHashMap workerMap = new ConcurrentHashMap();
    
    //уникальный счетчик воркеров
    private static Integer counter = 0;
    
    private String name;
    private PrintWriter out;
    
    public SocketWorker(Socket socket) throws IOException{
        this.socket = socket;
        
        counter ++;
        
        this.name = counter.toString();
        this.out = new PrintWriter(this.socket.getOutputStream());
        
    }
    
    public void sendMessage(String msg){
        out.println(msg);
        out.flush();
    }
    
    @Override
    public void run(){
        System.out.println("Start processing socket");
        try{
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String userInput;
            while((userInput = socketReader.readLine()) != null){
                if(userInput.equals("STOP")){
                    out.println("Stop connection...");
                    out.flush();
                    break;
                }
                
                for(Object obj : workerMap.values().toArray()){
                    
                    SocketWorker worker = (SocketWorker) obj;
                    System.out.println("Send message to worker: "+worker.toString());
                    
                    worker.sendMessage(userInput);
                }
            }

            System.out.println("Before closing");
            socketReader.close();
            out.close();
        }catch(IOException e){
            System.err.println("Getted IOException: "+e.toString());
        }finally{
            System.out.println("Remove worker with name "+name+" from map "+workerMap.toString());
            workerMap.remove(name);
        }
    }
    
    @Override
    public String toString(){
        return "SocketWorker-"+name;
    }
    
    public void start(){
        if(t == null){
            t = new Thread(this, name);
            workerMap.put(name, this);
            System.out.println("Map now: " + workerMap.toString());
            t.start();
        }
    }
}

public class Echo_server {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Integer PORT = 1234;
        if(args.length == 1){
            try{
                PORT = Integer.parseInt(args[0]);
            }catch(NumberFormatException e){
                System.err.println("If first argument passed it must be an integer");
                System.exit(1);
            }
        }else if(args.length > 1){
            System.err.println("Only 1 or 0 arguments can be passed");
            System.exit(2);
        }
        try{
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("Server created on port "+PORT.toString());
            while(true){
                Socket socket = server.accept();
                System.out.println("Getted new TCP socket");
                
                SocketWorker worker = new SocketWorker(socket);
                worker.start();
                
            }
        }catch(IOException e){
            System.err.println("Getted exception: "+e.toString());
            System.exit(3);
        }
        
    }
    
}
