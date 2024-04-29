package com.arcturus.appserver.cluster.hazelcast;

import com.arcturus.appserver.cluster.Node;
import com.arcturus.appserver.cluster.NodeIdentity;
import com.arcturus.appserver.cluster.RemoteMessageReceiver;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.instance.EndpointQualifier;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * A hazelcast based implementation of {@link Node}.
 *
 * @author doomkopf
 */
public class HazelcastMember implements Node
{
	public static volatile RemoteMessageReceiver remoteMessageReceiver;

	private static class InternalRunnable implements Runnable, Externalizable
	{
		private static final long serialVersionUID = 1L;

		private byte[] byteData = null;

		public InternalRunnable()
		{
		}

		InternalRunnable(byte[] byteData)
		{
			this.byteData = byteData;
		}

		@Override
		public void run()
		{
			remoteMessageReceiver.handleMessage(byteData);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException
		{
			if (byteData == null)
			{
				out.writeInt(-1);
			}
			else
			{
				out.writeInt(byteData.length);
				for (var b : byteData)
				{
					out.writeByte(b);
				}
			}
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException
		{
			var length = in.readInt();
			if (length == -1)
			{
				byteData = null;
			}
			else
			{
				byteData = new byte[length];
				for (var i = 0; i < length; i++)
				{
					byteData[i] = in.readByte();
				}
			}
		}
	}

	private final Member hzMember;
	private final IExecutorService hzExecutorService;
	private final Executor executor;
	private final NodeIdentity nodeIdentity;

	HazelcastMember(Member hzMember, IExecutorService hzExecutorService, Executor executor)
	{
		this.hzMember = hzMember;
		this.hzExecutorService = hzExecutorService;
		this.executor = executor;
		nodeIdentity = new NodeIdentity(
			hzMember.getSocketAddress(EndpointQualifier.MEMBER).getAddress().getAddress(),
			hzMember.getAddress().getPort()
		);
	}

	@Override
	public UUID getId()
	{
		return UUID.fromString(hzMember.getUuid());
	}

	@Override
	public NodeIdentity getPhysicalIdentity()
	{
		return nodeIdentity;
	}

	@Override
	public boolean isLocal()
	{
		return hzMember.localMember();
	}

	@Override
	public void send(byte[] byteData)
	{
		executor.execute(() -> executeOnMember(byteData));
	}

	private void executeOnMember(byte[] byteData)
	{
		hzExecutorService.executeOnMember(new InternalRunnable(byteData), hzMember);
	}
}