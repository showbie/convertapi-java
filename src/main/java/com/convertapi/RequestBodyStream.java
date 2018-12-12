package com.convertapi;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

class RequestBodyStream {
    static RequestBody create(final MediaType mediaType, final InputStream inputStream, final long contentLength) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public long contentLength() {
                return contentLength;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(inputStream);
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }
}