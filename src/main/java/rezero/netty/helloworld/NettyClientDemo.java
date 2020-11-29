package rezero.netty.helloworld;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.util.function.Supplier;

/**
 * @Author : BirdSnail
 * @Date : 2020/11/28
 */
public class NettyClientDemo {

    private static String host = "localhost";
    private static int port = 8888;

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HelleClientHandler(() -> "hello every one"));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }

    }

    static class HelleClientHandler extends ChannelHandlerAdapter {

        private final Supplier<String> messageProducer;

        public HelleClientHandler(Supplier<String> messageProducer) {
            this.messageProducer = messageProducer;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            String mes = messageProducer.get();
            System.out.println("client send message: " + mes);
            ctx.writeAndFlush(Unpooled.copiedBuffer(mes, CharsetUtil.UTF_8));
        }


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf) msg;
            try {
                System.out.println("client receive msg from server: " + in.toString(CharsetUtil.UTF_8));
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
