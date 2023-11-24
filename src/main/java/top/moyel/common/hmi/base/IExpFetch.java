package top.moyel.common.hmi.base;

/**
 * @author moyel
 */
public interface IExpFetch<F, O> {
    /**
     * 判断是否支持该表达式
     *
     * @param exp expression
     * @return true if support
     */
    boolean support(String exp);

    /**
     * 解析表达式
     *
     * @param exp   expression
     * @param param param
     * @return result
     */
    O fetch(String exp, F param);
}
