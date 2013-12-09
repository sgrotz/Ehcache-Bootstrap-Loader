package org.sg.ehcache.bootstrapper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;

public class BootstrapCacheLoaderClass implements BootstrapCacheLoader {

	private int threadCount = 1;
	private int sleepTime = 200;
	private boolean useBulkLoadAPI = true;
	private boolean isAsync = false;
	
	/**
	 * Constructor to create a Cache Loader.
	 * @param threadCount
	 * @param sleepTime
	 */
	public BootstrapCacheLoaderClass(int threadCount, int sleepTime, Boolean useBulkLoadAPI, Boolean isAsync) {
		if (threadCount == 0) {
			this.threadCount = 3;
		}
		this.threadCount = threadCount;
		this.sleepTime = sleepTime;
		this.useBulkLoadAPI = useBulkLoadAPI;
		this.isAsync = isAsync;
	}
	
	
	public Object clone() throws CloneNotSupportedException { 
		// TODO Auto-generated method stub 
		return super.clone(); 
	} 

	
	@Override
	public boolean isAsynchronous() {
		// TODO Auto-generated method stub
		return isAsync;
	}
	
	private Ehcache ehcache;
	private int start; 
	private int end; 
	private List cacheKeys = new LinkedList();
	private List keys = new LinkedList();

	/* (non-Javadoc)
	 * @see net.sf.ehcache.bootstrap.BootstrapCacheLoader#load(net.sf.ehcache.Ehcache)
	 * Main method to load the data into the local heap/offheap
	 */
	@Override
	public void load(Ehcache cache) throws CacheException {
		// TODO Auto-generated method stub
		
		System.out.println("*** Starting Bootstrap Loading for " + cache.getName() + " at " + getCurrentTime());
		long startTime = System.currentTimeMillis();
		this.ehcache = cache;
		
		if (useBulkLoadAPI) {
			System.out.println("*** Enabling BulkLoad API for " + cache.getName());
			this.ehcache.setNodeBulkLoadEnabled(true);
		}
		
		System.out.println("*** Getting all keys for cache " + cache.getName() + " ... (could take a while!)");
		
		// This is an odd one - It seems that the cache.getKeys() results in a java.lang.UnsupportedOperationException
		// This is caused by java.util.SubList.get(AbstractList.java:621) 
		//keys = cache.getKeysWithExpiryCheck();
		cacheKeys = cache.getKeys();
		
		// Mapping the keys list into a new list object - fixes the above mentioned error :)
		final List<String> keys = new ArrayList<String>(cacheKeys);
		
		System.out.println("*** The cache contains a total of: " + keys.size() + " objects...");
		
		
		try {
			if (keys.size() > 0) {
				System.out.println("*** Loading a total of: " + keys.size() + " objects, using " + threadCount + " threads - Sleep time set to " + sleepTime + " msecs..."+ " at " + getCurrentTime());
			
				int objectsPerThread = keys.size() / threadCount;
				
				this.start = 0;
				this.end = start + objectsPerThread;
				
				
				// Create new threads ...
				Thread[] threads = new Thread[threadCount];
				for (int i = 0; i < threads.length; i++) {

					//System.out.println("Starting Thread " + i);
					threads[i] = new Thread() {

						public void run() {
							System.out.println("Thread: " + Thread.currentThread().getId() + " is processing items " + Integer.valueOf(start) + " up to " + Integer.valueOf(end));
							List<String> subList = keys.subList(Integer.valueOf(start), Integer.valueOf(end));
							loadData(subList);
							//loadData(keys);
						}
					};
					// Start the threads ...
					threads[i].start();
					
					// Timing issue - need to wait for the thread to initiate first. Give it a second to start! 
					// Will need to check why later on...
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					start = start + objectsPerThread;
					end = end + objectsPerThread;
				}
				
				for (int i = 0; i < threads.length; i++) {
					try {
						threads[i].join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
					
				
			} else {
				System.out.println("*** Cache is empty - Will exit! ...");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("*** ERROR occurred during bootstrap ***");
			e.printStackTrace();
		}

		if (useBulkLoadAPI) {
			this.ehcache.setNodeBulkLoadEnabled(false);
			System.out.println("*** Disabling BulkLoad API for " + cache.getName());
		}
		
		
		
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		long cacheSize = (cache.getStatistics().getLocalHeapSizeInBytes() + cache.getStatistics().getLocalOffHeapSizeInBytes()) / 1024;
		
		System.out.println("*** Finished Bootstrap Loading for " + cache.getName() + " at " + getCurrentTime());
		System.out.println("*** Bootstrapping " + keys.size() +" elements ("+ cacheSize + " kbytes) took: " + duration + " msec, averaging at approx " + (  keys.size() / duration * 1000) + " elements/sec (" + (cacheSize / duration * 1000) + " kbytes/sec)");
		
		//Only for testing purposes :)
		//System.exit(0);
		
	}
	
	
	
	
	/**
	 * Method to load the data into the local heap/offheap. 
	 * @param items
	 */
	private void loadData(List items) {
		
		
		// To avoid lazz loading, simply iterate over the items and get them individually.
		for (int i = 0; i < items.size(); i++) {
			ehcache.get(items.get(i));
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		
		
		// Lets not take the getAll, as it may lead to OOME
		// Further on, the getAll method, loads the elements lazily - which means, the objects are not yet fully loaded to the client.
		//ehcache.getAll(items);
		
	}
	
	/**
	 * Returns the current timestamp
	 * @return currentTimeStamp
	 */
	private String getCurrentTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
		Date date = new Date();
		return dateFormat.format(date);
	}

}
