package com.arcturus.appserver.reflect;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarEntry;

/**
 * Some functions for finding classes recursively in a package.
 *
 * @author doomkopf
 */
public enum ClassLookupTools
{
	;

	public interface ClassFilter
	{
		boolean filterJarEntry(JarEntry jarEntry);

		boolean filterFile(File file);
	}

	private static final String CLASS_FILE_EXTENSION = ".class";

	private static final ClassFilter DEFAULT_CLASS_FILTER = new ClassFilter()
	{
		@Override
		public boolean filterFile(File file)
		{
			return file.getName().endsWith(CLASS_FILE_EXTENSION) && !file.getName().contains("$");
		}

		@Override
		public boolean filterJarEntry(JarEntry jarEntry)
		{
			return jarEntry.getName().endsWith(CLASS_FILE_EXTENSION);
		}
	};

	public static List<Class<?>> findClasses(String packageName, ClassFilter classFilter)
		throws ClassNotFoundException, IOException, URISyntaxException
	{
		var classLoader = Thread.currentThread().getContextClassLoader();
		var path = packageName.replace('.', '/');
		var resources = classLoader.getResources(path);
		var dirs = new ArrayList<File>();
		var classes = new ArrayList<Class<?>>();
		while (resources.hasMoreElements())
		{
			var resource = resources.nextElement();
			if ("jar".equals(resource.getProtocol()))
			{
				handleJar(resource, path, classFilter, classes);
			}
			else
			{
				dirs.add(new File(resource.toURI()));
			}
		}

		for (var directory : dirs)
		{
			classes.addAll(findClasses(directory, packageName, classFilter));
		}

		return classes;
	}

	public static List<Class<?>> findClasses(String packageName)
		throws ClassNotFoundException, IOException, URISyntaxException
	{
		return findClasses(packageName, DEFAULT_CLASS_FILTER);
	}

	private static void handleJar(
		URL resource, CharSequence path, ClassFilter classFilter, Collection<Class<?>> classes
	) throws ClassNotFoundException, IOException
	{
		var jar = (JarURLConnection) resource.openConnection();
		jar.connect();
		try (var jarFile = jar.getJarFile())
		{
			var en = jarFile.entries();
			while (en.hasMoreElements())
			{
				var jarEntry = en.nextElement();
				var name = jarEntry.getName();
				if (name.contains(path) && classFilter.filterJarEntry(jarEntry))
				{
					classes.add(Class.forName(removeClassExtension(name.replace("/", "."))));
				}
			}
		}
	}

	private static String removeClassExtension(String className)
	{
		return className.substring(0, className.length() - CLASS_FILE_EXTENSION.length());
	}

	private static Collection<Class<?>> findClasses(
		File directory, String packageName, ClassFilter classFilter
	) throws ClassNotFoundException
	{
		var classes = new ArrayList<Class<?>>();
		if (!directory.exists())
		{
			return classes;
		}

		var files = directory.listFiles();
		for (var file : files)
		{
			var fileName = file.getName();
			if (file.isDirectory())
			{
				classes.addAll(findClasses(file, packageName + '.' + fileName, classFilter));
			}
			else if (classFilter.filterFile(file))
			{
				Class<?> c;
				try
				{
					c = Class.forName(buildClassName(packageName, fileName));
				}
				catch (ExceptionInInitializerError e)
				{
					c = Class.forName(
						buildClassName(packageName, fileName),
						false,
						Thread.currentThread().getContextClassLoader()
					);
				}
				catch (NoClassDefFoundError e)
				{
					continue;
				}

				classes.add(c);
			}
		}

		return classes;
	}

	private static String buildClassName(String packageName, String fileName)
	{
		return packageName + '.' + removeClassExtension(fileName);
	}
}