package se.sics.kompics.wan.config;

import java.io.IOException;
import java.math.BigInteger;

import org.apache.commons.cli.Option;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kompics.address.Address;
import se.sics.kompics.wan.util.LocalIPAddressNotFound;

public class CyclonConfiguration extends Configuration {

	private static final Logger logger = LoggerFactory.getLogger(CyclonConfiguration.class);
	
	public final static String CYCLON_CONFIG_PROPERTIES_FILE = "config/cyclon.properties";
	
	public final static String PROP_CYCLON_MONITOR_ID = "cyclon.monitorid";
	public final static String PROP_CYCLON_SHUFFLE_PERIOD = "cyclon.shuffle.period";
	public final static String PROP_CYCLON_SHUFFLE_LENGTH = "cyclon.shuffle.length";
	public final static String PROP_CYCLON_SHUFFLE_TIMEOUT = "cyclon.shuffle.timeout";
	public final static String PROP_CYCLON_CACHE_SIZE = "cyclon.cache.size";
	public final static String PROP_CYCLON_ID_SPACE_SIZE = "cyclon.idspace.size";
	public final static String PROP_CYCLON_BOOTSTRAP_NUM_PEERS = "cyclon.bootstrap.numpeers";
	
	protected final static int DEFAULT_CYCLON_MONITOR_ID = Integer.MAX_VALUE - 1;
	protected final static int DEFAULT_CYCLON_SHUFFLE_PERIOD = 5000;
	protected final static int DEFAULT_CYCLON_SHUFFLE_LENGTH = 5;
	protected final static int DEFAULT_CYCLON_SHUFFLE_TIMEOUT = 5000;
	protected final static int DEFAULT_CYCLON_CACHE_SIZE = 10;
	protected final static String DEFAULT_CYCLON_IDSPACE_SIZE = "10000";
	protected final static int DEFAULT_CYCLON_BOOTSTRAP_NUM_PEERS = 5;
	
	protected Address cyclonMonitorServerAddress = null;

	/********************************************************/
	/********* Helper fields ********************************/
	/********************************************************/
	protected Option cyclonMonitorIdOption;
	
	protected Option shuffleLengthOption;
	protected Option shufflePeriodOption;
	protected Option shuffleTimeoutOption;
	protected Option cacheSizeOption;
	protected Option idSpaceSizeOption;
	protected Option bootstrapRequestPeerCountOption;

	protected PropertiesConfiguration cyclonConfig;
	
	protected static boolean cyclonInitialized = false;
	
	/**
	 * 
	 * @param args
	 * @throws IOException
	 */
	public CyclonConfiguration(String[] args) throws IOException, ConfigurationException {
		super(args);
		cyclonInitialized = true;
	}

	@Override
	protected void parseAdditionalOptions(String[] args) throws IOException {
		
		cyclonMonitorIdOption = new Option("monitorid", true, "Cyclon monitor-id");
		cyclonMonitorIdOption.setArgName("id");
		options.addOption(cyclonMonitorIdOption);
		
		shuffleLengthOption = new Option("shufflelength", true, "the number of descriptors exchanged during a shuffle");
		shuffleLengthOption.setArgName("shufflelength");
		options.addOption(shuffleLengthOption);

		shufflePeriodOption = new Option("shuffleperiod", true, "Gossiping round time: the number of milliseconds between two consecutive shuffles initiated by a peer");
		shufflePeriodOption.setArgName("shuffleperiod");
		options.addOption(shufflePeriodOption);

		shuffleTimeoutOption = new Option("shuffletimeout", true, "num. of milliseconds after which a node that does not respond to a shuffle request is considered dead");
		shuffleTimeoutOption.setArgName("shuffletimeout");
		options.addOption(shuffleTimeoutOption);

		cacheSizeOption = new Option("cachesize", true, "the size of the cache of each Cyclon node");
		cacheSizeOption.setArgName("cachesize");
		options.addOption(cacheSizeOption);
		
		idSpaceSizeOption = new Option("idspace", true, "the size of the identifier space");
		idSpaceSizeOption.setArgName("idspace");
		options.addOption(idSpaceSizeOption);
		
		bootstrapRequestPeerCountOption = new Option("bootpeers", true, "the number of peers to request from Bootstrap server");
		bootstrapRequestPeerCountOption.setArgName("bootpeers");
		options.addOption(bootstrapRequestPeerCountOption);

	}

