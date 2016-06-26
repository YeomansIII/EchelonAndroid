package io.yeomans.echelon.firebase;

/**
 * Created by jason on 6/25/16.
 */

import com.google.firebase.crash.FirebaseCrash;

public class FirebaseErrorLogger {

    public void reportError(Throwable throwable, Object... args) {
        if (args != null) {
            FirebaseCrash.log(argumentsAsString(args));
        }
        FirebaseCrash.report(throwable);
    }

    private String argumentsAsString(Object[] args) {
        StringBuilder builder = new StringBuilder();
        int length = args.length;
        for (int i = 0; i < length; i++) {
            Object arg = args[i];
            builder.append(arg);
            if (i < length - 1) {
                builder.append(" : ");
            }

        }
        return builder.toString();
    }
}