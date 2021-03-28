package com.ken.rpc.server.provider.service.impl;

import com.ken.rpc.common.api.IHelloWorldService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author ken
 * @version 1.0
 * @date 2021-03-28
 */
@Service
@Slf4j
public class HelloWorldServiceImpl implements IHelloWorldService {

    @Override
    public String sayHello(String name) {
        String result = "hello world!" + name;
        log.info(result);
        return result;
    }
}
