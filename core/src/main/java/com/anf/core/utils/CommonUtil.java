package com.anf.core.utils;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j;

@UtilityClass
@Log4j
public class CommonUtil {

    /**
     * Convert Object To Json String
     * 
     * @param source
     *            Object to be converted.
     * @return String
     */
    public String convertObjectToJsonString(Object source) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(source);
        } catch (Exception e) {
            log.error("Unable to serialize object to json", e);
        }
        return StringUtils.EMPTY;
    }
}