	@Override
	protected void processAdditionalOptions() throws IOException {
		
		try {
			cyclonConfig = new PropertiesConfiguration(CYCLON_CONFIG_PROPERTIES_FILE);
			cyclonConfig.setReloadingStrategy(new FileChangedReloadingStrategy());
			compositeConfig.addConfiguration(cyclonConfig);
		}
		catch (ConfigurationException e)
		{
			logger.warn("Configuration file for cyclon not found, using default values: " + CYCLON_CONFIG_PROPERTIES_FILE);
		}

		if (line.hasOption(cyclonMonitorIdOption.getOpt()))
		{
			int cyclonMonitorId = new Integer(line.getOptionValue(cyclonMonitorIdOption.getOpt()));
			configuration.compositeConfig.setProperty(PROP_CYCLON_MONITOR_ID, cyclonMonitorId);
		}
		if (line.hasOption(shufflePeriodOption.getOpt()))
		{
			int sp = new Integer(line.getOptionValue(shufflePeriodOption.getOpt()));
			configuration.compositeConfig.setProperty(PROP_CYCLON_SHUFFLE_PERIOD, sp);
		}
		if (line.hasOption(shuffleLengthOption.getOpt()))
		{
			int sl = new Integer(line.getOptionValue(shuffleLengthOption.getOpt()));
			configuration.compositeConfig.setProperty(PROP_CYCLON_SHUFFLE_LENGTH, sl);
		}
		if (line.hasOption(shuffleTimeoutOption.getOpt()))
		{
			int st = new Integer(line.getOptionValue(shuffleTimeoutOption.getOpt()));
			configuration.compositeConfig.setProperty(PROP_CYCLON_SHUFFLE_TIMEOUT, st);
		}
		if (line.hasOption(cacheSizeOption.getOpt()))
		{
			int cs = new Integer(line.getOptionValue(cacheSizeOption.getOpt()));
			configuration.compositeConfig.setProperty(PROP_CYCLON_CACHE_SIZE, cs);
		}
		if (line.hasOption(idSpaceSizeOption.getOpt()))
		{
			int idss = new Integer(line.getOptionValue(idSpaceSizeOption.getOpt()));
			configuration.compositeConfig.setProperty(PROP_CYCLON_ID_SPACE_SIZE, idss);
		}
		if (line.hasOption(bootstrapRequestPeerCountOption.getOpt()))
		{
			int bp = new Integer(line.getOptionValue(bootstrapRequestPeerCountOption.getOpt()));
			configuration.compositeConfig.setProperty(PROP_CYCLON_BOOTSTRAP_NUM_PEERS, bp);
		}

	}

	@Override
	protected int getMonitorId() {
		cyclonInitialized();
		return configuration.compositeConfig.getInt(PROP_CYCLON_MONITOR_ID, DEFAULT_CYCLON_MONITOR_ID);
	}

	@Override
	protected Address getMonitorServerAddress() throws LocalIPAddressNotFound {
		cyclonInitialized();
		if (cyclonMonitorServerAddress == null){
			cyclonMonitorServerAddress = new Address(getIp(), getPort(), getMonitorId());
		}			
		return cyclonMonitorServerAddress;
	}
	
	static public int getShufflePeriod() {
		cyclonInitialized();
		return configuration.compositeConfig.getInt(PROP_CYCLON_SHUFFLE_PERIOD, 
				DEFAULT_CYCLON_SHUFFLE_PERIOD);
	}
	
	static public int getShuffleLength() {
		cyclonInitialized();
		return configuration.compositeConfig.getInt(PROP_CYCLON_SHUFFLE_LENGTH, 
				DEFAULT_CYCLON_SHUFFLE_LENGTH);
	}
	
	static public int getShuffleTimeout() {
		cyclonInitialized();
		return configuration.compositeConfig.getInt(PROP_CYCLON_SHUFFLE_TIMEOUT, 
				DEFAULT_CYCLON_SHUFFLE_TIMEOUT);
	}

	static public int getCacheSize() {
		cyclonInitialized();
		return configuration.compositeConfig.getInt(PROP_CYCLON_CACHE_SIZE, 
				DEFAULT_CYCLON_CACHE_SIZE);
	}	
	
	static public BigInteger getIdSpaceSize() {
		cyclonInitialized();
		String idSpaceSize = configuration.compositeConfig.getString(PROP_CYCLON_ID_SPACE_SIZE, 
				DEFAULT_CYCLON_IDSPACE_SIZE);
		return new BigInteger(idSpaceSize);
	}	
	
	
	static public int getBootstrapRequestPeerCount() {
		cyclonInitialized();
		return configuration.compositeConfig.getInt(PROP_CYCLON_BOOTSTRAP_NUM_PEERS, 
				DEFAULT_CYCLON_BOOTSTRAP_NUM_PEERS);
	}
	
	static protected void cyclonInitialized() {
		baseInitialized();
		if (cyclonInitialized == false)
		{
			throw new IllegalStateException("ChordConfiguration not initialized before use.");
		}
	}
}
