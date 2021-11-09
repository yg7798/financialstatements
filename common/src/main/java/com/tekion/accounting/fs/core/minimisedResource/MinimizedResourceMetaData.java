package com.tekion.accounting.fs.core.minimisedResource;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects;
import java.util.Set;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class MinimizedResourceMetaData {
    public static  final String MINIMIZED="minimized";
    public static final String ID="id";
    public static final String INCLUDE_TYPE= IncludeType.INCLUSION.name();
    private  Set<String> fields;
    private  IncludeType includeType;

    @Builder.Default
    private boolean addMinimizedFlag = true;

    public static enum IncludeType {
        INCLUSION,EXCLUSION
    }
    public static IncludeType  findIncludeType(String includeType)
    {
        return Objects.isNull(includeType) ? IncludeType.INCLUSION : IncludeType.valueOf(includeType);
    }
}