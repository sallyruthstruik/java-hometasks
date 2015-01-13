/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aggregator;

import java.util.HashMap;
import java.util.Map;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


class Worker{
    //тут храним окончательный результат, по закрытым парам
    private static final Map userTimesMap = new HashMap();
    //тут храним последние встреченные события
    private static final Map userActionsMap = new HashMap();

    Opener opener;
    
    public Worker(Opener opener) {
        this.opener = opener;
    }
    
    
    private void processOneRow(String row, Long lineno){
        String [] splittedRow = row.split(",");
        
        Long duration = Long.parseLong(splittedRow[0]);
        Integer uid = Integer.parseInt(splittedRow[1]);
        String action = splittedRow[2];
        
        if(!userActionsMap.containsKey(uid)){
            userActionsMap.put(uid, action);
            userTimesMap.put(uid, -duration);
        }else{
            String lastAction = (String)userActionsMap.get(uid);
            if(lastAction.equals(action)){
                System.err.println("Not opened action at line no "+lineno);
                System.exit(1);
            }else{
                Long prevDuration = (Long)userTimesMap.get(uid);
                userTimesMap.put(uid, prevDuration + (action.equals("login")?-1:1)*duration);
                userActionsMap.put(uid, action);
            }
        }
        
    }
    
    public void process() throws IOException{
        //open file        
        long i=0L;
        for(String line:opener){
            processOneRow(line, i++);
        }
        
        printResults();
        
    }
    
    List sortResulted(){
        List list = new ArrayList(userTimesMap.entrySet());
        
        Collections.sort(list, new Comparator(){
            public int compare(Object o1, Object o2){
                Long val1 = ((Map.Entry<Integer, Long>)o1).getValue();
                Long val2 = ((Map.Entry<Integer, Long>)o2).getValue();
                return ((Comparable)val2).compareTo(val1);
            }
        });
        
        return list;
    }
    
    void printResults(){
        List sorted_items = sortResulted();
         
        for(Object item :  sorted_items){
            Map.Entry<Integer, Long> node = (Map.Entry<Integer, Long>)item;
            System.out.println("User " + node.getKey() + " has duration "+ node.getValue());
        }
    }
}

/**
 *
 * @author Станислав
 */
public class Aggregator {

    /**
     * @param args the command line arguments
     */
    public static void main(String [] args)throws IOException{
        long start = System.currentTimeMillis();
        
        try(Opener op = new MemoryMappedOpener("test.txt")){
            new Worker(op).process();
        }
        System.out.println("Execution time "+(System.currentTimeMillis() - start) + " ms");
    }
    
}
