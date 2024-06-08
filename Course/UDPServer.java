package Course;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.logging.SimpleFormatter;

public class UDPServer {
    private static final int PORT = 12345;
    private static final int BUFFER_SIZE = 1024;

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(PORT);
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
        System.out.println("Server started. Listening on port " + PORT);
        socket.receive(request);
        String receiveConnnect = new String(request.getData(),0,request.getLength());
        if(receiveConnnect.equals("CONNECT")){
            System.out.println("客户端请求连接...");
            System.out.println("许可");
            String responseConnectString = "许可";
            byte[] responseByteConnect = responseConnectString.getBytes();
            DatagramPacket responseConnect = new DatagramPacket(responseByteConnect,0,responseByteConnect.length,request.getAddress(), request.getPort());
            socket.send(responseConnect);
        }

        while(true){
            socket.receive(request);
            String seqNo= new String(request.getData(), 0, request.getLength()).split("\\|")[0];
            String requestString = new String(request.getData(), 0, request.getLength());
            //打印输出
            System.out.println(" 第"+seqNo+"个Packet "+"Received request: " + requestString);


            if(new Random().nextDouble()<0.4){
                //打印输出
                System.out.println(" 第"+seqNo+"个"+"Packet lost");
                continue;
            }
            String currentTime = new SimpleDateFormat("HH-mm-ss").format(new Date());
            String responseString = requestString.split("\\|")[0]+"|"+ requestString.split("\\|")[1]+ "|" + currentTime;
            byte[] responseStringBuffer = responseString.getBytes();
            DatagramPacket responsePacket = new DatagramPacket(responseStringBuffer,responseStringBuffer.length,request.getAddress(),request.getPort());
            socket.send(responsePacket);
            //打印输出
            System.out.println(" 第"+seqNo+"个 Packet "+"Responded: " + responseString+"\n");
        }
    }
}
