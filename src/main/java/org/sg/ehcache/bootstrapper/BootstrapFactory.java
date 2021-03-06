package org.sg.ehcache.bootstrapper;

import java.util.Properties;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.bootstrap.BootstrapCacheLoaderFactory;


@SuppressWarnings("rawtypes")
public class BootstrapFactory extends BootstrapCacheLoaderFactory{

	@Override
	public BootstrapCacheLoader createBootstrapCacheLoader(Properties props) {
		// TODO Auto-generated method stub
		
		
		// Set a minimum amount of threads to use...
		String threads = "3";
		if (props.containsKey("Threads")) {
			threads = props.getProperty("Threads");
		} 
		
		// Make sure to set a minimum sleep time, if nothing is set!
		// Sleep time is important, because the initial bootstrap can cause severe load on the Terracotta Server Array!
		String sleepTime = "100";
		if (props.containsKey("SleepMsec")) {
			sleepTime = props.getProperty("SleepMsec");
		} 
		
		String useBulk = "true";
		if (props.containsKey("useBulkLoad")) {
			useBulk = props.getProperty("useBulkLoad");
		} 
		
		String isAsync = "false";
		if (props.containsKey("bootstrapAsynchronously")) {
			useBulk = props.getProperty("bootstrapAsynchronously");
		} 
		
		return new BootstrapCacheLoaderClass(Integer.valueOf(threads), Integer.valueOf(sleepTime), Boolean.valueOf(useBulk), Boolean.valueOf(isAsync));
	}

}
