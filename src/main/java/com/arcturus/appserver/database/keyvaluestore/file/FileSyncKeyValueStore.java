package com.arcturus.appserver.database.keyvaluestore.file;

import com.arcturus.appserver.concurrent.lock.LockManager;
import com.arcturus.appserver.config.Config;
import com.arcturus.appserver.config.ServerConfigPropery;
import com.arcturus.appserver.database.keyvaluestore.SyncKeyValueStore;
import com.arcturus.appserver.system.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A file based implementation of {@link SyncKeyValueStore}.
 *
 * @author doomkopf
 */
public class FileSyncKeyValueStore implements SyncKeyValueStore<String, String>
{
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	private static void writeFile(String path, byte[] content) throws IOException
	{
		Files.write(Paths.get(path), content);
	}

	private static byte[] readFile(String path)
	{
		try
		{
			return Files.readAllBytes(Paths.get(path));
		}
		catch (IOException e)
		{
			return EMPTY_BYTE_ARRAY;
		}
	}

	private final String pathWithEndingSlash;
	private final String fileExtension;
	private final LockManager<String> fileLockManager = new LockManager<>();

	public FileSyncKeyValueStore(Config config) throws IOException
	{
		pathWithEndingSlash = config.getString(ServerConfigPropery.fileDatabasePath);
		fileExtension = config.getString(ServerConfigPropery.fileDatabaseExtension);

		var dataDir = Paths.get(pathWithEndingSlash);
		if (!dataDir.toFile().exists())
		{
			Files.createDirectory(dataDir);
		}
	}

	private String createPathForKey(String key)
	{
		return pathWithEndingSlash + key + '.' + fileExtension;
	}

	@Override
	public String get(String key)
	{
		var bytes = readFile(createPathForKey(key));
		if (bytes.length == 0)
		{
			return null;
		}

		return new String(bytes, Constants.CHARSET_UTF8);
	}

	@Override
	public boolean put(String key, String value)
	{
		try
		{
			fileLockManager.lock(key);
			writeFile(createPathForKey(key), value.getBytes(Constants.CHARSET_UTF8));
		}
		catch (IOException e)
		{
			return false;
		}
		finally
		{
			fileLockManager.unlock(key);
		}

		return true;
	}

	@Override
	public boolean remove(String key)
	{
		try
		{
			Files.deleteIfExists(Paths.get(createPathForKey(key)));
		}
		catch (IOException e)
		{
			return false;
		}

		return true;
	}

	@Override
	public void shutdown()
	{
	}
}