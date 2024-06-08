package TCPServer;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class ReverseClient {
    public static void main(String[] args) throws IOException {
        String serverIP; // 默认服务器 IP
        int serverPort; // 默认服务器端口号
        int Lmin = 0; // 最小数据块长度
        int Lmax = 0; // 最大数据块长度
        Scanner sc = new Scanner(System.in);
        do {
            System.out.print("请输入最小数据块长度 Lmin: ");
            Lmin = sc.nextInt();
            System.out.print("请输入最大数据块长度 Lmax: ");
            Lmax = sc.nextInt();
            if(Lmin>=Lmax){
                System.out.println("最小数据块长度 Lmin 必须小于最大数据块长度 Lmax");
            }
        }while(Lmin>=Lmax);

        System.out.print("请输入服务器IP地址: ");
        serverIP = sc.next();

        do {
            System.out.print("请输入服务器的端口号: ");
            serverPort = sc.nextInt();
            if(serverPort!=44444){
                System.out.println("Refused to connect to server");
            }
        } while (serverPort!=44444);

        sc.close();

        Socket socket = new Socket(serverIP, serverPort);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        String filePath = "D:\\IDEA\\javacode\\NetCourse\\src\\TCPServer\\test.txt"; // 文件路径
        File file = new File(filePath);
        byte[] fileData = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(fileData);
        fis.close();
       // System.out.println(fileData.length);

        int N = (int) Math.floor((double) fileData.length / Lmax);//向下取整 向上是ceil
        int type = 1;
        dos.writeInt(type);//发送type和块数N
        dos.writeInt(N);
        dos.flush();
        int type2 = dis.readInt();
        if(type2 != 2){
            System.out.println("Intialization报文的type不为1,拒绝发送agress报文");
            return;
        }else{
            //System.out.println("接收到来自服务器的agree报文");
        }

        int bytesRead = 0;
        String path = "D:\\IDEA\\javacode\\NetCourse\\src\\TCPServer\\reverse.txt";
        File f = new File(path);
        FileWriter fw = new FileWriter(f);
        for (int i = 0; i < N; i++) {
            int length = (i == N - 1) ? fileData.length - bytesRead : Lmin + new Random().nextInt(Lmax - Lmin);
            int type3 = 3;
            dos.writeInt(type3);
            dos.writeInt(length); // 发送数据长度
            dos.write(fileData, bytesRead, length); // 发送数据
            dos.flush();
            int type4 = dis.readInt();//读取来自服务器端的reverseAnswer
            if(type4!=4){
                System.out.println("接收到来自服务器的reverseAnswer报文的type不为4,出现错误");
                return;
            }else{
                //System.out.println("接收到来自服务器的reverseAnswer报文的type为4");
            }
            int reversedLength = dis.readInt(); // 读取反转数据长度
            byte[] reversedData = new byte[reversedLength];
            dis.readFully(reversedData); // 读取反转数据
            if (!f.exists()) {
                f.createNewFile(); // 如果文件不存在，则创建它
            }
            try {
                String str = new String(reversedData);
               // System.out.println(str);
                fw.write(str);
                fw.write("\n");
                fw.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println( (i + 1) + ": " + new String(reversedData));
            bytesRead += length;
        }
        fw.close();
        dis.close();
        dos.close();
        socket.close();
    }
}
