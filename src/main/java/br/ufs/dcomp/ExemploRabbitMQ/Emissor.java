package br.ufs.dcomp.ExemploRabbitMQ;

import java.io.IOException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
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
import java.net.URL;
import java.net.InetAddress;
import java.net.*;
import br.ufs.dcomp.ExemploRabbitMQ.AddressBookProtoBuf.*;
import com.google.protobuf.*;
import org.apache.commons.io.*;


public class Emissor {
    
    
    private static final String EXCHANGE_NAME = "logs";
    
    public static void main(String[] argv) throws Exception{ 
        Scanner leitor = new Scanner(System.in);
        System.out.print("User:");
        String ex_name = leitor.nextLine();
        String ex_name_file = ex_name + "_file";
        String queue_name = "";
        String exchange_name = "";
        String prompt = "";
        
        char token_actual = ' ';
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqp://dhjnphvk:h6CrcgMBUWLMfyqDjaYmg8cEYJfWB0uy@termite.rmq.cloudamqp.com/dhjnphvk");
        Connection connection = factory.newConnection();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval( 5000 );
        Channel channel = connection.createChannel();
        channel.queueDeclare(ex_name, false, false, false, null);
        Channel channel2 = connection.createChannel();
        channel2.queueDeclare(ex_name_file, false, false, false, null);
        
        
        
        TimeZone timeZone1 = TimeZone.getTimeZone("America/Maceio");
        //Calendar c = Calendar.getInstance();
        Calendar c = new GregorianCalendar();
        c.setTimeZone(timeZone1);
        
        DateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        dateFormat1.setTimeZone(timeZone1);
        
        
        Consumer consumer = new DefaultConsumer(channel) 
        {
          public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bodyc)
              throws IOException {
            Mensagem msg_consumer = Mensagem.parseFrom(bodyc);
            Mensagem.Conteudo msg_conteudo = Mensagem.Conteudo.parseFrom(bodyc);
            //String message = new String(body, "UTF-8");
            //String msg_consumer[] = message.split("/n");
            //if(msg_conteudo.getGroup() == "")
                if(msg_consumer.getContent().getType().equals("mensagem")){
                    System.out.println("");
                    System.out.println("(" + msg_consumer.getDate() + " às " + msg_consumer.getTime() + ") " + msg_consumer.getSender() + " diz: " +
                                    msg_consumer.getContent().getBody().toStringUtf8());
                    System.out.print(">>");
                }
                else
                {
                    System.out.println("");
                    System.out.println("File " + msg_consumer.getContent().getName() + " from @" + msg_consumer.getSender() + " downloaded");
                    System.out.print(">>");
                }
                //System.out.println(prompt + ">>");
            //System.out.println(message);
            //System.out.println("(" + msg_consumer.getDate + "às" + msg_consumer.getTime + ")");
          }
        };
        channel.basicConsume(ex_name, true, consumer);
        channel2.basicConsume(ex_name_file,true,consumer);

