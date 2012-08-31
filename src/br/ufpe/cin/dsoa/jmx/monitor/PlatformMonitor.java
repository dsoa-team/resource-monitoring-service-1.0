package br.ufpe.cin.dsoa.jmx.monitor;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;
import javax.management.ReflectionException;

public class PlatformMonitor extends NotificationBroadcasterSupport implements
		PlatformMonitorMBean, Runnable {

	public static final String STATUS_TYPE = "resource.status";

	private static String OBJECT_NAME = "jmx.monitor:Type=PlatformMonitor";
	private static final String SYSTEM_LOAD = "SystemLoadAverage";
	private static final String FREE_PHYSICAL = "FreePhysicalMemorySize";
	private static final String TOTAL_PHYSICAL = "TotalPhysicalMemorySize";
	private static final String TOTAL_SWAP_MEM = "TotalSwapSpaceSize";
	private static final String FREE_SWAP_MEM = "FreeSwapSpaceSize";
	private static final String SHARED_MEM = "CommittedVirtualMemorySize";

	private long sequenceNumber = 0;
	private long interval = 3000;
	private Map<String, Object> status = new HashMap<String, Object>();

	private Thread monitoringThread;

	private MBeanServer mbeanServer;
	private ObjectName platformMonitor;

	public void start() {
		this.mbeanServer = ManagementFactory.getPlatformMBeanServer();

		try {
			platformMonitor = new ObjectName(OBJECT_NAME);
			this.mbeanServer.registerMBean(this, platformMonitor);

		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (InstanceAlreadyExistsException e) {
			e.printStackTrace();
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (NotCompliantMBeanException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			this.mbeanServer.unregisterMBean(platformMonitor);
		} catch (MBeanRegistrationException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void startMonitoring() {

		if (monitoringThread == null) {
			monitoringThread = new Thread(this);
		}
		if (!monitoringThread.isAlive()) {
			monitoringThread.start();
		}
	}

	public void stopMonitoring() {
		monitoringThread.interrupt();
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public long getInterval() {
		return this.interval;
	}

	public Map<String, Object> getStatus() {
		return this.status;
	}

	public void run() {
		this.poolingSystemProperties();
	}

	private void poolingSystemProperties() {
		try {
			ObjectName name = new ObjectName(
					ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME);

			while (true) {

				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
				}

				status = new HashMap<String, Object>();

				status.put(SYSTEM_LOAD, getCpuStatus(mbeanServer, name));
				status.put(FREE_PHYSICAL,
						getPhysicalMemoryStatus(mbeanServer, name));

				this.sendNotification(STATUS_TYPE, status);
			}

		} catch (MalformedObjectNameException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (AttributeNotFoundException e) {
			e.printStackTrace();
		} catch (InstanceNotFoundException e) {
			e.printStackTrace();
		} catch (MBeanException e) {
			e.printStackTrace();
		} catch (ReflectionException e) {
			e.printStackTrace();
		}
	}

	private Object getCpuStatus(MBeanServer mbeanServer, ObjectName name)
			throws AttributeNotFoundException, InstanceNotFoundException,
			MBeanException, ReflectionException {

		Object systemLoad = mbeanServer.getAttribute(name, SYSTEM_LOAD);
		return systemLoad;
	}

	private Object getPhysicalMemoryStatus(MBeanServer mbeanServer, ObjectName name)
			throws AttributeNotFoundException, InstanceNotFoundException,
			MBeanException, ReflectionException {

		Long totalPhysical = (Long) mbeanServer.getAttribute(name,
				TOTAL_PHYSICAL);
		Long freePhysical = (Long) mbeanServer.getAttribute(name,
				FREE_PHYSICAL);
		double percentPhysical = (double) ((freePhysical * 100 / totalPhysical));

		return percentPhysical;
	}
	
	@SuppressWarnings("unused")
	private Object getSwapMemoryStatus(MBeanServer mbeanServer, ObjectName name)
			throws AttributeNotFoundException, InstanceNotFoundException,
			MBeanException, ReflectionException {

		Long freeSwap = (Long) mbeanServer.getAttribute(name, FREE_SWAP_MEM);

		Long totalSwap = (Long) mbeanServer.getAttribute(name, TOTAL_SWAP_MEM);

		Double percentSwap = (double) ((freeSwap * 100) / totalSwap);

		return percentSwap;

	}

	@SuppressWarnings("unused")
	private Object getSharedMemoryStatus(MBeanServer mbeamServer,
			ObjectName name) throws AttributeNotFoundException,
			InstanceNotFoundException, MBeanException, ReflectionException {
		Long sharedMemory = (Long) mbeanServer.getAttribute(name, SHARED_MEM);

		Long sharedMbyteMemory = sharedMemory / 1000000;

		return sharedMbyteMemory;
	}

	private void sendNotification(String message, Map<String, Object> status) {

		Notification notification = new Notification(
				PlatformMonitor.STATUS_TYPE, platformMonitor,
				this.sequenceNumber++, message);

		notification.setUserData(status);
		//TODO: enviar queue status (ou o manager da queue tornar-se listener JMX)
		super.sendNotification(notification);
	}

}
