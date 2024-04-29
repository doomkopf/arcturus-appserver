package com.arcturus.appserver.cluster.hazelcast;

import com.arcturus.appserver.cluster.Cluster;
import com.arcturus.appserver.cluster.Node;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A hazelcast based implemenation of {@link Cluster}.
 *
 * @author doomkopf
 */
public class HazelcastCluster implements Cluster, MembershipListener
{
	private final IExecutorService hzExecutorService;
	private final HazelcastMember localMember;
	private final ExecutorService executorService;
	private volatile Map<UUID, HazelcastMember> readOnlyIdToMemberMap;

	public HazelcastCluster(SharedHazelcastInstance sharedHazelcastInstance, Config config)
	{
		var hz = sharedHazelcastInstance.getHazelcastInstance();

		hzExecutorService = hz.getExecutorService("remoteExecutorService");

		executorService = Executors.newFixedThreadPool(config.getInt(ServerConfigPropery.hazelcastMemberSenderThreads));

		localMember = new HazelcastMember(hz.getCluster().getLocalMember(),
			hzExecutorService,
			executorService
		);
		readOnlyIdToMemberMap = copyMembers(hz.getCluster().getMembers());

		hz.getCluster().addMembershipListener(this);
	}

	private Map<UUID, HazelcastMember> copyMembers(Iterable<com.hazelcast.core.Member> hazelcastMembers)
	{
		var newMap = new HashMap<UUID, HazelcastMember>();
		for (var hzMember : hazelcastMembers)
		{
			var member = new HazelcastMember(hzMember, hzExecutorService, executorService);
			newMap.put(member.getId(), member);
		}

		return newMap;
	}

	@Override
	public Node getLocalNode()
	{
		return localMember;
	}

	@Override
	public Node getNodeById(UUID id)
	{
		return readOnlyIdToMemberMap.get(id);
	}

	@Override
	public Iterable<UUID> getAllNodesIterable()
	{
		return readOnlyIdToMemberMap.keySet();
	}

	@Override
	public int getNodeCount()
	{
		return readOnlyIdToMemberMap.size();
	}

	@Override
	public void memberAdded(MembershipEvent membershipEvent)
	{
		readOnlyIdToMemberMap = copyMembers(membershipEvent.getMembers());
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent)
	{
		readOnlyIdToMemberMap = copyMembers(membershipEvent.getMembers());
	}

	@Override
	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent)
	{
		readOnlyIdToMemberMap = copyMembers(memberAttributeEvent.getMembers());
	}

	public void shutdown() throws InterruptedException
	{
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.SECONDS);
	}
}