package com.ken.rpc.client;

import com.ken.rpc.client.proxy.RpcProxy;
import com.ken.rpc.common.api.IHelloWorldService;
import lombok.extern.slf4j.Slf4j;

/**
 * Hello world!
 *
 */
@Slf4j
public class NettyClientApplication {

    public static void main( String[] args ) {
        IHelloWorldService helloWorldService = RpcProxy.create(IHelloWorldService.class);
        String sayHello = helloWorldService.sayHello("ken");
        log.info(sayHello);
    }
}
