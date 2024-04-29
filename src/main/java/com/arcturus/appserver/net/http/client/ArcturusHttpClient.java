package com.arcturus.appserver.net.http.client;

import com.arcturus.api.tool.JsonStringSerializer;
import com.arcturus.appserver.system.app.service.UseCaseProcessor;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.UUID;

public class ArcturusHttpClient
{
	private static final HttpClient httpClient = HttpClient.newHttpClient();

	private final UseCaseProcessor useCaseProcessor;
	private final JsonStringSerializer jsonStringSerializer;

	public ArcturusHttpClient(
		UseCaseProcessor useCaseProcessor, JsonStringSerializer jsonStringSerializer
	)
	{
		this.useCaseProcessor = useCaseProcessor;
		this.jsonStringSerializer = jsonStringSerializer;
	}

	public void request(
		HttpRequest request,
		String resultUseCase,
		long requestId,
		UUID requestingUserId,
		String payload
	)
	{
		var httpRequestBuilder = java.net.http.HttpRequest.newBuilder()
			.uri(URI.create(request.url));

		switch (request.method)
		{
		case GET:
			httpRequestBuilder.GET();
			break;
		case POST:
			httpRequestBuilder.POST(BodyPublishers.ofString(request.body));
			break;
		case PUT:
			httpRequestBuilder.PUT(BodyPublishers.ofString(request.body));
			break;
		case DELETE:
			httpRequestBuilder.DELETE();
			break;
		}

		for (var header : request.headers)
		{
			httpRequestBuilder.header(header.key, header.value);
		}

		httpClient.sendAsync(httpRequestBuilder.build(), BodyHandlers.ofString())
			.thenAccept(response -> useCaseProcessor.process(resultUseCase,
				requestId,
				requestingUserId,
				jsonStringSerializer.toJsonString(new HttpClientResponse(payload,
					new HttpResponse(response.statusCode(), response.body())
				)),
				null
			))
			.exceptionally(throwable ->
			{
				useCaseProcessor.process(resultUseCase,
					requestId,
					requestingUserId,
					jsonStringSerializer.toJsonString(new HttpClientResponse(payload,
						new HttpResponse(0, throwable.getMessage())
					)),
					null
				);
				return null;
			});
	}
}