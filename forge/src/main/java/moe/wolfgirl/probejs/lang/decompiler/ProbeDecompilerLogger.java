package moe.wolfgirl.probejs.lang.decompiler;

import moe.wolfgirl.probejs.ProbeJS;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

public class ProbeDecompilerLogger extends IFernflowerLogger {
    @Override
    public void writeMessage(String message, Severity severity) {
        ProbeJS.LOGGER.info(message);
    }

    @Override
    public void writeMessage(String message, Severity severity, Throwable t) {
        ProbeJS.LOGGER.info(message);
    }

    @Override
    public void startProcessingClass(String className) {
        ProbeJS.LOGGER.info("Started processing: %s".formatted(className));
    }
}
