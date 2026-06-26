package com.ces.exam.payload.response;

import java.util.List;

public class BulkImportResponse {
    private int created;
    private List<RowError> errors;

    public BulkImportResponse(int created, List<RowError> errors) {
        this.created = created;
        this.errors = errors;
    }

    public int getCreated() { return created; }
    public List<RowError> getErrors() { return errors; }

    public static class RowError {
        private int row;
        private String message;

        public RowError(int row, String message) {
            this.row = row;
            this.message = message;
        }

        public int getRow() { return row; }
        public String getMessage() { return message; }
    }
}
