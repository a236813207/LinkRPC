package com.ken.rpc.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ken
 * @version 1.0
 * @date 2021-03-28
 */
@Data
@ConfigurationProperties(prefix = "netty.rpc")
public class NettyProperty {

    private Integer port;

}
