import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * {@link FileChannel#transferFrom(ReadableByteChannel, long, long)}可以将数据从ReadableByteChannel传输到FileChannel
 * 方法的输入参数position表示从position处开始向目标文件写入数据，
 * count表示最多传输的字节数。如果源通道的剩余空间小于 count 个字节，
 * 则所传输的字节数要小于请求的字节数。
 *
 * {@link FileChannel#transferTo(long, long, WritableByteChannel)}将数据从FileChannel传输到WritableByteChannel中。
 *
 *
 * 使用时要注意：
 * 在SoketChannel的实现中，SocketChannel只会传输此刻准备好的数据（可能不足count字节）。
 * 因此，SocketChannel可能不会将请求的所有数据(count个字节)全部传输到FileChannel中。
 * Created by zhaoshq on 2017/7/28.
 */
public class TransferFromAndToTest {

    public static void TransferFromAndToMethod(){
        RandomAccessFile fromFile = null;
        RandomAccessFile toFile = null;
        try
        {
            fromFile = new RandomAccessFile("scattingAndGather.txt","rw");
            FileChannel fromChannel = fromFile.getChannel();
            toFile = new RandomAccessFile("toFile.txt","rw");
            FileChannel toChannel = toFile.getChannel();

            long position = 0;
            long count = fromChannel.size();
            System.out.println(count);
            //这里的数据传输并没有ByteBuffer
//            fromChannel.transferTo(position,count,toChannel);
            fromChannel.transferFrom(toChannel,position,count);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally{
            try{
                if(fromFile != null){
                    fromFile.close();
                }
                if(toFile != null){
                    toFile.close();
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        TransferFromAndToMethod();
    }
}
