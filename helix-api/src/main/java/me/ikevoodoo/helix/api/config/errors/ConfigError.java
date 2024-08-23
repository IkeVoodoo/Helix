package me.ikevoodoo.helix.api.config.errors;

public interface ConfigError {

    ConfigErrorType getErrorType();

    String getMessage();
}
