package br.ufs.dcomp.ExemploRabbitMQ;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.TimeZone;
import java.lang.*;
import br.ufs.dcomp.ExemploRabbitMQ.AddressBookProtoBuf.*;
import com.google.protobuf.*;
import org.apache.commons.io.*;



public class Arquivo implements Runnable
{
    private String destinatario;
    private String destinatario_orig;
    private String remetente;
    private String remetente_orig;
    private String diretorio;
    private String prompt;
    private char token_actual;
    private Channel channel;
    
    public Arquivo(String destinatario, String destinatario_orig, String remetente, String remetente_orig ,String diretorio, String prompt, char token_actual, Channel channel)
    {
        this.destinatario = destinatario;
        this.destinatario_orig = destinatario_orig;
        this.remetente = remetente;
        this.remetente_orig = remetente_orig;
        this.diretorio = diretorio;
        this.prompt = prompt;
        this.token_actual = token_actual;
        this.channel = channel;
    }
    
    public void run()
    {
        System.out.println("Uploading file " + diretorio + " to " + prompt + "...");
        //channel = connection.createChannel();
        //InputStream inputstream;
               try {
                      InputStream inputstream = new FileInputStream(diretorio);
                      
                      //byte[] dataAsByte = new byte[1024];   
                      byte[] dataAsByte = IOUtils.toByteArray(inputstream);
                      //channel.basicPublish("", destinatario, null, dataAsByte);
                      ByteString msg_Array = ByteString.copyFrom(dataAsByte);
                      /*
                      int bytesRead = inputstream.read(dataAsByte);
                      ByteString msg_Array = ByteString.copyFrom(dataAsByte);
                      while(bytesRead != -1)
                      {
                          msg_Array = ByteString.copyFrom(dataAsByte);
                          //channel.basicPublish("", destinatario, null, dataAsByte);
                          bytesRead = inputstream.read(dataAsByte);
                          
                      }
                      //inputstream = new BufferedInputStream(new FileInputStream(diretorio));
                      */
                        Mensagem.Builder direct_msg = Mensagem.newBuilder();
                        
                        Mensagem.Conteudo.Builder direct_conteudo = Mensagem.Conteudo.newBuilder();
                        direct_msg.setSender(remetente_orig);
                        
                        
                        TimeZone timeZone1 = TimeZone.getTimeZone("America/Maceio");
                        //Calendar c = Calendar.getInstance();
                        Calendar c = new GregorianCalendar();
                        c.setTimeZone(timeZone1);
                        DateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        dateFormat1.setTimeZone(timeZone1);
                        //System.out.println(dateFormat1.format(c.getTime()));
                        String date_actual1[] = dateFormat1.format(c.getTime()).split(" ");
                        direct_msg.setDate(date_actual1[0]);
                        direct_msg.setTime(date_actual1[1]);
                        //direct_msg.setType("txt/plain");
                        //direct_msg.setBody("diretorio".getBytes());
                        //direct_msg.build();
                        //byte[] msg_bytes = msg.getBytes();
                        //ByteString msg_Array = ByteString.copyFrom(dataAsByte);
                        
                        direct_conteudo.setType("txt/png");
                        direct_conteudo.setBody(msg_Array);
                        direct_conteudo.setName(diretorio);
                        direct_conteudo.build();
                        direct_msg.setContent(direct_conteudo);
                      
                      
                      /*
                      if(token_actual == '@')
                        channel.basicPublish("", destinatario, null, dataAsByte);
                      else if(token_actual == '#')
                        channel.basicPublish(destinatario, "", null, dataAsByte);
                      */
                      
                      if(token_actual == '@')
                        channel.basicPublish("", destinatario, null, direct_msg.build().toByteArray());
                      else if(token_actual == '#')
                        channel.basicPublish(destinatario, "", null, direct_msg.build().toByteArray());
                      
                      inputstream.close();
                      System.out.println("File " + diretorio + " is available to " + prompt + " !");
                      //channel.basicPublish("", destinatario, null, dataAsByte);
               } catch (FileNotFoundException e1) {
                      // TODO Auto-generated catch block
                      e1.printStackTrace();
               } catch (IOException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
               }
               
        //inputstream.close();
    }
}

