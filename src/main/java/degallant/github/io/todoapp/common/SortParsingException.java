package degallant.github.io.todoapp.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class SortParsingException extends RuntimeException {

    public SortParsingException() {
    }

    @Getter
    @RequiredArgsConstructor
    public static class InvalidQuery extends SortParsingException {

        private final String query;

    }

    @Getter
    @RequiredArgsConstructor
    public static class InvalidDirection extends SortParsingException {

        private final String direction;

    }

    @Getter
    @RequiredArgsConstructor
    public static class InvalidAttribute extends SortParsingException {

        private final String attribute;

    }

}
