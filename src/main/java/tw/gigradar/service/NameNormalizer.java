package tw.gigradar.service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 名稱正規化：全形轉半形、英文轉小寫、去掉空白與常見標點。
 * 大小寫、全半形、空白的差異在這裡處理；中英文名稱對應（中文團名 vs 英文團名）靠別名表。
 */
public final class NameNormalizer {

    private static final Pattern NOISE =
            Pattern.compile("[\\s\\-_.·・'’\"()（）\\[\\]]+", Pattern.UNICODE_CHARACTER_CLASS);

    private NameNormalizer() {}

    public static String normalize(String name) {
        String s = Normalizer.normalize(name, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        return NOISE.matcher(s).replaceAll("");
    }
}
