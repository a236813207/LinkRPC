package com.ken.rpc.client.proxy;


import com.ken.rpc.client.handler.NettyRpcClientHandler;
import com.ken.rpc.common.protocol.InvokerProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author ken
 * @version 1.0
 * @date 2021-03-28
 */
@Slf4j
public class RpcProxy {

    public static <T> T create(Class<?> clazz) {
        MethodProxy proxy = new MethodProxy(clazz);
        Class<?>[] classes = clazz.isInterface() ? new Class[]{clazz} : clazz.getInterfaces();
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), classes, proxy);

    }

    private static class MethodProxy implements InvocationHandler {
        private Class<?> clazz;

        public MethodProxy(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            return rpcInvoke(proxy, method, args);
        }

        private Object rpcInvoke(Object proxy, Method method, Object[] args) {
            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setParamTypes(method.getParameterTypes());
            msg.setParams(args);

            EventLoopGroup workGroup = new NioEventLoopGroup();

            NettyRpcClientHandler nettyRpcClientHandler = new NettyRpcClientHandler();

            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .handler(new NettyClientChannelHandler(nettyRpcClientHandler));
            try {
                ChannelFuture future = bootstrap.connect("localhost", 9000).sync();
                log.info("rpc client connect with port:{}", 9000);
                future.channel().writeAndFlush(msg).sync();
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                workGroup.shutdownGracefully();
            }
            return nettyRpcClientHandler.getResponse();
        }

        private static class NettyClientChannelHandler extends ChannelInitializer<SocketChannel> {

            private final NettyRpcClientHandler nettyRpcClientHandler;

            public NettyClientChannelHandler(NettyRpcClientHandler nettyRpcClientHandler) {
                this.nettyRpcClientHandler = nettyRpcClientHandler;
            }

            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                ChannelPipeline pipeline = socketChannel.pipeline();
                pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0 ,4));
                pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
                pipeline.addLast("encoder", new ObjectEncoder());
                pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                pipeline.addLast("handler", nettyRpcClientHandler);
            }
        }
    }


}
