package com.gal.firechat;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;


/**
 * Created by Gal on 13/07/2017.
 */

public class Message{
    private String message,sender,receiver,dateShort;
    private String status;
    static SimpleDateFormat sdf;
    public Message(){}

    public Message(User sender,User receiver,String message){
        this.sender = sender.getEmail();
        this.receiver = receiver.getEmail();
        this.message = message;
        final Object waiter = new Object();
        status = "unread";
        sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        new Thread()
        {
            public void run() {
                TimeLookup time = new TimeLookup();
                if (time.requestTime("0.asia.pool.ntp.org",10000)){
                    dateShort = sdf.format(time.getNtpTime());
                    Log.i("Date server",dateShort.toString());
                    synchronized (waiter) {
                        waiter.notify();
                    }
                }
            }
        }.start();

        try {
            synchronized (waiter) {
                waiter.wait();
            }
        }
        catch (InterruptedException ex){
            System.out.println(ex.getMessage());
        }
    }

    public String getMessage() {return message;}

    public String getDateShort() {
        return dateShort;
    }

    public String getStatus(){
        return status;
    }
    public String getSender() {return sender;}
    public String getReceiver() {return receiver;}

    public static Comparator<Message> MessageDateComparator
            = new Comparator<Message>() {

        public int compare(Message o1, Message o2) {
            Date first,second;
            first=second=null;
            try {
                first=sdf.parse(o1.getDateShort());
                second=sdf.parse(o2.getDateShort());
            }
            catch (ParseException pe){
                System.out.println(pe.getMessage());
            }
            if (first != null && second != null)
                return first.compareTo(second);
            else {
                Log.i("Compare","ERROR");
                return 0;
            }
        }

    };

}
