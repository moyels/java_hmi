package top.moyel.common.hmi.utils;

/**
 * 简化异常捕获util
 *
 * @author moyel
 */
public class CatchUtil {
    public static <R> R catchExcept(ICatchProcess<R> catchProcess) {
        return catchExcept(catchProcess, false);
    }

    /**
     * 无需在意捕获的实际异常的场景，简化try-catch
     *
     * @param catchProcess 需要catch的过程
     * @param print        是否打印异常堆栈信息
     * @param <R>          返回值泛型
     * @return 返回值
     */
    public static <R> R catchExcept(ICatchProcess<R> catchProcess, boolean print) {
        try {
            return catchProcess.execute();
        } catch (Exception ex) {
            if (print) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    /**
     * catch 过程
     *
     * @param <R> 返回值泛型
     */
    public interface ICatchProcess<R> {
        /**
         * 执行的过程
         *
         * @return 返回值
         * @throws Exception 可能会抛出异常
         */
        R execute() throws Exception;
    }
}
