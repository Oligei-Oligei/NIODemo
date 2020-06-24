package com.company;

import sun.security.util.Length;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author 有梦想的咸鱼
 * nio缓冲区（buffer）：在Java NIO 中负责数据的存取，缓冲区就是数组，用于存储不同类型的数据
 * 根据数据类型的不同，java提供了不同类型的缓冲区（boolean类型除外）：如 ByteBuffer, CharBuffer, ShortBuffer, IntBuffer, LongBuffer FloatBuffer, DoubleBuffer
 * 这些缓冲区管理方式一致，均通过 allocate()方法获取缓冲区
 *
 * 缓冲区有直接缓冲区和非直接缓冲区两个概念：
 * 非直接缓冲区：通过 allocate() 方法分配缓冲区，将缓冲区建立在 JVM 的内存中，非直接缓冲区读取数据的过程是应用程序调用read()或write()方法从用户地址空间
 * 中读取或写入数据，然后用户地址空间在从内核地址空间中复制数据或将数据复制到内核地址空间中，而内核地址空间从物理磁盘中读取数据或写入数据的过程
 * 直接缓冲区：通过 allocateDirect() 方法分配直接缓冲区，将缓冲区建立在物理内存中，这一种方法可以提交读写效率，但是会消耗额外的内存资源，直接缓冲区弃用了用户地址空间和
 * 内核地址空间两个概念，直接使用一个物理内存实现程序和物理磁盘之间的数据交换。
 * 使用 buffer的 isDirect() 方法可以判断当前缓冲区是否是直接缓冲区，如果方法返回 true，则为缓冲区
 *
 * 缓冲区存取数据的两个核心方法为：
 * put()：存取数据到缓冲区中
 * get()：获取缓冲区中的数据
 *
 * Buffer有四个核心属性：
 * capacity：缓冲区最大存储数据的容量，一旦声明不在改变
 * limit：表示缓冲区中可以操作数据的容量大小，limit后面的数据不可以进行读写，和io的游标类似
 *position：表示缓冲区中正在操作数据的位置，使用 position和 limit结合可以知道当前可以操作的数据范围
 *
 * buffer在创建时默认是写数据模式，可以通过调用 flip()方法切换到读数据模式
 *
 * buffer的 rewind方法可以将position重新设置为 0
 *
 * buffer的 clear方法可以清空缓冲区s，这行了这个方法后缓冲区中的数据仍然存在，数据处于“被遗忘”的状态，仅仅是position和limit被返回为初始值了，当前仍然可以使用 get方法读取到数据
 *
 * buffer的 mark方法表示记录当前的position的位置，可以使用 reset()方法恢复到mark的位置
 *
 * buffer的 hasRemaining()判断position到 limit之间是否还有数据，如果有就可以使用get方法获取，或使用remaining方法获取可读取的字节数
 *
 * 最终有一个等式： 0 <= mark <= position <= limit <= capacity
 *
 *
 */
public class Main {

    public static void main(String[] args) throws UnsupportedEncodingException {
        /*分配一个指定大小的缓冲区，使用allocate(int buffer)*/
        ByteBuffer buf = ByteBuffer.allocate(1024);
        test2(buf);
        buf.remaining();
        test1(buf);
        /**/
        test3(buf);
        test1(buf);
        /*读取数据*/
        test4(buf);
        test1(buf);
        /*将position重设置为 0*/
        test5(buf);
        test1(buf);
        /*清空缓冲区*/
        test6(buf);
        test1(buf);
    }

    public static void test1(ByteBuffer buf){
        System.out.println(buf.position());
        System.out.println(buf.limit());
        System.out.println(buf.capacity());
    }

    public static void test2(ByteBuffer buf){
        /*向缓冲区中存入数据*/
        buf.put("Hello World".getBytes());
        System.out.println("写入数据长度" + "Hello World".getBytes().length);
    }

    public static void test3(ByteBuffer buf){
        /*切换到写数据模式*/
        buf.flip();
    }

    public static void test4(ByteBuffer buf) throws UnsupportedEncodingException {
        /*读取数据*/
        byte[] by = new byte[buf.limit()];
        buf.get(by);
        System.out.println(new String(by, StandardCharsets.UTF_8));
    }

    public static void test5(ByteBuffer buf){
        buf.rewind();
    }

    public static void test6(ByteBuffer buf){
        buf.clear();
    }
}













