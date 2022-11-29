package top.moyel.common.hmi.test;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class StrSplitTest {
    @Test
    @Disabled
    public void split() {
        String regex = "\\|\\|";

        String exp = "a==1||a==2";

        String[] split = exp.split(regex);
        System.out.println(Arrays.toString(split));
    }
}
