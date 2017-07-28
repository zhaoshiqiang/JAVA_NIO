import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * NIO中的{@link java.nio.channels.Channel}的主要实现有四种：
 *      {@link FileChannel}对应文件IO
 *      {@link java.nio.channels.DatagramChannel}对应UDP
 *      {@link java.nio.channels.SocketChannel}对应TCP中的client
 *      {@link java.nio.channels.ServerSocketChannel}对应TCP中的server端
 * 这里是介绍{@link FileChannel}的使用
 * Created by zhaoshiqiang on 2017/7/27.
 */
public class FileChannelTest {

    public static void fileInputStreamMethod(String filePath){
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filePath));
            byte[] buf = new byte[1024];
            int byteRead = in.read(buf);
            while (byteRead != -1){
                for (int i = 0; i <byteRead; i++) {
                    System.out.print((char) buf[i]);
                }
                byteRead = in.read(buf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 使用Buffer一般遵循下面几个步骤：
     * 1、分配空间（ByteBuffer buf = ByteBuffer.allocate(1024)；还有通过文件映射{@link FileChannel#map(FileChannel.MapMode, long, long)}来分配）
     * 2、写入数据到Buffer（int bytesRead = fileChannel.read(buf)）
     * 3、调用{@link Buffer#flip()}方法(将缓冲区从写模式转为读模式)
     * 4、从Buffer中读取数据（System.out.println((char)buf.get());）
     * 5、调用{@link Buffer#clear()}方法或者{@link ByteBuffer#compact()}方法（将缓冲区从读模式转为写模式）
     * 6、重复2-5，直到bytesRead != -1
     *
     * Buffer中几个方法说明：
     * {@link ByteBuffer #filp()}：position设回0，并将limit设成之前的position的值
     * {@link Buffer#clear()}：position将被设回0，limit设置成capacity，换句话说，Buffer被清空了，其实Buffer中的数据并未被清空，只是这些标记告诉我们可以从哪里开始往Buffer里写数据。如果Buffer中有一些未读的数据，调用clear()方法，数据将“被遗忘”，意味着不再有任何标记会告诉你哪些数据被读过，哪些还没有。
     * {@link ByteBuffer#compact()}：将所有未读的数据拷贝到Buffer起始处。然后将position设到最后一个未读元素正后面。如果Buffer中仍有未读的数据，且后续还需要这些数据，但是此时想要先先写些数据，那么使用compact()方法。
     *
     * @param filePath 读取文件的地址
     */
    public static void fileChannelMethod(String filePath){
        RandomAccessFile aFile = null;
        try {
            aFile = new RandomAccessFile(filePath,"rw");
            FileChannel fileChannel = aFile.getChannel();   //这里也可以通过FileInputStream.getChannel()来获取FileChannel
            //分配空间
            ByteBuffer buf = ByteBuffer.allocate(1024);
            //写数据
            int bytesRead = fileChannel.read(buf);
            System.out.println(bytesRead);

            while (bytesRead != -1){
                //将缓冲区从读模式转为写模式
                buf.flip();
                while (buf.hasRemaining()){
                    //从buffer中读取数据
                    System.out.print((char)buf.get());
                }
                //将缓冲区从写模式转为读模式
                buf.compact();
                bytesRead = fileChannel.read(buf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (aFile != null){
                try {
                    aFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void main(String[] args) {
        fileInputStreamMethod("E:\\ProgramZhaoShiqiang\\JAVA_NIO\\.gitignore/.gitignore");
        fileChannelMethod("E:\\ProgramZhaoShiqiang\\JAVA_NIO\\.gitignore/.gitignore");
    }
}
