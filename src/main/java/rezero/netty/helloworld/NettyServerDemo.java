package rezero.netty.helloworld;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * @Author : BirdSnail
 * @Date : 2020/11/28
 */
public class NettyServerDemo {

//    private static int port = 10001;

    public static void main(String[] args) throws InterruptedException {

        new NettyServerDemo().start(8888);
    }

    private void start(int port) throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            // 1. 创建一个ServerBootStrap
            // 2. 确定线程模型
            // 3. 自定义ChannelHandler
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .channel(NioServerSocketChannel.class)
//                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pl = socketChannel.pipeline();
                            pl.addLast(new HelloServerHandler());
//                            pl.addLast(new LineBasedFrameDecoder(1024));//很重要哦
                        }
                    });
            // 同步方式绑定端口
            ChannelFuture ch = serverBootstrap.bind(port).sync();
//            Thread.sleep(100000);
            ch.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


    /**
     * 处理IO事件
     */
    static class HelloServerHandler extends ChannelHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                ByteBuf in = (ByteBuf) msg;
                System.out.println("message from client:" + in.toString(CharsetUtil.UTF_8));
                // 发送消息给客户端
                ctx.writeAndFlush(Unpooled.copiedBuffer("你也好！", CharsetUtil.UTF_8));
            } finally {
                ReferenceCountUtil.release(msg);
                System.out.println("message was  released");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("error message: " + cause.getMessage());
            ctx.close();
        }
    }

}