        while(true)
        {
            //channel2.basicConsume(ex_name_file, true, consumer);
            //if(channel2.Alre)
            System.out.print(prompt +">>");
            
            c = Calendar.getInstance();
            //channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            String msg = leitor.nextLine();
            char token = msg.charAt(0);
            String commands[] = msg.split("\\s+");
            //System.out.println(commands[0]);
            
            
            if(token == '@')
            {
                queue_name = msg.substring(1,msg.length());
                token_actual = token;
                prompt = msg;
                
            }
            else if(token == '#')
            {
                //queue_name = msg.substring(1,msg.length());
                exchange_name = msg.substring(1,msg.length());
                token_actual = token;
                prompt = msg;
            }
            else if(commands[0].equals("!addGroup"))
            //else if(token == '!')
            {
                channel.exchangeDeclare(commands[1], "fanout");
                channel2.exchangeDeclare(commands[1]+"_file", "fanout");
                //channel.queueBind(queueName, "logs", "");
                channel.queueBind(ex_name, commands[1], "");
                channel2.queueBind(ex_name+"_file", commands[1]+"_file", "");
            }
            else if(commands[0].equals("!addUser"))
            {
                channel.queueBind(commands[1], commands[2], "");
                channel2.queueBind(commands[1]+"_file", commands[2]+"_file", "");
            }
            else if(commands[0].equals("!delFromGroup"))
            {
                channel.queueUnbind(commands[1], commands[2], "");
                channel2.queueUnbind(commands[1]+"_file", commands[2]+"_file", "");
            }
            else if(commands[0].equals("!removeGroup"))
            {
                channel.exchangeDelete(commands[1]);
                channel2.exchangeDelete(commands[1]+"_file");
            }
            else if(commands[0].equals("!listUsers"))
            {
                //InetAddress ip = InetAddress.getLocalHost().getHostAddress();
                //URL url = new URL("http://"+ InetAddress.getLocalHost().getHostAddress() + ":15672/api/queues");
                URL url = new URL("http://52.67.219.8:15672/api/queues/%2f/leo/bindings");
                //URL url = new URL("http://localhost:15672/api/vhosts");
                URLConnection conn = url.openConnection();
                System.out.println(InetAddress.getLocalHost().getHostAddress());
                BufferedReader buffer = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String linha;
                while ((linha = buffer.readLine()) != null)
                {
                    System.out.println(linha);

                }
                //System.out.println(url.getContent());
            }
            else if(commands[0].equals("!upload"))
            {
                //upload arquivos
                if(token_actual == '@')
                {
                    //Arquivo(String destinatario, String diretorio, String prompt, String token_actual)
                    Arquivo a1 = new Arquivo(queue_name+"_file", queue_name, ex_name_file, ex_name, commands[1], prompt, token_actual, channel2);
                    Thread t1 = new Thread(a1);
                    t1.start();
                }
                else if(token_actual == '#')
                {
                    Arquivo a2 = new Arquivo(exchange_name+"_file", queue_name, ex_name_file, ex_name, commands[1], prompt, token_actual, channel2);
                    Thread t2 = new Thread(a2);
                    t2.start();
                }
                
            }
            else
            {
                if(token_actual == '@'){
                    //Mensagem.Builder direct_msg = Mensagem.newBuilder();
                    Mensagem.Builder direct_msg = Mensagem.newBuilder();
                    
                    Mensagem.Conteudo.Builder direct_conteudo = Mensagem.Conteudo.newBuilder();
                    direct_msg.setSender(ex_name);
                    
                    String date_actual1[] = dateFormat1.format(c.getTime()).split(" ");
                    direct_msg.setDate(date_actual1[0]);
                    direct_msg.setTime(date_actual1[1]);

                    ByteString msg_Array = ByteString.copyFromUtf8(msg);
                    
                    direct_conteudo.setType("mensagem");
                    direct_conteudo.setBody(msg_Array);
                    
                    direct_conteudo.build();
                    direct_msg.setContent(direct_conteudo);

                    channel.basicPublish("", queue_name, null, direct_msg.build().toByteArray());
                }
                    
                else if(token_actual == '#')
                {
                    Mensagem.Builder group_msg = Mensagem.newBuilder();
                    
                    Mensagem.Conteudo.Builder group_conteudo = Mensagem.Conteudo.newBuilder();
                    group_msg.setSender(ex_name+prompt);
                    
                    String date_actual2[] = dateFormat1.format(c.getTime()).split(" ");
                    group_msg.setDate(date_actual2[0]);
                    group_msg.setTime(date_actual2[1]);
                    ByteString msg_Array1 = ByteString.copyFromUtf8(msg);
                    
                    group_conteudo.setType("mensagem");
                    group_conteudo.setBody(msg_Array1);
                    
                    group_conteudo.build();
                    group_msg.setContent(group_conteudo);
                    
                    channel.basicPublish(exchange_name, "", null, group_msg.build().toByteArray());
                }
            }
            
            

        }
        
        //channel.close();
        //connection.close();
    }
    //...
}