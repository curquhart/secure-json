package com.chelseaurquhart.securejson;

final class Util {
    private Util() {
    }

    static Throwable unwrapException(final Throwable parInput) {
        Throwable myCause = parInput;
        while (myCause.getCause() != null) {
            myCause = myCause.getCause();
        }

        return myCause;
    }
}
