package com.company;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * @author 有梦想的咸鱼
 * 使用 NIO实现socket通信需要三个对象：
 * 通道：负责连接数据对象
 * 缓冲区：负责数据的存取
 * 选择器：即 SelectableChannel的多路复用器，用于监控 SelectableChannel 的IO状况
 *
 * 选择器有四个常量分别为：
 * SelectionKey.OP_READ(1)  监控只读
 * SelectionKey.OP_WRITE(4) 监控只写
 * SelectionKey.OP_CONNECT(8) 监控连接状态
 * SelectionKey.OP_ACCEPT(16) 监控接收状态
 * 这四个常量用于表示监听当前注册的通道的哪些事件，一般服务器的 serverSocketChannel只需要监听 OP_ACCENT，其他的监听 OP_READ或 OP_WRITE等
 */
public class NotBlockIO {
    public static void main(String[] args) throws IOException {
        NotBlockIO notBlockIO = new NotBlockIO();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    notBlockIO.server();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    notBlockIO.client();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
        t2.start();
//        notBlockIO.server();
//        notBlockIO.client();
    }
    /*非阻塞式socket编程，使用选择器Selector*/
    /*客户端*/
    public void client() throws IOException {
        System.out.println("启动客户端");
        SocketChannel client = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8989));
        /*切换为非阻塞模式*/
        client.configureBlocking(false);
        /*分配缓冲区*/
        ByteBuffer buf = ByteBuffer.allocate(1024);
        /**/
        buf.put("client：服务器您好!!!".getBytes());
        buf.flip();
        client.write(buf);
        buf.clear();
        /*关闭资源*/
        try {
        }finally {
            client.close();
        }
    }

    /*服务器*/
    public void server() throws IOException {
        System.out.println("启动服务端");
        ServerSocketChannel server = ServerSocketChannel.open();
        /*设置为非阻塞模式*/
        server.configureBlocking(false);
        /*绑定端口号*/
        server.bind(new InetSocketAddress(8989));
        /*获取选择器*/
        Selector selector = Selector.open();
        /*将通道注册到选择器，SelectionKey看上方类信息*/
        /*对server监听 OP_ACCEPT和 OP_READ两个事件*/
        server.register(selector,SelectionKey.OP_ACCEPT , SelectionKey.OP_READ);


        /*轮询式获取选择器上已经“准备就绪”的事件*/
        while (selector.select() > 0){
            /*获取选择器中的所有选择键，即已就绪的监听事件*/
            Iterator<SelectionKey> it =selector.selectedKeys().iterator();
            while (it.hasNext()){
                /*遍历获取准备就绪的事件*/
                SelectionKey sk = it.next();
                /*判断具体就绪事件*/
                /*接收事件就绪，接收客户端Socket连接请求*/
                if (sk.isAcceptable()){
                    SocketChannel serverSocket = server.accept();
                    /*切换成非阻塞通道*/
                    serverSocket.configureBlocking(false);
                    /*将通道注册到选择器上*/
                    serverSocket.register(selector, SelectionKey.OP_READ, SelectionKey.OP_WRITE);
                } else if(sk.isReadable()) {
                    /*获取读就绪状态通道*/
                    SocketChannel readChannel = (SocketChannel) sk.channel();
                    /*读取数据*/
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    int len = 0;
                    while ((len = readChannel.read(buf)) > 0){
                        buf.flip();
                        System.out.println(new String(buf.array(), 0, len, StandardCharsets.UTF_8));
                        buf.clear();
                    }
                }
                /*取消选择键*/
                it.remove();
            }
        }

    }




    /*阻塞式socket编程*/
    /*客户端*/
    public void client2() throws IOException, InterruptedException {
        System.out.println("启动客户端");
        /*获取通道*/
        SocketChannel client = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9898));
        /*创建缓冲区*/
        ByteBuffer buf = ByteBuffer.allocate(1024);

        /*读取数据并发送到服务端*/
        System.out.println("客户端写入数据");
        String str = "client：hello world";
        buf.put(str.getBytes());
        System.out.println("position = " + buf.position() +"\tlimit = " + buf.limit());
        buf.flip();
        client.write(buf);
        buf.clear();
        try {
        }finally {
            client.close();
        }
    }

    /*服务器端*/
    public void server2() throws IOException {
        System.out.println("启动服务器");
        ServerSocketChannel server = ServerSocketChannel.open();
        /*绑定端口号*/
        server.bind(new InetSocketAddress(9898));
        /*获取客户端连接的通道*/
        SocketChannel serverChannel = server.accept();
        /*获取数据*/
        ByteBuffer buf = ByteBuffer.allocate(1024);
        while (serverChannel.read(buf) != -1){
/*            buf.flip();*/
            System.out.println("position = " + buf.position() + "\tlimit = " + buf.limit());
            System.out.println("服务器端接收数据");
            System.out.println(new String(buf.array(), StandardCharsets.UTF_8));
        }
        try {
        }finally {
            serverChannel.close();
            server.close();
        }
    }

}




















