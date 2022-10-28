/*
 *  Copyright 2015 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.anf.core.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Servlet;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.anf.core.error.AppException;
import com.anf.core.error.ErrorCode;
import com.anf.core.model.User;
import com.anf.core.services.ContentService;
import com.anf.core.utils.CommonUtil;

import lombok.extern.log4j.Log4j;

@Component(
        service = { Servlet.class })
@SlingServletPaths(
        value = "/bin/saveUserDetails")
@Log4j
public class UserServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Reference
    private ContentService contentService;

    @Override
    protected void doPost(final SlingHttpServletRequest req, final SlingHttpServletResponse resp) throws IOException {
        // Make use of ContentService to write the business logic

        PrintWriter writer = resp.getWriter();
        try {

            // commit user details
            contentService.commitUserDetails(populateUser(req));

            // return 202 accepted response when details are saved successfully
            resp.setStatus(HttpStatus.SC_ACCEPTED);
            
        } catch (Exception e) {
            // handle errors and return proper json response describing the error
            log.error("Error saving user details", e);
            handleError(resp, writer, e);
        }

    }
    
    private User populateUser(final SlingHttpServletRequest req) {

        // Populate user object by reading values from sling request
        String age = StringUtils.defaultIfEmpty(req.getParameter("age"), "0");
        return User.builder()
                .firstName(req.getParameter("firstName"))
                .lastName(req.getParameter("lastName"))
                .country(req.getParameter("country"))
                .age(Integer.valueOf(age))
                .build();
    }

    private void handleError(final SlingHttpServletResponse resp, PrintWriter writer, Exception exp) {

        resp.setContentType("application/json");

        // read error code from exception type
        ErrorCode errorCode = ErrorCode.RUNTIME_ERROR;
        final String exceptionType = exp.getClass()
                .getName();

        if (exceptionType.equals(AppException.class.getName())) {
            AppException exception = (AppException) exp;
            errorCode = exception.getErrorCode();

        }

        // Convert error code into JSON error response. Set http status based on error code.
        resp.setStatus(errorCode.getStatusCode());
        writer.write(CommonUtil.convertObjectToJsonString(com.anf.core.model.Error.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build()));

    }
}
