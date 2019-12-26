package kaiwens.basicapp.kaiwens.basicapp.swf;

import java.util.concurrent.Callable;

public class Utils {
    public static <T> T eventually(Callable<T> taskValidator, Integer timeout, Integer interval) {
        if (timeout == null || timeout <= 0) {
            timeout = 5 * 60;
        }
        long timeoutMs = timeout * 1000L;
        if (interval == null || interval < 0) {
            interval = 30;
        }
        long intervalMs = interval * 1000L;
        long startMs= System.currentTimeMillis();
        while (true) {
            try {
                return taskValidator.call();
            } catch (AssertionError e) {
                long nowMs = System.currentTimeMillis();
                long sofarMs = Math.max(timeout, nowMs - startMs);
                if (sofarMs >= timeoutMs) {
                    throw new RuntimeException(e);
                }
                try {
                    Thread.sleep(Math.min(intervalMs, timeoutMs - sofarMs));
                } catch (InterruptedException e2) {
                    throw new RuntimeException(e2);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
