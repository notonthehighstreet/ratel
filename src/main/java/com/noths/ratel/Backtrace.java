package com.noths.ratel;

class Backtrace {

    static Backtrace fromStackTrace(final StackTraceElement e) {
        final Backtrace backtrace = new Backtrace();
        backtrace.setFile(e.getClassName());
        backtrace.setMethod(e.getMethodName());
        backtrace.setNumber(e.getLineNumber());
        return backtrace;
    }

    static Backtrace markerBacktrace(final Throwable e) {
        final Backtrace backtrace = new Backtrace();
        backtrace.setNumber(-1);
        backtrace.setFile("Caused by: " + e);
        return backtrace;
    }

    private String file;
    private Integer number;
    private String method;

    public String getFile() {
        return file;
    }

    public void setFile(final String file) {
        this.file = file;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(final Integer number) {
        this.number = number;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

}
