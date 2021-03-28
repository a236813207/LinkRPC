package com.ken.rpc.server.handler;

import com.ken.rpc.common.protocol.InvokerProtocol;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ken
 * @version 1.0
 * @date 2021-03-28
 */
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {

    private static ConcurrentHashMap<String, Object> serviceMap = new ConcurrentHashMap<>();

    private List<String> classNames = new ArrayList<>();

    public NettyRpcServerHandler() {
        scannerClass("com.ken.rpc.server.provider.service.impl");
        doRegister();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();
        InvokerProtocol request = (InvokerProtocol) msg;

        if (serviceMap.containsKey(request.getClassName())) {
            Object clazz = serviceMap.get(request.getClassName());
            Method method = clazz.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            result = method.invoke(clazz, request.getParams());
        }
        ctx.writeAndFlush(result);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private void scannerClass(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        if (url == null) {
            return;
        }
        try {
            File dir = new File(url.toURI().getPath());
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    scannerClass(packageName + "." + file.getName());
                }else {
                    classNames.add(packageName + "." + file.getName().replace(".class", "").trim());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRegister() {
        if (classNames.size() == 0) {
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> clazzInterface = clazz.getInterfaces()[0];
                serviceMap.put(clazzInterface.getName(), clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
