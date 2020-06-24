package com.company;

import com.sun.javafx.logging.PulseLogger;
import jdk.nashorn.internal.ir.WhileNode;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author 有梦想的咸鱼
 *
 * Channel：通道，Channel表示IO源于目标打开的连接，可以认为是程序与文件之间用于数据交换的专用通道，类似与go语言中的channel，Channel只能与Buffer交互
 * 在老版本的系统设计中，进行某个线程进行IO操作时需要阻塞线程，并且交由 CPU去读取磁盘中的数据到内存中，然后线程读取数据，但是这种方式需要消耗大量的CPU时间，因为在线程阻塞读取IO的过程中，CPU 被空出来了但是
 * 不能被用于执行事务。在新的系统设计中IO操作被使用DMA芯片来代替CPU的读取磁盘操作，这样CPU只需要在需要进行中断读取磁盘数据时将读取权限交给DMA芯片，DMA执行IO操作，在
 * DMA执行IO操作的过程中，CPU仍然可以处理其他事务，但是DMA也可能会遇到并发读取的问题（暂时无法解释），所以又使用了一个虚拟的通道Channel执行DMA的操作，也就是说在java中IO编程使用DMA进行IO操作，而NIO编程
 * 使用Channel执行IO操作
 *
 * Channel的实现类：
 * FileChannel：用于本地文件传输
 * DatagramChannel：用于网络传输
 *ScoketChannel：用于网络传输
 * ServerSocketChannel：用于网络传输
 *
 * 在Java JDK1.7以后针对上述的每个通道都提供了一个静态方法 open（）
 * 在传统IO中添加了 getChannel（）获取通道
 *
 * 分散与读取概念：
 * 分散读取：将通道中的数据分散到多个缓冲区中
 * 聚集写入：将多个缓冲区中的数据聚集到通道中
 *
 */
public class ChannelMain {
    public static void main(String[] args) throws IOException {
        test5();

    }
    /*通过fileInputStream获取通道（或fileOutputStream）*/
    public static void test1() throws IOException {
        FileInputStream inputStream = new FileInputStream("1.jpg");
        FileOutputStream outputStream = new FileOutputStream("2.jpg");

        /*获取两个通道，对应两个文件*/
        FileChannel FInput = inputStream.getChannel();
        FileChannel FOutput = outputStream.getChannel();

        /*创建缓冲区*/
        ByteBuffer buf = ByteBuffer.allocate(1024);

        /*从通道中读取数据*/
        while( FInput.read(buf) != -1){
            buf.flip();
            FOutput.write(buf);         // 向第二个通道写入数据
            buf.clear();
        }
        try {

        }catch (Exception e){

        }finally {
            FOutput.close();
            FInput.close();
            inputStream.close();
            outputStream.close();
        }

        /*从通道中写入数据*/
    }

    /*利用通道给出的open方法创建通道*/
    public static void test2() throws IOException {
        /*StandardOpenOption有多个方法参数，包括但不限于 read 和 write*/
        // 创建一个关于 1.jpg的只读通道参数可以设置多个，包括 read和 write同时出现
        FileChannel FIChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ, StandardOpenOption.WRITE);
        /*CREATE表示如果2.jpg不存在，则创建一个新的文件*/
        FileChannel FOChannel = FileChannel.open(Paths.get("3.jpg"),
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.READ
        );

        /*使用 map 方法获取缓存区*/
        // READ_ONLY表示当前缓冲区只读，从下标 0 开始，读取通道大小的数据到缓冲区中，这个缓冲map是直接缓冲，只支持byteBuffer
        MappedByteBuffer IMap= FIChannel.map(FileChannel.MapMode.READ_ONLY, 0, FIChannel.size());
        MappedByteBuffer OMap = FOChannel.map(FileChannel.MapMode.READ_WRITE, 0, FIChannel.size());

        /*直接对缓冲区进行读写操作*/
        byte[] dst = new byte[IMap.limit()];
        IMap.get(dst);
        OMap.put(dst);
        try{ }
        finally {
            FIChannel.close();
            FOChannel.close();
        }

    }

    /*通道之间直接进行数据传输*/
    public static void test3() throws IOException {
        /*StandardOpenOption有多个方法参数，包括但不限于 read 和 write*/
        // 创建一个关于 1.jpg的只读通道参数可以设置多个，包括 read和 write同时出现
        FileChannel FIChannel = FileChannel.open(Paths.get("1.jpg"), StandardOpenOption.READ, StandardOpenOption.WRITE);
        /*CREATE表示如果2.jpg不存在，则创建一个新的文件*/
        FileChannel FOChannel = FileChannel.open(Paths.get("5.jpg"),
                StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.READ
        );

        /*直接跳过缓冲区将FIChannel中的内容传递到 FOChannel中*/
//        FIChannel.transferTo(0, FIChannel.size(), FOChannel);
        /*FOChannel直接跳过缓冲区获取FIChannel中的内容，和上一句代码实现的功能一样*/
        FOChannel.transferFrom(FIChannel, 0, FIChannel.size());
        try {

        }finally {
            FIChannel.close();
            FOChannel.close();
        }
    }

    /*将通道中的数据分散到缓冲区中*/
    public static void test4() throws IOException {
        RandomAccessFile raf1 = new RandomAccessFile("1.txt", "rw");
        /*获取通道*/
        FileChannel fileChannel1 = raf1.getChannel();
        /*创建多个缓冲区*/
        ByteBuffer buf1 = ByteBuffer.allocate(100);
        ByteBuffer buf2 = ByteBuffer.allocate(1024);

        /*分散读取*/
        ByteBuffer[] bufs = {buf1, buf2};
        fileChannel1.read(bufs);

        for (ByteBuffer buf : bufs){
            System.out.println(new String((byte[]) buf.flip().array(), StandardCharsets.UTF_8));
            System.out.println("######################");
        }

        /*聚集写入*/
        RandomAccessFile raf2 = new RandomAccessFile("2.txt", "rw");
        FileChannel fileChannel2 = raf2.getChannel();
        fileChannel2.write(bufs);

        /*关闭通道*/
        try {
        }finally{
            fileChannel1.close();
            fileChannel2.close();
        }

    }

    /*通道实现编码和解码*/
    public static void test5() throws CharacterCodingException {
        /*获取标准编码集对象*/
        Charset cs1 = StandardCharsets.UTF_8;

        /*获取编码器*/
        CharsetEncoder ce = cs1.newEncoder();
        /*获取解码器*/
        CharsetDecoder cd = cs1.newDecoder();

        /*创建buffer对象*/
        CharBuffer cBuf = CharBuffer.allocate(1024);
        cBuf.put("Hello world!!");
        cBuf.flip();
        /*编码,byteBuffer中保存 cBuf中的字符串的字符集*/
        ByteBuffer byteBuffer = ce.encode(cBuf);
        System.out.println("position=" + byteBuffer.limit());
        /*解码*/
        CharBuffer cbuf2 = cd.decode(byteBuffer);
        System.out.println("limit=" + cBuf.limit());
        for (int i = 0; i < cbuf2.limit(); i++){
            System.out.println(cbuf2.get());
        }



    }

}













































