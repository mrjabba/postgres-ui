package org.whatever;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result {
    private String col1;
    private String col2;
    private String col3;
}
