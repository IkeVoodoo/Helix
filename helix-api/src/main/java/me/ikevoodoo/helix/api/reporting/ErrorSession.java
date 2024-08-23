package me.ikevoodoo.helix.api.reporting;

import java.util.Map;

public interface ErrorSession {

    void reportError(String message, ErrorType type);

    int errorCount(ErrorType type);

    Map<String, ErrorType> getErrors();

}
