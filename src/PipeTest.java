import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * java NIO 中的{@link Pipe}是2个线程之间的单向数据连接。
 * {@link Pipe}有一个source通道和一个sink通道。数据会被写到sink通道，从source通道读取。
 * Created by zhaoshq on 2017/7/28.
 */
public class PipeTest {

    public static void pipeMethod(){
        Pipe pipe = null;
        ExecutorService exec = Executors.newFixedThreadPool(2);

        try {
            pipe = Pipe.open();
            final Pipe pipetemp = pipe;

            exec.submit(new Runnable() {
                @Override
                public void run()  {
                    try {
                        Pipe.SinkChannel sinkChannel = pipetemp.sink(); //向通道内写数据
                        while (true){
                            TimeUnit.SECONDS.sleep(1);
                            String datas = "Pipe Test At Time "+System.currentTimeMillis();
                            ByteBuffer buffer = ByteBuffer.allocate(1024);

                            buffer.clear();
                            buffer.put(datas.getBytes());

                            buffer.flip();
                            while (buffer.hasRemaining()){
                                sinkChannel.write(buffer);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

            exec.submit(new Runnable() {
                @Override
                public void run()  {
                    try {
                        Pipe.SourceChannel sourceChannel = pipetemp.source(); //从通道内读数据
                        while (true){
                            TimeUnit.SECONDS.sleep(3);
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            //从buffer中写数据
                            buffer.clear();
                            int bytesread = sourceChannel.read(buffer);
                            while (bytesread != -1){

                                //从buffer中读数据
                                buffer.flip();
                                while (buffer.hasRemaining()){
                                    System.out.print((char) buffer.get());
                                }
                                System.out.println();
                                //从buffer中写数据
                                buffer.clear();
                                bytesread = sourceChannel.read(buffer);
                                TimeUnit.SECONDS.sleep(3);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        pipeMethod();
    }
}
