package com.arcturus.appserver.system.app.type.js;

import com.arcturus.api.service.entity.aggregation.AggregationIndex;
import com.arcturus.appserver.json.JsonFactory;
import com.arcturus.appserver.system.Tools;
import com.arcturus.appserver.system.app.service.entity.aggregation.ArcturusAggregationService;

import java.util.ArrayList;
import java.util.UUID;

public class JsAggregationService
{
	private final ArcturusAggregationService aggregationService;
	private final JsonFactory jsonFactory;

	public JsAggregationService(
		ArcturusAggregationService aggregationService, JsonFactory jsonFactory
	)
	{
		this.aggregationService = aggregationService;
		this.jsonFactory = jsonFactory;
	}

	public void start(
		String entityServiceName,
		String mappingUseCaseId,
		String finalUseCaseId,
		String aggregationIndicesJsJson,
		String requestId,
		String requestingUserId
	)
	{
		var jsonIndices = jsonFactory.parseReadonly(aggregationIndicesJsJson).getArray("indices");

		var indices = new ArrayList<AggregationIndex>(jsonIndices.length());
		for (var i = 0; i < jsonIndices.length(); i++)
		{
			var jsonIndex = jsonIndices.getObject(i);
			indices.add(new AggregationIndex(jsonIndex.getString("name"),
				UUID.fromString(jsonIndex.getString("id"))
			));
		}

		aggregationService.start(entityServiceName,
			mappingUseCaseId,
			finalUseCaseId,
			indices,
			Tools.parseLongFromRadix36EncodedString(requestId).longValue(),
			(requestingUserId == null) ? null : UUID.fromString(requestingUserId)
		);
	}
}