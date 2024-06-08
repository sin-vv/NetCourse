package Course;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class UDPClient {
    private static  String SERVER_IP;
    private static  int SERVER_PORT;
    private static final int BUFFER_SIZE = 1024;
    private static final int TIMEOUT = 100; // 超时时间（毫秒）
    private static final int MAX_RETRIES = 2; // 最大重试次数

    public static void main(String[] args) throws SocketException, Exception {
        DatagramSocket socket = new DatagramSocket();
        Scanner sc = new Scanner(System.in);

        System.out.print("请输入服务器IP地址: ");
        SERVER_IP = sc.nextLine();

        System.out.print("请输入服务器的端口号: ");
        SERVER_PORT = sc.nextInt();
        if (SERVER_PORT < 1 || SERVER_PORT > 65535) {
            System.out.println("端口号必须在1到65535之间。");
            System.exit(1);
        }
        sc.close();
        InetAddress address = InetAddress.getByName(SERVER_IP);

        System.out.println("模拟TCP连接过程启动");
        System.out.println("尝试与服务器端连接..." );
        String connect = "CONNECT";
        byte[] bufferconnect = connect.getBytes();
        DatagramPacket connectPacket = new DatagramPacket(bufferconnect, bufferconnect.length,address,SERVER_PORT);
        socket.send(connectPacket);

        byte[] responseConnect = new byte[BUFFER_SIZE];
        DatagramPacket connectRespnse = new DatagramPacket(responseConnect, responseConnect.length);
        socket.receive(connectRespnse);
        String responseConnectData = new String(connectRespnse.getData(),0,connectRespnse.getLength());
        if(responseConnectData.equals("许可")){
            System.out.println("连接成功,那我可要发送Packet了哦");
        }




        List<Long> rttList = new ArrayList<Long>();
        List<String> responseTimes = new ArrayList<String>();
        for(int seqNo = 1;seqNo<=12;seqNo++) {
            String requestData = seqNo + "|2|";
            requestData +=generateRandomData(200);

            byte[] buffer = requestData.getBytes();
            DatagramPacket request = new DatagramPacket(buffer,buffer.length,address,SERVER_PORT);

            boolean  receivedResponse = false;
            int retries = 0;

            while (!receivedResponse && retries < MAX_RETRIES) {
                long startTime = System.currentTimeMillis();
                socket.send(request);

                socket.setSoTimeout(TIMEOUT);

                try {
                    byte[] responseBuffer = new byte[BUFFER_SIZE];
                    DatagramPacket response = new DatagramPacket(responseBuffer, responseBuffer.length);
                    socket.receive(response);

                    long endTime = System.currentTimeMillis();
                    long rtt = endTime - startTime;
                    rttList.add(rtt);

                    String responseData = new String(response.getData(), 0, response.getLength());

                    String serverTime = responseData.split("\\|")[2];
                    responseTimes.add(serverTime);

                    receivedResponse = true;
                    System.out.println("第"+seqNo+"个 Packet "+"Response: " + responseData +" sequence no:"+responseData.split("\\|")[0] +
                          " serverIP:"+ response.getPort() +", RTT: " + rtt + "ms"+"\n");
                } catch (SocketTimeoutException e) {
                    retries++;
                    System.out.println("第"+seqNo+"个 request Packet"+"Timeout. Retrying...");
                }
            }
            if (!receivedResponse) {
                System.out.println("Has failed to receive response for packet " + seqNo+"\n");
            }
        }
        socket.close();

        System.out.println("Summary:");
        System.out.println("Received UDP packets: " + rttList.size());
        System.out.println("Losted packets: " + (12 - rttList.size()));
        System.out.println("Lost rate: " + ((12 - rttList.size()) / 12.0) * 100 + "%");

        if (!rttList.isEmpty()) {
            long maxRTT = Long.MIN_VALUE;
            long minRTT = Long.MAX_VALUE;
            long sumRTT = 0;

            for (long rtt : rttList) {
                if (rtt > maxRTT) {
                    maxRTT = rtt;
                }
                if (rtt < minRTT) {
                    minRTT = rtt;
                }
                sumRTT += rtt;
            }

            double avgRTT = (double) sumRTT / rttList.size();

            double stdDevRTT = calculateStandardDeviation(rttList);

            System.out.println("Max RTT: " + maxRTT + "ms, Min RTT: " + minRTT + "ms, Avg RTT: " + avgRTT + "ms, RTT Standard Deviation: " + stdDevRTT + "ms");
        }

        if (!responseTimes.isEmpty()) {
            String firstResponseTime = responseTimes.get(0);
            String lastResponseTime = responseTimes.get(responseTimes.size() - 1);//得到首和尾
            long serverResponseTime = calculateResponseTimeDifference(firstResponseTime, lastResponseTime);
            //打印server整体的响应时间
            System.out.println("Server's overall response time: " + serverResponseTime + " s");
        }
    }

    private static long calculateResponseTimeDifference(String firstResponseTime, String lastResponseTime) throws Exception {
        SimpleDateFormat format  = new SimpleDateFormat("HH-mm-ss");
        long firstTime = format.parse(firstResponseTime).getTime();
        long lastTime = format.parse(lastResponseTime).getTime();
        //System.out.println(firstTime+","+lastTime);
        return (lastTime-firstTime)/1000;//输出秒
    }

    private static double calculateStandardDeviation(List<Long> rttList) {
        long sum =0;
        int n = rttList.size();

        for (long rtt: rttList) {
            sum+=rtt;
        }
        double ave = (double)sum/n;
        double Dev=0;
        for(long rtt: rttList) {
            Dev+=(rtt-ave)*(rtt-ave);
        }
        double stdDev = (double)Math.sqrt(Dev/n);
        return stdDev;
    }

    private static String generateRandomData(int len) {
        Random r  = new Random();
        StringBuilder str = new StringBuilder();
        for (int i =0;i<len;i++){
            str.append((char)('a'+r.nextInt(26)));
        }
        return str.toString();
    }
}
