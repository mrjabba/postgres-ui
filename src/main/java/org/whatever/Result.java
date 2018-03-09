package org.whatever;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result {
    private Object col1;
    private Object col2;
    private Object col3;
}
