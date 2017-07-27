import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaoshiqiang on 2017/7/27.
 */
public class SocketChannelTest {
    private static final int BUF_SIZE=1024;
    private static final int PORT = 8080;

    /**
     *SocketChannel的用法如下：
     * 1、打开SocketChannel（通过{@link SocketChannel#open()}）
     * 2、连接端口（通过{@link SocketChannel#connect(SocketAddress)}
     * 3、读取/写入数据
     * 4、关闭（通过{@link SocketChannel#close()}）
     *
     *
     *  向Buffer中写数据方法有两个：
     *  从Channel写到Buffer (fileChannel.read(buf))
     *  通过Buffer的put()方法 （buf.put(…)）
     *  其步骤如下：
     * 1、调用{@link Buffer#clear()}方法或者{@link ByteBuffer#compact()}方法
     * 2、通过channel.read(buf)或者buf.put(…)写入数据
         * 若是通过buf.put(…)写入数据，直接写就好，其结构为：
         * <code>
         *     buffer.clear();
         *     buffer.put(info.getBytes());
         * </code>
         * 若是通过channel.read(buf)来写数据，其结构为：
         * <code>
         *      int bytesRead = fileChannel.read(buf);
         *      while(byteRead != -1){
         *          //处理buf中的数据
         *          ......
         *          buf.clear(); // or buf.compact();
         *          bytesRead = fileChannel.read(buf);
         *      }
         * </code>
     *
     *
     * 从Buffer中读取数据的方法有两个：
     *  从Buffer读取到Channel (channel.write(buf))
     *  使用get()方法从Buffer中读取数据 （buf.get()）
     * 其步骤如下：
     * 1、调用{@link Buffer#flip()}方法
     * 2、通过channel.write(buf)或者buf.get()读入数据
         * 若是通过buf.get()读入数据，直接读就好，其结构为：
         * <code>
         *     buf.flip();
         *     while (buf.hasRemaining()){
         *          System.out.print(buf.get());
         *     }
         * </code>
         * 若是通过channel.write(buf)来读数据，其结构为：
         * <code>
         *     buf.flip();
         *     while (buffer.hasRemaining()){
         *          socketChannel.write(buffer);
         *     }
         * </code>
     */
    public static void client(){
        ByteBuffer buffer = ByteBuffer.allocate(BUF_SIZE);
        SocketChannel socketChannel = null;

        try {
            //打开SocketChannel
            socketChannel = SocketChannel.open();
            //配置为非阻塞模式
            socketChannel.configureBlocking(false);
            //连接端口
            socketChannel.connect(new InetSocketAddress("127.0.0.1",PORT));
            if (socketChannel.finishConnect()){
                int i = 0;
                //写入数据
                while (true){
                    TimeUnit.SECONDS.sleep(1l);
                    String info = "I'm "+i+++"-th information from client";
                    //想buffer中写数据
                    buffer.clear();
                    buffer.put(info.getBytes());
                    //从buffer中读数据
                    buffer.flip();
                    while (buffer.hasRemaining()){
                        System.out.println(buffer);
                        //这里要放在循环中，Write()方法无法保证能写多少字节到SocketChannel。所以，我们重复调用write()直到Buffer没有要写的字节为止。
                        socketChannel.write(buffer);
                    }
                    if (i == 10000){
                        i=0;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }finally {
            if (socketChannel != null){
                try {
                    //关闭连接
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        client();
    }
}
