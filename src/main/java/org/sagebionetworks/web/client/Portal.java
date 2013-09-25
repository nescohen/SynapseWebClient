package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.mvp.AppActivityMapper;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Portal implements EntryPoint {
	
	//  We are using gin to create all of our objects
	private final PortalGinInjector ginjector = GWT.create(PortalGinInjector.class);
	private final AppLoadingView loading = GWT.create(AppLoadingView.class);

	private SimplePanel appWidget = new SimplePanel();

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		//we might need to reload using the new token scheme (required for SEO)
		String initToken = History.getToken();
		if (initToken.length() > 0 && !initToken.startsWith("!")) {
			String fullUrl = Window.Location.getHref();
			fullUrl = fullUrl.replace("#"+initToken, "#!"+initToken);
			Window.Location.replace(fullUrl);
			Window.Location.reload();
		} else {
			// Show a loading dialog while we downlaod code
			loading.showWidget();
			// This is a split point where the browser can download the first large code file.
			GWT.runAsync(new RunAsyncCallback() {
				@Override
				public void onFailure(Throwable reason) {
					// Not sure what to do here.
					loading.hide();
					Window.alert(reason.getMessage());	
				}

				@Override
				public void onSuccess() {
					try {
						EventBus eventBus = ginjector.getEventBus();
						PlaceController placeController = new PlaceController(eventBus);

						// Start ActivityManager for the main widget with our ActivityMapper
						AppActivityMapper activityMapper = new AppActivityMapper(ginjector, new SynapseJSNIUtilsImpl(), loading);
						ActivityManager activityManager = new ActivityManager(activityMapper, eventBus);
						activityManager.setDisplay(appWidget);
						
						// All pages get added to the root panel
						appWidget.addStyleName("rootPanel");

						// Start PlaceHistoryHandler with our PlaceHistoryMapper
						AppPlaceHistoryMapper historyMapper = GWT.create(AppPlaceHistoryMapper.class);		
						PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);		
						historyHandler.register(placeController, eventBus, activityMapper.getDefaultPlace());						
						
						RootPanel.get("rootPanel").add(appWidget);

						GlobalApplicationState globalApplicationState = ginjector.getGlobalApplicationState();
						globalApplicationState.setPlaceController(placeController);
						globalApplicationState.setAppPlaceHistoryMapper(historyMapper);
						globalApplicationState.setActivityMapper(activityMapper);

						// start version timer
						ginjector.getVersionTimer().start();
						
						// Goes to place represented on URL or default place
						historyHandler.handleCurrentHistory();
						loading.hide();
					} catch (Throwable e) {
						onFailure(e);
					}
				}
			});
			
		}
	}
}
