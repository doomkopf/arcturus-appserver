package com.arcturus.appserver.cluster.hazelcast.serializer;

/**
 * Type ids for custom hazelcast serializers.
 * 
 * @author doomkopf
 */
public enum HazelcastSerializer
{
	uuid;

	public int getId()
	{
		// Hazelcast custom serializer ids have to start at 1
		return ordinal() + 1;
	}
}