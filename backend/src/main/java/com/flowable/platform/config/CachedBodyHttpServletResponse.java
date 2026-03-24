package com.flowable.platform.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CachedBodyHttpServletResponse extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream content = new ByteArrayOutputStream();
    private final ServletOutputStream outputStream = new CachedBodyServletOutputStream(content);
    private final List<String> headers = new ArrayList<>();

    public CachedBodyHttpServletResponse(HttpServletResponse response) {
        super(response);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return outputStream;
    }

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        headers.add(name + ": " + value);
    }

    @Override
    public void addHeader(String name, String value) {
        super.setHeader(name, value);
        headers.add(name + ": " + value);
    }

    public List<String> getHeaders() {
        return headers;
    }

    public byte[] getCachedBody() {
        if (content.size() == 0) {
            return new byte[0];
        }
        return content.toByteArray();
    }

    public String getCachedBodyString() {
        byte[] body = getCachedBody();
        if (body.length == 0) {
            return "";
        }
        return new String(body, StandardCharsets.UTF_8);
    }

    public void copyBodyToResponse() throws IOException {
        byte[] body = getCachedBody();
        if (body.length > 0) {
            // Write to the original response's output stream
            org.springframework.util.StreamUtils.copy(body, getResponse().getOutputStream());
        }
    }

    private static class CachedBodyServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream outputStream;

        public CachedBodyServletOutputStream(ByteArrayOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(int b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            outputStream.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            outputStream.write(b, off, len);
        }
    }
}
