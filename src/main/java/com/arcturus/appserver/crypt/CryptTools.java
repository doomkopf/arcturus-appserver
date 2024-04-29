package com.arcturus.appserver.crypt;

import com.arcturus.appserver.system.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public enum CryptTools
{
	;

	private static final ThreadLocal<MessageDigest> messageDigesters = new ThreadLocal<>();
	private static final byte[] BUFFER = new byte[10000000]; // TODO ...

	private static byte[] zip(String str) throws IOException
	{
		try (var baos = new ByteArrayOutputStream())
		{
			var out = new DeflaterOutputStream(baos);
			out.write(str.getBytes(Constants.CHARSET_UTF8));
			out.close();
			return baos.toByteArray();
		}
	}

	private static synchronized String unzipString(byte[] bytes) throws IOException
	{
		try (var in = new InflaterInputStream(new ByteArrayInputStream(bytes));
			var baos = new ByteArrayOutputStream())
		{
			int len;
			while ((len = in.read(BUFFER)) > 0)
			{
				baos.write(BUFFER, 0, len);
			}
			return new String(baos.toByteArray(), Constants.CHARSET_UTF8);
		}
	}

	public static String base64Zip(String str) throws IOException
	{
		return new String(Base64.getEncoder().encode(zip(str)), Constants.CHARSET_UTF8);
	}

	public static String base64Unzip(String str) throws IOException
	{
		return unzipString(Base64.getDecoder().decode(str));
	}

	private static MessageDigest getMessageDigest() throws NoSuchAlgorithmException
	{
		var md = messageDigesters.get();
		if (md == null)
		{
			md = MessageDigest.getInstance("MD5");
			messageDigesters.set(md);
		}

		return md;
	}

	public static String hashMD5(String str)
	{
		try
		{
			var md = getMessageDigest();
			md.update(str.getBytes(Constants.CHARSET_UTF8));
			return new String(Base64.getEncoder().encode(md.digest()), Constants.CHARSET_UTF8);
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace(); // Impossible
			return null;
		}
	}
}