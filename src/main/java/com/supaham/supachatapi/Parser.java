package com.supaham.supachatapi;

import com.supaham.supachatapi.fanciful.FancyMessage;

/**
 * Represents a {@link String} parser used to parse messages.
 */
public interface Parser {

    /**
     * Parses a {@link String} using this Parser.
     *
     * @param source source to parse
     * @param params params to replace
     * @return an instance of {@link FancyMessage} with the parsed {@code source}
     */
    public FancyMessage parse(String source, Object... params) throws Exception;
}
