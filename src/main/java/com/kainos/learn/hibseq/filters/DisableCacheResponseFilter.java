package com.kainos.learn.hibseq.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DisableCacheResponseFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Set standard HTTP/1.1 no-cache headers.
        httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");

        // Set IE extended HTTP/1.1 no-cache headers (use addHeader).
        httpResponse.addHeader("Cache-Control", "post-check=0, pre-check=0");

        chain.doFilter(request, httpResponse);
    }

    @Override
    public void destroy() {
    }

}

