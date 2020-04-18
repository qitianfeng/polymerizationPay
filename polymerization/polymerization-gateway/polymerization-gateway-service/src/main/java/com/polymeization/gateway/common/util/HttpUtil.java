package com.polymeization.gateway.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polymerization.common.domain.RestResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HttpUtil {

    public static void writerError(RestResponse restResponse, HttpServletResponse response) throws IOException {
        response.setContentType("application/json,charset=utf-8");
        response.setStatus(restResponse.getCode());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getOutputStream(),restResponse);
    }
}
