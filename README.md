Ehcache-Bootstrap-Loader
URL: https://github.com/sgrotz/Ehcache-Bootstrap-Loader
Author: Stephan Grotz (stephan.grotz@gmail.com)
========================

The Bootstrap-Loader can be used for Ehcache to load all data from the Terracotta Server Array upon startup. This bootstrapper is an extension to the already existing default bootstrap factories. 

This loader is especially useful, if you want to load ALL data from the TSA to your local client. If you only want to preload the objects, which were in the cache before the shutdown, please take a look at: http://ehcache.org/documentation/2.4/user-guide/configuration#terracottabootstrapcacheloaderfactory


To use this bootstrap loader, please configure your ehcache.xml file with the following settings:
<bootstrapCacheLoaderFactory class="org.sg.ehcache.bootstrapper.BootstrapFactory" properties="bootstrapAsynchronously=true,Threads=5,SleepMsec=10"/>


Available properties are:
* bootstrapAsynchronously: boolean field - indicates, if the cache is being loaded before it becomes available (false), or if the cache can become available, while it is being loaded - lazyLoading (true)
* Threads: Integer field - indicates how many threads should be used to load the local client.
* SleepMsec: Integer field - indicates how many milliseconds to sleep while loading each element. PLEASE NOTE: Bootstrapping can cause excessive load onto the Terracotta Server Array, especially if multiple clients are reloading at the same time. Using the SleepMsec can distribute the load more evenly.

Enjoy :)
