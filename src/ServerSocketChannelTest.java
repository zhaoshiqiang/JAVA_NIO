import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 使用{@link ServerSocketChannel}的步骤如下：
 * 1、创建一个Selector实例
 * 2、将该实例注册到各种通道，指定每个通道上感兴趣的I/O操作
 * 3、重复执行
 *      a、调用一种select()方法
 *      b、选取已选键集
 *      c、对于已选键集中的每一个键
 *          i 将已选键从键集中移除
 *          ii 获取行到，并从键中获取附件（如果需要）
 *          iii 确定准备就绪的操作并执行；对于accept操作获得的SocketChannel对象，需将信道设置为非阻塞模式，并将其注册到选择器中
 *          iv 根据需要，修改键的兴趣操作集
 *
 * Created by zhaoshiqiang on 2017/7/27.
 */
public class ServerSocketChannelTest {
    private static final int BUF_SIZE=1024;
    private static final int PORT = 8080;
    private static final int TIMEOUT = 3000;

    public static void server(){
        selector();
    }

    /**
     * {@link Selector}使用步骤：
     * 1、通过{@link Selector#open()}创建：Selector selector = Selector.open();
     * 2、为了将Channel和Selector配合使用，通过{@link ServerSocketChannel#register(Selector, int, Object)} or {@link ServerSocketChannel#register(Selector, int)}将Channel注册到Selector上
     * 3、通过调用几个重载的{@link Selector#select()}方法,这个方法会阻塞，直到至少有一个通道在你注册的事件上就绪了。
     * 4、通过调用{@link Selector#selectedKeys()}得到已选择键集中的就绪通道selectionKeySet
     * 5、迭代selectionKeySet，处理{@link SelectionKey}中对应的各种事件
     * 6、使用完后通过{@link Selector#close()}关闭选择器
     *  注意点：
     * 1、{@link ServerSocketChannel#register(Selector, int, Object)}的第二个参数。这是一个“interest集合”，意思是在通过Selector监听Channel时对什么事件感兴趣。其值为{@link SelectionKey#OP_CONNECT}，{@link SelectionKey#OP_ACCEPT}，{@link SelectionKey#OP_READ}，{@link SelectionKey#OP_WRITE}，其含义可以见代码
     * 2、迭代selectionKeySet，处理完SelectionKey后，要调用Iterator.remove()将其从selectionKeySet移除。Selector不会自己从已选择键集中移除SelectionKey实例。必须在处理完通道时自己移除。下次该通道变成就绪时，Selector会再次将其放入已选择键集中。
     * 3、与Selector一起使用时，Channel必须处于非阻塞模式下。这意味着不能将FileChannel与Selector一起使用，因为FileChannel不能切换到非阻塞模式，而套接字通道都可以
     * 4、select()方法返回的int值表示有多少通道已经就绪。亦即，自上次调用select()方法后有多少通道变成就绪状态。
     *
     * {@link SelectionKey}的使用：
     * 这个类中包含的属性有：1）interest集合 2）ready集合 3）Channel 4）Selector 5）附加的对象（可选）
     * interest集合：是你所选择的感兴趣的事件集合。可以通过SelectionKey读写interest集合。
     * ready 集合:是通道已经准备就绪的操作的集合
     * Channel获取: Channel  channel  = selectionKey.channel();
     * Selector获取:Selector selector = selectionKey.selector();
     * 附加的对象：可以将一个对象或者更多信息附着到SelectionKey上，这样就能方便的识别某个给定的通道，eg与通道一起使用的Buffer，或是包含聚集数据的某个对象。使用方法如下：
         * <code>
         * selectionKey.attach(theObject);
         * Object attachedObj = selectionKey.attachment();
         * </code>
     *
     */
    private static void selector() {
        Selector selector = null;
        ServerSocketChannel ssc = null;
        try {
            //打开一个选择器
            selector = Selector.open();
            //打开一个ServerSocketChannel
            ssc = ServerSocketChannel.open();
            //绑定到TCP端口上，ServerSocketChannel不提供bind方法，需要使用内部的socket对象来绑定
            ssc.socket().bind(new InetSocketAddress(PORT));
            //与Selector一起使用时，Channel必须处于非阻塞模式下。
            ssc.configureBlocking(false);
            //注册事件
            ssc.register(selector, SelectionKey.OP_ACCEPT);

            while (true){
                //该方法会阻塞等待，直到有一个或更多的信道准备好了I/O操作或等待超时
                if (selector.select(TIMEOUT) == 0){
                    System.out.println("==");
                    continue;
                }
                Set<SelectionKey> selectionKeySet = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectionKeySet.iterator();
                while (iter.hasNext()){
                    SelectionKey key = iter.next();
                    //注意要将已选键从键集中移除
                    iter.remove();
                    //接收就绪，调用isValid保证通道有效
                    if (key.isValid() && key.isAcceptable()){
                        handleAccept(key);
                    }
                    //有一个数据可读通道，读就绪
                    if (key.isValid() && key.isReadable()){
                        handleRead(key);
                    }
                    //有一个等待写数据的通道，写就绪
                    if (key.isValid() && key.isWritable()){
                        handleWrite(key);
                    }
                    //连接就绪
                    if (key.isValid() && key.isConnectable()){
                        System.out.println("isConnectable = true");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
                try {
                    if (selector != null){
                        selector.close();
                    }
                    if (ssc != null){
                        ssc.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    private static void handleWrite(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buf = (ByteBuffer) key.attachment();
        //将buf中的数据写入channel
        buf.flip();
        while (buf.hasRemaining()){
            sc.write(buf);
        }
    }

    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        ByteBuffer buf = (ByteBuffer) key.attachment();
        //非阻塞模式下,read()方法在尚未读取到任何数据时可能就返回了。所以需要关注它的int返回值，它会告诉你读取了多少字节。
        int bytesRead = sc.read(buf);
        while (bytesRead > -1){
            //buf读
            buf.flip();
            while (buf.hasRemaining()){
                System.out.print((char) buf.get());    //读取buf中的数据
            }
            //buf写
            buf.clear();
            sc.read(buf);   //将channel内的数据写入buf中
        }
        if (bytesRead == -1){
            sc.close();
        }
    }

    private static void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
        //因为收到连接事件而进来，所以此方法会立刻返回
        SocketChannel sc = ssChannel.accept();
        //配置为异步模式
        sc.configureBlocking(false);
        //注册监听事件，这里用ByteBuffer做附件
        sc.register(key.selector(),SelectionKey.OP_READ, ByteBuffer.allocateDirect(BUF_SIZE));
    }

    public static void main(String[] args) {
        server();
    }
}
