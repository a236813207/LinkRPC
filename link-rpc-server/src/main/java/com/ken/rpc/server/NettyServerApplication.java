package com.ken.rpc.server;

import com.ken.rpc.server.config.NettyProperty;
import com.ken.rpc.server.handler.NettyRpcServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


/**
 * @author ken
 * @version 1.0
 * @date 2021-03-28
 */
@SpringBootApplication(scanBasePackages = {"com.ken.rpc.server.**"})
@EnableConfigurationProperties({NettyProperty.class})
@Slf4j
public class NettyServerApplication {

    private NettyProperty nettyProperty;

    public static void main( String[] args ) {
        start();
    }

    public static void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NettyServerChannelHandler())
                .option(ChannelOption.SO_BACKLOG, 256)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            ChannelFuture future = bootstrap.bind(9000).sync();
            log.info("rpc server start with port:{}", 9000);
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


    private static class NettyServerChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0 ,4));
            pipeline.addLast(new LengthFieldPrepender(4));
            pipeline.addLast("encoder", new ObjectEncoder());
            pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
            pipeline.addLast(new NettyRpcServerHandler());
        }
    }

    @Autowired
    public NettyProperty getNettyProperty() {
        return nettyProperty;
    }
}
