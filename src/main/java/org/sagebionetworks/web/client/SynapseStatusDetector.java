package org.sagebionetworks.web.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.inject.Inject;

public class SynapseStatusDetector {

	public static final int INTERVAL_MS = 1000*60; //check once every minute
	PopupUtilsView popupUtils;
	GWTWrapper gwt;
	public static final String STATUS_PAGE_IO_PAGE = "kh896k90gyvg";
	DateTimeFormat iso8601DateFormat = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
	DateTimeUtils dateTimeUtils;
	@Inject
	public SynapseStatusDetector(
			GWTWrapper gwt,
			PopupUtilsView popupUtils,
			DateTimeUtils dateTimeUtils) {
		this.gwt = gwt;
		this.popupUtils = popupUtils;
		this.dateTimeUtils = dateTimeUtils;
	}
	
	public void start() {
		gwt.scheduleFixedDelay(() -> {
			_getCurrentStatus(this);
			_getUnresolvedIncidents(this);
			_getActiveScheduledMaintenance(this);
		}, INTERVAL_MS);
	}
	
	public void showScheduledMaintenance(String moreInfo, String scheduledFor, String scheduledUntil) {
		String moreInfoString = moreInfo != null ? ": " + moreInfo : "";
		Date startDate = getDate(scheduledFor);
		Date endDate = getDate(scheduledUntil);
		String startTime = dateTimeUtils.getDateTimeString(startDate);
		Long seconds = (endDate.getTime() - startDate.getTime()) / 1000L;
		String friendlyTimeEstimate = dateTimeUtils.getFriendlyTimeEstimate(seconds);
		popupUtils.showInfo("<a href=\"http://status.synapse.org/\" target=\"_blank\" class=\"color-white\">Scheduled maintenance beginning at " + startTime + ", expecting to last for approximately " + friendlyTimeEstimate  + moreInfoString + "</a>", INTERVAL_MS - 1200);
	}
	
	public Date getDate(String iso8601DateString) {
		Date result = null;
		try {
			result = iso8601DateFormat.parse(iso8601DateString);
		} catch (Exception e)
		{
			SynapseJSNIUtilsImpl._consoleError(e);
		}
		return result;
	}
	
	public void showOutage(String info) {
		popupUtils.showError("<a href=\"http://status.synapse.org/\" target=\"_blank\" class=\"color-white\">" + info + "</a>", INTERVAL_MS - 1200);
	}
	
	private static native void _getCurrentStatus(SynapseStatusDetector x) /*-{
		var sp = new $wnd.StatusPage.page({
			page : @org.sagebionetworks.web.client.SynapseStatusDetector::STATUS_PAGE_IO_PAGE
		});
		sp.status({
			success : function(data) {
				if (data.status.indicator !== 'none') {
					// Houston, we have a problem...
					var description = data.status.description;
					x.@org.sagebionetworks.web.client.SynapseStatusDetector::showOutage(Ljava/lang/String;)(description);
				}
			}
		});
	}-*/;
	
	private static native void _getUnresolvedIncidents(SynapseStatusDetector x) /*-{
		var sp = new $wnd.StatusPage.page({
			page : @org.sagebionetworks.web.client.SynapseStatusDetector::STATUS_PAGE_IO_PAGE
		});
		sp.incidents({
			filter : 'unresolved',
			success: function(data) {
				if (data.incidents[0]) {
					var incident = data.incidents[0];
					var description = incident.name;
					x.@org.sagebionetworks.web.client.SynapseStatusDetector::showOutage(Ljava/lang/String;)(description);
				}
			}
		})
		
		sp.status({
			success : function(data) {
				if (data.status.indicator !== 'none') {
					// Houston, we have a problem...
					var description = data.status.description;
					x.@org.sagebionetworks.web.client.SynapseStatusDetector::showOutage(Ljava/lang/String;)(description);
				}
			}
		});
	}-*/;

	
	private static native void _getActiveScheduledMaintenance(SynapseStatusDetector x) /*-{
		var sp = new $wnd.StatusPage.page({
			page : @org.sagebionetworks.web.client.SynapseStatusDetector::STATUS_PAGE_IO_PAGE
		});
		sp.scheduled_maintenances({
			filter : 'upcoming',
			success : function(data) {
				if (data.scheduled_maintenances[0]) {
					var scheduledMaintenance = data.scheduled_maintenances[0];
					var scheduledFor = scheduledMaintenance.scheduled_for;
					var scheduledUntil = scheduledMaintenance.scheduled_until;
					// there's a scheduled maintenance. invoke alert with info.
					if (scheduledMaintenance.incident_updates && scheduledMaintenance.incident_updates[0]) {
						// we have more information, invoke with more information
						var info = scheduledMaintenance.incident_updates[0].body;
						x.@org.sagebionetworks.web.client.SynapseStatusDetector::showScheduledMaintenance(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(info, scheduledFor, scheduledUntil);
					} else {
						x.@org.sagebionetworks.web.client.SynapseStatusDetector::showScheduledMaintenance(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(null, scheduledFor, scheduledUntil);
					}
				}
			}
		});
	}-*/;

}
