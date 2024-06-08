package TCPServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPServerReadThread extends Thread {
    private Socket clientSocket;

    public TCPServerReadThread(Socket socket) {
        this.clientSocket = socket;
    }
    @Override
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());
            int type = dis.readInt();
            //System.out.println(type);
            if(type !=1){//初始报文的类型必须为1
                System.out.println("Intialization报文的type不为1");
                dos.writeInt(-1);
                return;
            }else{
                dos.writeInt(2); //agree报文
            }
            int N = dis.readInt(); // 读取块数
            //System.out.println(N);


            for (int i = 0; i < N; i++) {
                int type3 = dis.readInt();//读取type的类型
                int length = dis.readInt(); // 读取数据长度

                System.out.println(type3);
                if(type3 != 3){
                    System.out.println("此报文并不是reverseRequest报文");
                }else{
                    System.out.println("接收到来自Client端的reverseRequest报文");
                }
                byte[] data = new byte[length];
                dis.readFully(data);

                byte[] reversedData = new byte[length];
                for(int j=0; j<length/2;j++){
                    byte temp = data[j];
                    data[j] = data[length-j-1];
                    data[length-j-1] = temp;
                }
                for(int j=0;j<length;j++){
                    reversedData[j]  = data[j];
                }
                int type4 = 4;
                dos.writeInt(type4); //发送type
                dos.writeInt(reversedData.length); // 发送反转数据长度
                dos.write(reversedData); // 发送反转数据
                dos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
    }
}
