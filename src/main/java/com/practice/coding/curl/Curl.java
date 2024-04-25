package com.practice.coding.curl;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command(name = "curl", description = "")
public class Curl implements Callable<String> {

    @Option(names = {"-d", "--data", "--data-raw"}, description = "")
    private String body;

    @Option(names = {"-X", "--request"}, description = "", defaultValue = "get")
    private String requestType;

    @Option(names = {"-h", "--header"}, description = "")
    private Map<String, String> headers = new HashMap<>();

    @Parameters(index = "0", description = "")
    private String url;

    @Override
    public String call() throws Exception {
        var httpRequestBuilder = HttpRequest.newBuilder().uri(URI.create(url));

        var requestBody = HttpRequest.BodyPublishers.ofString(body);

        httpRequestBuilder = switch (requestType.trim().toLowerCase()) {
            case "get" -> httpRequestBuilder.GET();
            case "post" -> httpRequestBuilder.POST(requestBody);
            case "put" -> httpRequestBuilder.PUT(requestBody);
            case "delete" -> httpRequestBuilder.DELETE();
            default -> throw new RuntimeException("Unsupported HTTP Request Type");
        };

        if (Objects.nonNull(headers) && !headers.isEmpty()) {
            var headersArray =  headers.entrySet().parallelStream().flatMap(entry -> Stream.of(entry.getKey(), entry.getValue())).toArray(String[]::new);
            httpRequestBuilder = httpRequestBuilder.headers(headersArray);
        }

        var httpRequest = httpRequestBuilder.build();

        var httpClient = HttpClient.newHttpClient();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        var stringJoiner = new StringJoiner(System.lineSeparator());

        var headersSerialize = httpResponse.headers().map()
                .entrySet()
                .parallelStream()
                .filter(headerEntry -> !headerEntry.getKey().equalsIgnoreCase(":status"))
                .map(headerEntry -> String.format("%s: %s", headerEntry.getKey(), headerEntry.getValue().toString()))
                .collect(Collectors.joining(System.lineSeparator()));

        stringJoiner
                .add(String.format("%s %s %s", httpResponse.uri().toString(), httpResponse.statusCode(), httpResponse.version().name()))
                .add(headersSerialize)
                .add(httpResponse.body());

        return stringJoiner.toString();
    }
}
