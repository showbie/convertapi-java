package com.convertapi;

import okhttp3.*;
import javax.activation.MimetypesFileTypeMap;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Param {
    private String name;
    private CompletableFuture<String> value;
    private Config config = Config.defaults();

    public Param(String name, String value) {
        this.name = name.toLowerCase();
        this.value = CompletableFuture.completedFuture(value);
    }

    public Param(String name, int value) {
        this(name, String.valueOf(value));
    }

    public Param(String name, BigDecimal value) {
        this(name, String.valueOf(value));
    }

    public Param(String name, byte[] value, String fileFormat) {
        this(name, value, fileFormat, Config.defaults());
    }

    public Param(String name, byte[] value, String fileFormat, Config config) {
        this(name, "");
        String fileName = "getFile." + fileFormat;
        String contentTypeString = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(fileName);
        this.value = upload(value, fileName, MediaType.parse(contentTypeString), config);
    }

    public Param(String name, Path value) throws IOException {
        this(name, value, Config.defaults());
    }

    public Param(String name, Path value, Config config) throws IOException {
        this(name, "");
        String contentTypeString = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(value.toFile());
        this.value = upload(Files.readAllBytes(value), value.getFileName().toString(), MediaType.parse(contentTypeString), config);
    }

    public Param(String name, ConversionResult value) throws ExecutionException, InterruptedException {
        this(name, value, 0);
    }

    public Param(String name, ConversionResult value, int fileIndex) throws ExecutionException, InterruptedException {
        this(name, "");
        this.value = value.getFile(fileIndex).thenApplyAsync(f -> f.
                getUrl());
    }

    public String getName() {
        return name;
    }

    public Param setConfig(Config config) {
        this.config = config;
        return this;
    }

    public String getValue() throws ExecutionException, InterruptedException {
        return this.value.get();
    }

    private static CompletableFuture<String> upload(byte[] data, String fileName, MediaType fileContentType, Config config) {
        return CompletableFuture.supplyAsync(() -> {
            Request request = new Request.Builder()
                    .url(Http.getUrlBuilder(config).addPathSegment("upload")
                            .addQueryParameter("filename", fileName.toString())
                            .build())
                    .post(RequestBody.create(fileContentType, data))
                    .build();
            try {
                return Http.getClient().newCall(request).execute().body().string();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}