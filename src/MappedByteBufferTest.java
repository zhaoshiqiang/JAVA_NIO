import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * JAVA处理大文件，一般用BufferedReader,BufferedInputStream这类带缓冲的IO类，不过如果文件超大的话，更快的方式是采用MappedByteBuffer。
 * MappedByteBuffer是NIO引入的文件内存映射方案，读写性能极高。
 *
 * ByteBuffer有两种模式:直接/间接.
 * 间接模式最典型(也只有这么一种)的就是HeapByteBuffer,即操作堆内存 (byte[]).
 * 但是内存毕竟有限,如果我要发送一个1G的文件怎么办?不可能真的去分配1G的内存.这时就必须使用”直接”模式,即 MappedByteBuffer,文件映射.
 *
 * 谈谈操作系统的内存管理.一般操作系统的内存分两部分:物理内存;虚拟内存。
 * 虚拟内存一般使用的是页面映像文件,即硬盘中的某个(某些)特殊的文件。
 * 操作系统负责页面文件内容的读写,这个过程叫”页面中断/切换”。
 * MappedByteBuffer可以将文件直接映射到虚拟内存。
 * 通常，可以映射整个文件，即把整个文件都映射成MappedByteBuffer，如果文件比较大的话可以分段进行映射，只要指定文件的那个部分就可以。
 *
 * MappedByteBuffer是ByteBuffer的子类，其扩充了三个方法：
 *      force()：缓冲区是READ_WRITE模式下，此方法对缓冲区内容的修改强行写入文件；
 *      load()：将缓冲区的内容载入内存，并返回该缓冲区的引用；
 *      isLoaded()：如果缓冲区的内容在物理内存中，则返回真，否则返回假；
 *
 * Created by zhaoshq on 2017/7/28.
 */
public class MappedByteBufferTest {

    public static void ByteBufferRead(String filePath){

        RandomAccessFile afile = null;
        FileChannel fc = null;
        try {
            afile = new RandomAccessFile(filePath,"rw");
            fc = afile.getChannel();

            long timeBegin = System.currentTimeMillis();
            ByteBuffer buffer = ByteBuffer.allocate((int) afile.length());
            fc.read(buffer);
//            System.out.println((char)buffer.get((int)(afile.length()/2-1)));
            long timeEnd = System.currentTimeMillis();
            System.out.println("ByteBuffer Read time: "+(timeEnd-timeBegin)+"ms");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (afile != null){
                    afile.close();
                }
                if (fc != null){
                    fc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void MappedByteBufferRead(String filePath){
        RandomAccessFile afile = null;
        FileChannel fc = null;
        try {
            afile = new RandomAccessFile(filePath,"rw");
            fc = afile.getChannel();

            long timeBegin = System.currentTimeMillis();
            /**
            * MappedByteBuffer map(int mode,long position,long size);
            * 可以把文件的从position开始的size大小的区域映射为内存映像文件，mode指出了可访问该内存映像文件的方式：
            * READ_ONLY,（只读）： 试图修改得到的缓冲区将导致抛出 ReadOnlyBufferException.(MapMode.READ_ONLY)
            * READ_WRITE（读/写）： 对得到的缓冲区的更改最终将传播到文件；该更改对映射到同一文件的其他程序不一定是可见的。 (MapMode.READ_WRITE)
            * PRIVATE（专用）： 对得到的缓冲区的更改不会传播到文件，并且该更改对映射到同一文件的其他程序也不是可见的；相反，会创建缓冲区已修改部分的专用副本。 (MapMode.PRIVATE)
            * */
            MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY,0,afile.length());
//            System.out.println((char)buffer.get((int)(afile.length()/2-1)));
            long timeEnd = System.currentTimeMillis();
            System.out.println("MappedByteBuffer Read time: "+(timeEnd-timeBegin)+"ms");
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (afile != null){
                    afile.close();
                }
                if (fc != null){
                    fc.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        //35M的文件
        MappedByteBufferRead("d:/YoudaoDict_7.0.1.214_alading.exe");    //输出 1ms
        ByteBufferRead("d:/YoudaoDict_7.0.1.214_alading.exe");  //输出 47ms
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~");
        //1.13G的文件
        MappedByteBufferRead("d:/visio2010_12530.rar"); //输出 1ms
        ByteBufferRead("d:/visio2010_12530.rar");   //输出799ms

    }
}
