package org.ble.utils;

/**
 * Author: yuzzha
 * Date: 2021-11-27 20:54
 * Description:
 * Remark:
 * 定义一个抛出异常的形式的函数式接口, 这个接口只有参数没有返回值是个消费型接口
 *
 *    public static ThrowExceptionFunction runtimeException(boolean b) {
 *         return (errorMessage) -> {
 *             if (b) {
 *                 throw new RuntimeException(errorMessage);
 *             }
 *         };
 *     }
 *
 */
@FunctionalInterface
public interface ThrowExceptionFunction {

    /**
     * 抛出异常
     * @param message 异常消息
     */
    void throwMessage(String message);
}
