package com.ken.rpc.common.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ken
 * @version 1.0
 * @date 2021-03-28
 */
@Data
public class InvokerProtocol implements Serializable {

    /**
     * 类名称
     */
    private String className;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 参数类型数组
     */
    private Class<?>[] paramTypes;

    /**
     * 参数值数组
     */
    private Object[] params;

}
