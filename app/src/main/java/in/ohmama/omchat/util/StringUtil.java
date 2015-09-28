package in.ohmama.omchat.util;

/**
 * Created by Leon on 9/14/15.
 */
public class StringUtil {
    /**
     * 判断字符是否为空
     *
     * @param input 某字符串
     * @return 包含则返回true，否则返回false
     */
    public static boolean isEmpty(String input) {
        return input == null || input.length() == 0;
    }

}
