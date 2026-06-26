package com.ces.exam.payload.response;

public class BulkAssignmentResponse {
    private int created;
    private int skipped;

    public BulkAssignmentResponse(int created, int skipped) {
        this.created = created;
        this.skipped = skipped;
    }

    public int getCreated() { return created; }
    public int getSkipped() { return skipped; }
}
