package br.ufpe.cin.dsoa.jmx.monitor;

import java.util.Map;

public interface PlatformMonitorMBean {

	public void startMonitoring();

	public void stopMonitoring();

	public void setInterval(long interval);

	public long getInterval();

	public Map<String, Object> getStatus();

}