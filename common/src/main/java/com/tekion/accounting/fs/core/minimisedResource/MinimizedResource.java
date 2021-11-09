package com.tekion.accounting.fs.core.minimisedResource;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonSerialize(using = MinimizedResourceSerializer.class)
public class MinimizedResource<T> {
    T data;
    MinimizedResourceMetaData minimizedResourceMetaData;
    boolean minimizeObject;
}