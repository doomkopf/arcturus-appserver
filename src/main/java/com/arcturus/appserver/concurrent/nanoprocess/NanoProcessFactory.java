package com.arcturus.appserver.concurrent.nanoprocess;

public interface NanoProcessFactory<T>
{
	NanoProcess<T> create(NanoProcessThread<T> thread);
}