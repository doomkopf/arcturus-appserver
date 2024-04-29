package com.arcturus.appserver.cluster.hazelcast.serializer;

import java.io.IOException;
import java.util.UUID;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;

/**
 * A custom hazelcast serializer for {@link UUID}.
 * 
 * @author doomkopf
 */
public class UUIDStreamSerializer implements StreamSerializer<UUID>
{
	@Override
	public int getTypeId()
	{
		return HazelcastSerializer.uuid.getId();
	}

	@Override
	public UUID read(ObjectDataInput in) throws IOException
	{
		return new UUID(in.readLong(), in.readLong());
	}

	@Override
	public void write(ObjectDataOutput out, UUID uuid) throws IOException
	{
		out.writeLong(uuid.getMostSignificantBits());
		out.writeLong(uuid.getLeastSignificantBits());
	}

	@Override
	public void destroy()
	{
		// Nothing
	}
}